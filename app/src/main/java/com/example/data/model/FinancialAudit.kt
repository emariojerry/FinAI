package com.example.data.model

data class FinancialAudit(
    val auditDate: String,
    val healthScore: Int, // 0 - 100
    val scoreRating: String, // "Good", "Needs Attention", "Solid", etc.
    val executiveSummary: String,
    val totalIncome: Double,
    val totalExpenses: Double,
    val netCashFlow: Double,
    val savingsRate: Double, // % e.g. 21.9%
    val fixedExpenses: Double,
    val variableExpenses: Double,
    val debtPayments: Double,
    val debtToIncomeRatio: Double, // % e.g. 15.2%
    val totalAssets: Double,
    val totalLiabilities: Double,
    val netWorth: Double,
    val liquidAssets: Double,
    val nonLiquidAssets: Double,
    val moneyLeakages: List<MoneyLeakage> = emptyList(),
    val potentialMonthlySavings: Double = 0.0,
    val potentialAnnualSavings: Double = 0.0,
    val topSpendingCategories: List<CategorySpend> = emptyList(),
    val largestTransactions: List<Transaction> = emptyList(),
    val smallDiscretionaryCount: Int = 0,
    val smallDiscretionaryTotal: Double = 0.0,
    val bankFeesTotal: Double = 0.0,
    val strongAreas: List<String> = emptyList(),
    val areasToImprove: List<String> = emptyList(),
    val recommendedActions: List<String> = emptyList(),
    val resetPlan30Days: List<ActionPlanItem> = emptyList(),
    val currency: String = "NGN"
)

data class CategorySpend(
    val category: TransactionCategory,
    val amount: Double,
    val percentageOfSpend: Double,
    val transactionCount: Int
)

data class ActionPlanItem(
    val title: String,
    val description: String,
    val potentialMonthlySavings: Double,
    val category: String
)
