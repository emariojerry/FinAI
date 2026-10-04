package com.example.data.repository

import com.example.data.gemini.GeminiAuditorService
import com.example.data.local.AppDatabase
import com.example.data.local.AssetEntity
import com.example.data.local.CategoryRuleEntity
import com.example.data.local.LiabilityEntity
import com.example.data.local.SavingsGoalEntity
import com.example.data.local.TransactionEntity
import com.example.data.model.Asset
import com.example.data.model.AssetType
import com.example.data.model.FinancialAlert
import com.example.data.model.FinancialAudit
import com.example.data.model.GoalCategory
import com.example.data.model.Liability
import com.example.data.model.LiabilityType
import com.example.data.model.SavingsGoal
import com.example.data.model.Transaction
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionType
import com.example.data.model.UserProfile
import com.example.domain.engine.FinancialAuditEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class FinanceRepository(
    private val database: AppDatabase,
    private val geminiService: GeminiAuditorService = GeminiAuditorService()
) {
    private val transactionDao = database.transactionDao()
    private val assetDao = database.assetDao()
    private val liabilityDao = database.liabilityDao()
    private val goalDao = database.savingsGoalDao()
    private val ruleDao = database.categoryRuleDao()

    val transactions: Flow<List<Transaction>> = transactionDao.getAllTransactions().map { list ->
        list.map { it.toDomain() }
    }

    val assets: Flow<List<Asset>> = assetDao.getAllAssets().map { list ->
        list.map { it.toDomain() }
    }

    val liabilities: Flow<List<Liability>> = liabilityDao.getAllLiabilities().map { list ->
        list.map { it.toDomain() }
    }

    val savingsGoals: Flow<List<SavingsGoal>> = goalDao.getAllGoals().map { list ->
        list.map { it.toDomain() }
    }

    val learnedRules: Flow<Map<String, TransactionCategory>> = ruleDao.getAllRules().map { list ->
        list.associate { it.pattern to TransactionCategory.fromString(it.category) }
    }

    // Reactive Audit calculation combining transactions, assets, liabilities
    fun getFinancialAudit(userProfile: UserProfile): Flow<FinancialAudit> {
        return combine(transactions, assets, liabilities) { txs, asts, liabs ->
            FinancialAuditEngine.performAudit(userProfile, txs, asts, liabs)
        }
    }

    // Reactive Alerts
    fun getFinancialAlerts(userProfile: UserProfile): Flow<List<FinancialAlert>> {
        return getFinancialAudit(userProfile).map { audit ->
            FinancialAuditEngine.generateAlerts(audit, userProfile.currency)
        }
    }

    suspend fun addTransaction(transaction: Transaction) {
        val rules = learnedRules.first()
        val (cat, confidence) = if (transaction.category == TransactionCategory.OTHER) {
            CategoryClassifier.classify(transaction.merchant, transaction.description, rules)
        } else {
            transaction.category to 1.0f
        }
        val enriched = transaction.copy(category = cat, aiConfidence = confidence)
        transactionDao.insertTransaction(TransactionEntity.fromDomain(enriched))
    }

    suspend fun importTransactions(list: List<Transaction>) {
        val entities = list.map { TransactionEntity.fromDomain(it) }
        transactionDao.insertAll(entities)
    }

    /**
     * Learning loop: Updates transaction and records category rule for this merchant/pattern.
     */
    suspend fun reclassifyTransaction(transactionId: Long, merchant: String, newCategory: TransactionCategory) {
        val existing = transactionDao.getTransactionById(transactionId)
        if (existing != null) {
            val updated = existing.copy(category = newCategory.name, aiConfidence = 1.0f)
            transactionDao.updateTransaction(updated)
        }
        // Save rule for future learning
        if (merchant.isNotBlank()) {
            ruleDao.insertRule(CategoryRuleEntity(pattern = merchant.trim().lowercase(), category = newCategory.name))
            // Also update any existing transactions from the same merchant
            transactionDao.updateCategoryForMerchant(merchant, newCategory.name)
        }
    }

    suspend fun addAsset(asset: Asset) {
        assetDao.insertAsset(AssetEntity.fromDomain(asset))
    }

    suspend fun confirmPotentialAsset(assetId: Long) {
        val all = assetDao.getAllAssets().first()
        val match = all.firstOrNull { it.id == assetId }
        if (match != null) {
            assetDao.updateAsset(match.copy(isPotential = false))
        }
    }

    suspend fun deleteAsset(asset: Asset) {
        assetDao.deleteAsset(AssetEntity.fromDomain(asset))
    }

    suspend fun addLiability(liability: Liability) {
        liabilityDao.insertLiability(LiabilityEntity.fromDomain(liability))
    }

    suspend fun deleteLiability(liability: Liability) {
        liabilityDao.deleteLiability(LiabilityEntity.fromDomain(liability))
    }

    suspend fun addGoal(goal: SavingsGoal) {
        goalDao.insertGoal(SavingsGoalEntity.fromDomain(goal))
    }

    suspend fun updateGoalProgress(goalId: Long, addedAmount: Double) {
        val all = goalDao.getAllGoals().first()
        val match = all.firstOrNull { it.id == goalId }
        if (match != null) {
            val updated = match.copy(currentAmount = match.currentAmount + addedAmount)
            goalDao.updateGoal(updated)
        }
    }

    suspend fun deleteGoal(goal: SavingsGoal) {
        goalDao.deleteGoal(SavingsGoalEntity.fromDomain(goal))
    }

    suspend fun askAuditor(query: String, userProfile: UserProfile): String {
        val currentTxs = transactions.first()
        val currentAssets = assets.first()
        val currentLiabs = liabilities.first()
        val audit = FinancialAuditEngine.performAudit(userProfile, currentTxs, currentAssets, currentLiabs)
        return geminiService.askAuditor(query, userProfile, audit, currentTxs)
    }

    suspend fun clearAllData() {
        transactionDao.clearAll()
        assetDao.clearAll()
        liabilityDao.clearAll()
        goalDao.clearAll()
    }

    suspend fun seedDemoData() {
        // Clear old demo data
        transactionDao.clearAll()
        assetDao.clearAll()
        liabilityDao.clearAll()
        goalDao.clearAll()

        // 1. Transactions (Realistic Nigerian Dataset)
        val demoTransactions = listOf(
            // Salary / Inflow
            Transaction(0, "2026-09-01", "Paystack HR", "Monthly Consulting Salary - Tech Corp", 950000.0, "NGN", TransactionType.CREDIT, TransactionCategory.BUSINESS, isRecurring = true),
            Transaction(0, "2026-09-05", "Side Project Client", "Inflow - FinTech Advisory Retainer", 120000.0, "NGN", TransactionType.CREDIT, TransactionCategory.BUSINESS),

            // Fixed Living / Housing
            Transaction(0, "2026-09-02", "Landlord Escrow", "Lekki Phase 1 Apartment Annual Rent Pool", 150000.0, "NGN", TransactionType.DEBIT, TransactionCategory.HOUSING, isRecurring = true),
            Transaction(0, "2026-09-03", "Estate Association", "Lekki Estate Security & Facility Dues", 25000.0, "NGN", TransactionType.DEBIT, TransactionCategory.HOUSING),
            Transaction(0, "2026-09-04", "IKEDC Prepaid", "Electricity NEPA Prepaid Meter Token", 35000.0, "NGN", TransactionType.DEBIT, TransactionCategory.UTILITIES),

            // Food & Dining (₦186,000 total leak pattern as specified in vision)
            Transaction(0, "2026-09-02", "Shoprite Ikeja", "Bi-weekly Grocery & Household Supplies", 42000.0, "NGN", TransactionType.DEBIT, TransactionCategory.FOOD),
            Transaction(0, "2026-09-04", "Chowdeck Lagos", "Dinner order - Gourmet Burger & Wings", 14500.0, "NGN", TransactionType.DEBIT, TransactionCategory.FOOD),
            Transaction(0, "2026-09-06", "Terra Kulture", "Weekend Brunch with Colleagues", 38000.0, "NGN", TransactionType.DEBIT, TransactionCategory.FOOD),
            Transaction(0, "2026-09-08", "Chowdeck Lagos", "Lunch delivery - Jollof Rice & Turkey", 11200.0, "NGN", TransactionType.DEBIT, TransactionCategory.FOOD),
            Transaction(0, "2026-09-10", "Domino's Pizza", "Mid-week Pizza Treat & Drinks", 16800.0, "NGN", TransactionType.DEBIT, TransactionCategory.FOOD),
            Transaction(0, "2026-09-11", "Glovo App", "Fast food delivery order", 12500.0, "NGN", TransactionType.DEBIT, TransactionCategory.FOOD),
            Transaction(0, "2026-09-13", "Spar Lekki", "Fruit, Bakery & Dairy run", 26000.0, "NGN", TransactionType.DEBIT, TransactionCategory.FOOD),
            Transaction(0, "2026-09-14", "Mega Chicken", "Weekend Family Takeaway", 25000.0, "NGN", TransactionType.DEBIT, TransactionCategory.FOOD),

            // Transportation (₦148,000 total)
            Transaction(0, "2026-09-03", "Bolt Lagos", "Airport Transfer to Domestic Terminal", 18500.0, "NGN", TransactionType.DEBIT, TransactionCategory.TRANSPORTATION),
            Transaction(0, "2026-09-05", "TotalEnergies VI", "Fuel full tank - Premium Motor Spirit", 45000.0, "NGN", TransactionType.DEBIT, TransactionCategory.TRANSPORTATION),
            Transaction(0, "2026-09-07", "Uber Nigeria", "Peak surge ride VI to Mainland", 14200.0, "NGN", TransactionType.DEBIT, TransactionCategory.TRANSPORTATION),
            Transaction(0, "2026-09-09", "Bolt Lagos", "Work commute ride", 8500.0, "NGN", TransactionType.DEBIT, TransactionCategory.TRANSPORTATION),
            Transaction(0, "2026-09-12", "NNPC Retail", "Fuel refill - PMS Lekki Expressway", 38000.0, "NGN", TransactionType.DEBIT, TransactionCategory.TRANSPORTATION),
            Transaction(0, "2026-09-14", "Uber Nigeria", "Evening ride after dinner", 9800.0, "NGN", TransactionType.DEBIT, TransactionCategory.TRANSPORTATION),
            Transaction(0, "2026-09-15", "Bolt Lagos", "Quick city meeting trip", 7000.0, "NGN", TransactionType.DEBIT, TransactionCategory.TRANSPORTATION),

            // Subscriptions
            Transaction(0, "2026-09-02", "Netflix Premium", "Monthly Streaming 4K Ultra", 5000.0, "NGN", TransactionType.DEBIT, TransactionCategory.SUBSCRIPTIONS, isRecurring = true),
            Transaction(0, "2026-09-03", "Spotify Family", "Music Streaming Subscription", 1500.0, "NGN", TransactionType.DEBIT, TransactionCategory.SUBSCRIPTIONS, isRecurring = true),
            Transaction(0, "2026-09-05", "MultiChoice DSTV", "DSTV Compact Plus Monthly Renewal", 19800.0, "NGN", TransactionType.DEBIT, TransactionCategory.SUBSCRIPTIONS, isRecurring = true),
            Transaction(0, "2026-09-06", "iFitness Gym", "Gym & Fitness Club Membership", 18000.0, "NGN", TransactionType.DEBIT, TransactionCategory.SUBSCRIPTIONS, isRecurring = true),
            Transaction(0, "2026-09-08", "Apple Services", "iCloud 2TB Storage Plan", 4900.0, "NGN", TransactionType.DEBIT, TransactionCategory.SUBSCRIPTIONS, isRecurring = true),

            // Bank fees & SMS charges
            Transaction(0, "2026-09-02", "GTBank Alerts", "Electronic SMS Notification Maintenance", 1200.0, "NGN", TransactionType.DEBIT, TransactionCategory.BANK_FEES, isBankFee = true),
            Transaction(0, "2026-09-05", "CBN Electronic Levy", "Stamp Duty on Interbank Transfers", 1850.0, "NGN", TransactionType.DEBIT, TransactionCategory.BANK_FEES, isBankFee = true),
            Transaction(0, "2026-09-10", "ATM Cash Withdrawal", "Remote ATM Surcharge Fee", 800.0, "NGN", TransactionType.DEBIT, TransactionCategory.BANK_FEES, isBankFee = true),

            // Small purchases under ₦5,000 (discretionary leak pattern)
            Transaction(0, "2026-09-03", "Cold Stone Creamery", "Afternoon Ice Cream", 4200.0, "NGN", TransactionType.DEBIT, TransactionCategory.FOOD),
            Transaction(0, "2026-09-04", "MTN Nigeria", "Quick Airtime / 5G Data Bundle", 3500.0, "NGN", TransactionType.DEBIT, TransactionCategory.UTILITIES),
            Transaction(0, "2026-09-05", "Suya Spot VI", "Evening Suya Pack", 4800.0, "NGN", TransactionType.DEBIT, TransactionCategory.FOOD),
            Transaction(0, "2026-09-07", "Artisan Coffee Co", "Cold Brew & Croissant", 3900.0, "NGN", TransactionType.DEBIT, TransactionCategory.FOOD),
            Transaction(0, "2026-09-09", "Airtel Data", "Mobile Data Topup", 4000.0, "NGN", TransactionType.DEBIT, TransactionCategory.UTILITIES),
            Transaction(0, "2026-09-11", "Medplus Pharmacy", "Vitamins & Lozenges", 4500.0, "NGN", TransactionType.DEBIT, TransactionCategory.HEALTHCARE),
            Transaction(0, "2026-09-12", "Cinema Concessions", "Popcorn & Soda", 4900.0, "NGN", TransactionType.DEBIT, TransactionCategory.ENTERTAINMENT),

            // Debt Repayments
            Transaction(0, "2026-09-04", "Carbon Loans", "Monthly Loan Installment Debited", 45000.0, "NGN", TransactionType.DEBIT, TransactionCategory.DEBT, isRecurring = true),
            Transaction(0, "2026-09-06", "CredPal Finance", "BNPL Gadget Installment Debited", 35000.0, "NGN", TransactionType.DEBIT, TransactionCategory.DEBT, isRecurring = true)
        )
        transactionDao.insertAll(demoTransactions.map { TransactionEntity.fromDomain(it) })

        // 2. Assets (₦8,450,000 baseline as requested in vision)
        val demoAssets = listOf(
            Asset(0, "GTBank Main Account", AssetType.BANK_BALANCE, 1250000.0, "NGN", false, "Guaranty Trust Bank"),
            Asset(0, "Kuda High-Yield Pocket", AssetType.SAVINGS_ACCOUNT, 450000.0, "NGN", false, "Kuda MFB"),
            Asset(0, "FGN 364-Day Treasury Bills", AssetType.TREASURY_BILLS, 3500000.0, "NGN", false, "Central Bank of Nigeria / Cowrywise"),
            Asset(0, "Cowrywise Balanced Mutual Fund", AssetType.MUTUAL_FUNDS, 1850000.0, "NGN", false, "United Capital Asset Mgt"),
            Asset(0, "US Tech ETF (Bamboo)", AssetType.STOCKS, 1400000.0, "NGN", false, "Bamboo Global"),
            Asset(0, "Potential Land Title Plot", AssetType.REAL_ESTATE, 4500000.0, "NGN", isPotential = true, institution = "Ibeju-Lekki Allocation", notes = "Deed of assignment detected in transaction history — confirmation required")
        )
        assetDao.insertAll(demoAssets.map { AssetEntity.fromDomain(it) })

        // 3. Liabilities (₦2,100,000 baseline as requested in vision)
        val demoLiabilities = listOf(
            Liability(0, "Carbon Personal Loan", LiabilityType.PERSONAL_LOAN, 420000.0, 45000.0, 18.0, "NGN", "Carbon Nigeria", "2026-10-04"),
            Liability(0, "CredPal BNPL Finance", LiabilityType.BNPL, 180000.0, 35000.0, 22.0, "NGN", "CredPal", "2026-10-06"),
            Liability(0, "Vehicle Auto Financing", LiabilityType.VEHICLE_FINANCING, 1500000.0, 65000.0, 14.5, "NGN", "Stanbic Auto Loan", "2026-10-15")
        )
        liabilityDao.insertAll(demoLiabilities.map { LiabilityEntity.fromDomain(it) })

        // 4. Savings Goals
        val demoGoals = listOf(
            SavingsGoal(0, "Emergency Fund (6 Months)", GoalCategory.EMERGENCY_FUND, 2000000.0, 650000.0, 12, "2027-09-01"),
            SavingsGoal(0, "Annual Apartment Rent", GoalCategory.RENT, 1800000.0, 750000.0, 8, "2027-05-01"),
            SavingsGoal(0, "Vehicle Upgrade", GoalCategory.CAR, 3500000.0, 900000.0, 18, "2028-03-01")
        )
        goalDao.insertAll(demoGoals.map { SavingsGoalEntity.fromDomain(it) })
    }
}
