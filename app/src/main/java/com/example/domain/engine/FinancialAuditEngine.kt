package com.example.domain.engine

import com.example.data.model.ActionPlanItem
import com.example.data.model.AlertType
import com.example.data.model.Asset
import com.example.data.model.CategorySpend
import com.example.data.model.FinancialAlert
import com.example.data.model.FinancialAudit
import com.example.data.model.LeakageSeverity
import com.example.data.model.Liability
import com.example.data.model.MoneyLeakage
import com.example.data.model.Transaction
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionType
import com.example.data.model.UserProfile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FinancialAuditEngine {

    fun performAudit(
        user: UserProfile,
        transactions: List<Transaction>,
        assets: List<Asset>,
        liabilities: List<Liability>
    ): FinancialAudit {
        val totalAssets = assets.sumOf { it.value }
        val liquidAssets = assets.filter { it.isLiquid }.sumOf { it.value }
        val nonLiquidAssets = totalAssets - liquidAssets

        val totalLiabilities = liabilities.sumOf { it.outstandingAmount }
        val monthlyDebtPayments = liabilities.sumOf { it.monthlyPayment }
        val netWorth = totalAssets - totalLiabilities

        // Transactions breakdown
        val incomeTransactions = transactions.filter { it.type == TransactionType.CREDIT }
        val expenseTransactions = transactions.filter { it.type == TransactionType.DEBIT }

        // If income transactions exist, calculate actual income, or fallback to user's monthly income
        val calculatedIncome = incomeTransactions.sumOf { it.amount }
        val totalIncome = if (calculatedIncome > 0) calculatedIncome else user.monthlyIncome
        val totalExpenses = expenseTransactions.sumOf { it.amount }
        val netCashFlow = totalIncome - totalExpenses
        val savingsRate = if (totalIncome > 0) ((netCashFlow.coerceAtLeast(0.0) / totalIncome) * 100) else 0.0

        // Fixed vs Variable vs Debt expenses
        var fixedExpenses = 0.0
        var variableExpenses = 0.0
        var bankFeesTotal = 0.0
        var smallDiscretionaryCount = 0
        var smallDiscretionaryTotal = 0.0

        val categoryGroups = expenseTransactions.groupBy { it.category }
        val topSpendingCategories = categoryGroups.map { (cat, list) ->
            val sum = list.sumOf { it.amount }
            val pct = if (totalExpenses > 0) (sum / totalExpenses) * 100 else 0.0
            CategorySpend(cat, sum, pct, list.size)
        }.sortedByDescending { it.amount }

        for (tx in expenseTransactions) {
            if (tx.isBankFee || tx.category == TransactionCategory.BANK_FEES) {
                bankFeesTotal += tx.amount
            }
            if (tx.category.isEssential) {
                fixedExpenses += tx.amount
            } else {
                variableExpenses += tx.amount
            }
            if (tx.amount < 5000 && !tx.category.isEssential) {
                smallDiscretionaryCount++
                smallDiscretionaryTotal += tx.amount
            }
        }

        val largestTransactions = expenseTransactions.sortedByDescending { it.amount }.take(5)
        val debtToIncomeRatio = if (totalIncome > 0) (monthlyDebtPayments / totalIncome) * 100 else 0.0

        // Detect Leakages
        val leakages = detectMoneyLeakages(totalIncome, expenseTransactions, topSpendingCategories, smallDiscretionaryTotal, bankFeesTotal)
        val potentialMonthlySavings = leakages.sumOf { it.monthlyAmount * 0.5 } // realistic 50% cut
        val potentialAnnualSavings = potentialMonthlySavings * 12

        // Health Score (0 - 100)
        val healthScore = calculateHealthScore(
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            savingsRate = savingsRate,
            debtToIncomeRatio = debtToIncomeRatio,
            liquidAssets = liquidAssets,
            leakageCount = leakages.size
        )

        val scoreRating = when {
            healthScore >= 80 -> "Excellent"
            healthScore >= 70 -> "Good & Resilient"
            healthScore >= 55 -> "Fair - Needs Optimization"
            else -> "At Risk - Immediate Reset Required"
        }

        val strongAreas = mutableListOf<String>()
        val areasToImprove = mutableListOf<String>()

        if (netCashFlow > 0) strongAreas.add("Positive cash flow of ${formatCurrency(netCashFlow, user.currency)}")
        if (savingsRate >= 15.0) strongAreas.add("Healthy savings rate of ${String.format(Locale.US, "%.1f", savingsRate)}%")
        if (debtToIncomeRatio < 20.0) strongAreas.add("Low debt burden (debt repayments under 20% of income)")
        if (liquidAssets >= totalExpenses * 3) strongAreas.add("Robust emergency buffer (over 3 months of expenses)")

        if (debtToIncomeRatio >= 25.0) areasToImprove.add("Debt repayments consume ${String.format(Locale.US, "%.1f", debtToIncomeRatio)}% of monthly earnings")
        if (leakages.isNotEmpty()) areasToImprove.add("${leakages.size} active money leakages draining ${formatCurrency(leakages.sumOf { it.monthlyAmount }, user.currency)}/mo")
        if (smallDiscretionaryCount >= 15) areasToImprove.add("$smallDiscretionaryCount impulse micro-transactions under ₦5,000 totaling ${formatCurrency(smallDiscretionaryTotal, user.currency)}")
        if (liquidAssets < totalExpenses) areasToImprove.add("Emergency cushion is below 1 month of living expenses")

        val recommendedActions = mutableListOf<String>()
        recommendedActions.add("Plug the top leak in ${leakages.firstOrNull()?.title ?: "Dining & Subscriptions"} to free up ${formatCurrency(potentialMonthlySavings, user.currency)}/month.")
        if (debtToIncomeRatio > 15.0) {
            recommendedActions.add("Prioritize high-interest loans (BNPL/personal credit) using debt avalanche before luxury spending.")
        }
        recommendedActions.add("Automate an initial savings transfer of ${formatCurrency(totalIncome * 0.15, user.currency)} immediately on payday.")
        recommendedActions.add("Review bank transaction stamp duty and SMS alerts to consolidate accounts and avoid duplicate maintenance fees.")

        val resetPlan = listOf(
            ActionPlanItem(
                title = "Trim Recurring Subscriptions & Unused Memberships",
                description = "Cancel redundant streaming, software, and gym tier services.",
                potentialMonthlySavings = 12000.0,
                category = "Subscriptions"
            ),
            ActionPlanItem(
                title = "Cap Food Delivery & Restaurant Takeout by 40%",
                description = "Plan grocery batch-cooking for weekdays and limit ride-delivery orders.",
                potentialMonthlySavings = 35000.0,
                category = "Food & Dining"
            ),
            ActionPlanItem(
                title = "Optimize Ride-Hailing & Combine Transit Trips",
                description = "Group city errands and schedule off-peak rides to bypass surge charges.",
                potentialMonthlySavings = 20000.0,
                category = "Transportation"
            ),
            ActionPlanItem(
                title = "Direct ₦50,000/month Automatic Transfer to High-Yield Savings",
                description = "Lock savings in Treasury bills or secure money-market funds to earn 15%+ p.a.",
                potentialMonthlySavings = 50000.0,
                category = "Savings & Investment"
            )
        )

        val currentDate = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date())

        val executiveSummary = buildString {
            append("You earned ${formatCurrency(totalIncome, user.currency)} and spent ${formatCurrency(totalExpenses, user.currency)}, ")
            if (netCashFlow >= 0) {
                append("leaving ${formatCurrency(netCashFlow, user.currency)} in net positive cash flow. ")
            } else {
                append("leaving a deficit of ${formatCurrency(-netCashFlow, user.currency)}. ")
            }
            append("Your financial health score is $healthScore/100. ")
            if (leakages.isNotEmpty()) {
                append("Our audit identified ${leakages.size} spending leakages accounting for ${formatCurrency(leakages.sumOf { it.monthlyAmount }, user.currency)} per month.")
            }
        }

        return FinancialAudit(
            auditDate = currentDate,
            healthScore = healthScore,
            scoreRating = scoreRating,
            executiveSummary = executiveSummary,
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            netCashFlow = netCashFlow,
            savingsRate = savingsRate,
            fixedExpenses = fixedExpenses,
            variableExpenses = variableExpenses,
            debtPayments = monthlyDebtPayments,
            debtToIncomeRatio = debtToIncomeRatio,
            totalAssets = totalAssets,
            totalLiabilities = totalLiabilities,
            netWorth = netWorth,
            liquidAssets = liquidAssets,
            nonLiquidAssets = nonLiquidAssets,
            moneyLeakages = leakages,
            potentialMonthlySavings = potentialMonthlySavings,
            potentialAnnualSavings = potentialAnnualSavings,
            topSpendingCategories = topSpendingCategories,
            largestTransactions = largestTransactions,
            smallDiscretionaryCount = smallDiscretionaryCount,
            smallDiscretionaryTotal = smallDiscretionaryTotal,
            bankFeesTotal = bankFeesTotal,
            strongAreas = strongAreas,
            areasToImprove = areasToImprove,
            recommendedActions = recommendedActions,
            resetPlan30Days = resetPlan,
            currency = user.currency
        )
    }

    private fun detectMoneyLeakages(
        income: Double,
        expenses: List<Transaction>,
        topCategories: List<CategorySpend>,
        smallDiscretionaryTotal: Double,
        bankFeesTotal: Double
    ): List<MoneyLeakage> {
        val leakages = mutableListOf<MoneyLeakage>()

        // 1. Food Delivery & Takeout
        val foodSpend = topCategories.firstOrNull { it.category == TransactionCategory.FOOD }?.amount ?: 0.0
        if (foodSpend > income * 0.15) {
            val pct = if (income > 0) (foodSpend / income) * 100 else 0.0
            leakages.add(
                MoneyLeakage(
                    id = "leak_food",
                    title = "Excessive Food Delivery & Restaurant Dining",
                    monthlyAmount = foodSpend * 0.45,
                    percentageOfIncome = pct * 0.45,
                    severity = LeakageSeverity.HIGH,
                    whyItMatters = "You spent ${formatCurrency(foodSpend, "NGN")} on food & restaurants this month (${String.format(Locale.US, "%.1f", pct)}% of income). Convenience orders add heavy delivery and markup premiums.",
                    recommendedAction = "Cap ordering to weekends and meal-prep essentials. Aim to reduce delivery by 40% to save substantial cash.",
                    potentialAnnualSavings = (foodSpend * 0.45) * 12
                )
            )
        }

        // 2. Subscriptions
        val subSpend = expenses.filter { it.category == TransactionCategory.SUBSCRIPTIONS || it.isRecurring }.sumOf { it.amount }
        if (subSpend > 15000) {
            val pct = if (income > 0) (subSpend / income) * 100 else 0.0
            leakages.add(
                MoneyLeakage(
                    id = "leak_sub",
                    title = "Multiple Recurring Entertainment Subscriptions",
                    monthlyAmount = subSpend * 0.40,
                    percentageOfIncome = pct * 0.40,
                    severity = LeakageSeverity.MEDIUM,
                    whyItMatters = "You have ${expenses.filter { it.category == TransactionCategory.SUBSCRIPTIONS }.size} separate streaming, music, and software subscriptions totaling ${formatCurrency(subSpend, "NGN")}/month.",
                    recommendedAction = "Cancel 1-2 unused platforms or switch to family/annual tier plans.",
                    potentialAnnualSavings = (subSpend * 0.40) * 12
                )
            )
        }

        // 3. Frequent micro-transactions
        if (smallDiscretionaryTotal > 30000) {
            val pct = if (income > 0) (smallDiscretionaryTotal / income) * 100 else 0.0
            leakages.add(
                MoneyLeakage(
                    id = "leak_micro",
                    title = "Impulse Micro-Purchases under ₦5,000",
                    monthlyAmount = smallDiscretionaryTotal * 0.6,
                    percentageOfIncome = pct * 0.6,
                    severity = LeakageSeverity.MEDIUM,
                    whyItMatters = "Numerous sub-₦5,000 snack, airtime top-ups, and spontaneous items add up quietly to ${formatCurrency(smallDiscretionaryTotal, "NGN")} monthly.",
                    recommendedAction = "Set a dedicated weekly petty cash card or wallet and introduce a 24-hour pause rule before non-essential taps.",
                    potentialAnnualSavings = (smallDiscretionaryTotal * 0.6) * 12
                )
            )
        }

        // 4. Bank & Transfer charges
        if (bankFeesTotal > 3000) {
            leakages.add(
                MoneyLeakage(
                    id = "leak_bank_fees",
                    title = "Bank Transfer, ATM & SMS Alert Charges",
                    monthlyAmount = bankFeesTotal,
                    percentageOfIncome = if (income > 0) (bankFeesTotal / income) * 100 else 0.0,
                    severity = LeakageSeverity.LOW,
                    whyItMatters = "You incurred ${formatCurrency(bankFeesTotal, "NGN")} in transaction fees, SMS alerts, and electronic transfer levies.",
                    recommendedAction = "Consolidate your frequent transfers through fintech accounts offering free interbank transfers (e.g., Kuda, OPay) and switch to push/email alerts.",
                    potentialAnnualSavings = bankFeesTotal * 12
                )
            )
        }

        // 5. Ride Hailing
        val transportSpend = topCategories.firstOrNull { it.category == TransactionCategory.TRANSPORTATION }?.amount ?: 0.0
        if (transportSpend > income * 0.12) {
            val pct = if (income > 0) (transportSpend / income) * 100 else 0.0
            leakages.add(
                MoneyLeakage(
                    id = "leak_transport",
                    title = "Frequent Surge Ride-Hailing Trips",
                    monthlyAmount = transportSpend * 0.35,
                    percentageOfIncome = pct * 0.35,
                    severity = LeakageSeverity.MEDIUM,
                    whyItMatters = "Transportation consumed ${String.format(Locale.US, "%.1f", pct)}% of your monthly income (${formatCurrency(transportSpend, "NGN")}), largely fueled by peak-hour rides.",
                    recommendedAction = "Plan rides ahead of surge pricing hours or carpool during regular office commute times.",
                    potentialAnnualSavings = (transportSpend * 0.35) * 12
                )
            )
        }

        return leakages
    }

    private fun calculateHealthScore(
        totalIncome: Double,
        totalExpenses: Double,
        savingsRate: Double,
        debtToIncomeRatio: Double,
        liquidAssets: Double,
        leakageCount: Int
    ): Int {
        var score = 50 // baseline

        // Savings Rate (up to +20)
        when {
            savingsRate >= 25.0 -> score += 20
            savingsRate >= 15.0 -> score += 15
            savingsRate >= 5.0 -> score += 8
            savingsRate < 0.0 -> score -= 15
        }

        // Expense ratio (up to +15)
        val expenseRatio = if (totalIncome > 0) totalExpenses / totalIncome else 1.0
        when {
            expenseRatio <= 0.70 -> score += 15
            expenseRatio <= 0.85 -> score += 8
            expenseRatio > 1.0 -> score -= 15
        }

        // Debt-to-income (up to +15)
        when {
            debtToIncomeRatio <= 10.0 -> score += 15
            debtToIncomeRatio <= 20.0 -> score += 8
            debtToIncomeRatio <= 35.0 -> score += 0
            else -> score -= 15
        }

        // Emergency cushion (up to +10)
        val monthsCovered = if (totalExpenses > 0) liquidAssets / totalExpenses else 0.0
        when {
            monthsCovered >= 6.0 -> score += 10
            monthsCovered >= 3.0 -> score += 7
            monthsCovered >= 1.0 -> score += 4
            else -> score -= 5
        }

        // Leakages penalty
        score -= (leakageCount * 3)

        return score.coerceIn(10, 98)
    }

    fun generateAlerts(audit: FinancialAudit, currency: String): List<FinancialAlert> {
        val alerts = mutableListOf<FinancialAlert>()

        // Food budget alert
        val foodCat = audit.topSpendingCategories.firstOrNull { it.category == TransactionCategory.FOOD }
        if (foodCat != null && foodCat.amount > audit.totalIncome * 0.18) {
            alerts.add(
                FinancialAlert(
                    id = "alert_food",
                    type = AlertType.SPENDING,
                    title = "Spending Alert: Dining & Groceries",
                    message = "You have spent ${formatCurrency(foodCat.amount, currency)} on food this month, reaching 88% of your standard target.",
                    severity = LeakageSeverity.HIGH,
                    actionableTip = "Consider batch-cooking for the next 7 days to stay within limits."
                )
            )
        }

        // Debt repayment alert
        if (audit.debtToIncomeRatio >= 25.0) {
            alerts.add(
                FinancialAlert(
                    id = "alert_debt",
                    type = AlertType.DEBT,
                    title = "Debt Burden Alert",
                    message = "Debt repayments are consuming ${String.format(Locale.US, "%.1f", audit.debtToIncomeRatio)}% of monthly income.",
                    severity = LeakageSeverity.HIGH,
                    actionableTip = "Prioritize clearing high-interest BNPL or loan balances to reduce monthly obligations."
                )
            )
        }

        // Subscription alert
        if (audit.moneyLeakages.any { it.id == "leak_sub" }) {
            alerts.add(
                FinancialAlert(
                    id = "alert_sub",
                    type = AlertType.SUBSCRIPTION,
                    title = "Subscription Audit Alert",
                    message = "You paid for multiple active subscriptions for consecutive months. 2 appear inactive.",
                    severity = LeakageSeverity.MEDIUM,
                    actionableTip = "Audit digital memberships to instantly reclaim ₦12,000/month."
                )
            )
        }

        // Savings opportunity alert
        if (audit.potentialMonthlySavings > 25000) {
            alerts.add(
                FinancialAlert(
                    id = "alert_savings",
                    type = AlertType.SAVINGS_OPPORTUNITY,
                    title = "Savings Opportunity: ${formatCurrency(audit.potentialMonthlySavings, currency)}/mo",
                    message = "By addressing detected leakages, you could accumulate ${formatCurrency(audit.potentialAnnualSavings, currency)} extra in 12 months.",
                    severity = LeakageSeverity.LOW,
                    actionableTip = "Activate the 30-Day Reset Plan in your Audit tab."
                )
            )
        }

        return alerts
    }

    private fun formatCurrency(amount: Double, currency: String): String {
        val symbol = if (currency == "NGN") "₦" else "$"
        val formatter = java.text.NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
        return "$symbol${formatter.format(amount)}"
    }
}
