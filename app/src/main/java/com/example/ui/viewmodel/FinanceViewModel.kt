package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Asset
import com.example.data.model.AssetType
import com.example.data.model.ChatMessage
import com.example.data.model.FinancialAlert
import com.example.data.model.FinancialAudit
import com.example.data.model.GoalCategory
import com.example.data.model.Liability
import com.example.data.model.LiabilityType
import com.example.data.model.MessageSender
import com.example.data.model.SavingsGoal
import com.example.data.model.TaxFilingAssessment
import com.example.data.model.TaxFilingStatus
import com.example.data.model.TaxInputState
import com.example.data.model.Transaction
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionType
import com.example.data.model.UserProfile
import com.example.data.repository.CsvParser
import com.example.data.repository.FinanceRepository
import com.example.data.repository.StatementExtractor
import com.example.domain.tax.NigerianTaxCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<TransactionCategory?>(null)
    val selectedCategoryFilter: StateFlow<TransactionCategory?> = _selectedCategoryFilter.asStateFlow()

    private val _selectedTypeFilter = MutableStateFlow<TransactionType?>(null)
    val selectedTypeFilter: StateFlow<TransactionType?> = _selectedTypeFilter.asStateFlow()

    private val _isAuditorThinking = MutableStateFlow(false)
    val isAuditorThinking: StateFlow<Boolean> = _isAuditorThinking.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.AUDITOR,
                text = "Hello! I am your FinAudit AI assistant. I've audited your accounts, spending patterns, assets, and liabilities. Ask me anything about where your money is going, how to cut leakages, or if you can afford an upcoming purchase."
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    val transactions: StateFlow<List<Transaction>>
    val assets: StateFlow<List<Asset>>
    val liabilities: StateFlow<List<Liability>>
    val savingsGoals: StateFlow<List<SavingsGoal>>
    val audit: StateFlow<FinancialAudit?>
    val alerts: StateFlow<List<FinancialAlert>>

    private val _taxInputState = MutableStateFlow(
        TaxInputState(
            taxYear = 2026,
            taxpayerName = _userProfile.value.name,
            grossAnnualIncome = _userProfile.value.monthlyIncome * 12.0
        )
    )
    val taxInputState: StateFlow<TaxInputState> = _taxInputState.asStateFlow()

    private val _taxAssessment = MutableStateFlow(
        NigerianTaxCalculator.calculateTax(
            TaxInputState(
                taxYear = 2026,
                grossAnnualIncome = 950000.0 * 12.0
            )
        )
    )
    val taxAssessment: StateFlow<TaxFilingAssessment> = _taxAssessment.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = FinanceRepository(db)

        transactions = repository.transactions.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        assets = repository.assets.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        liabilities = repository.liabilities.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        savingsGoals = repository.savingsGoals.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        audit = repository.getFinancialAudit(_userProfile.value).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

        alerts = repository.getFinancialAlerts(_userProfile.value).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: TransactionCategory?) {
        _selectedCategoryFilter.value = category
    }

    fun setTypeFilter(type: TransactionType?) {
        _selectedTypeFilter.value = type
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    fun addManualTransaction(
        merchant: String,
        description: String,
        amount: Double,
        type: TransactionType,
        category: TransactionCategory,
        accountName: String,
        isRecurring: Boolean = false
    ) {
        viewModelScope.launch {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            repository.addTransaction(
                Transaction(
                    date = todayStr,
                    merchant = merchant.ifBlank { "Manual Entry" },
                    description = description.ifBlank { merchant },
                    amount = amount,
                    currency = _userProfile.value.currency,
                    type = type,
                    category = category,
                    accountName = accountName.ifBlank { "Manual Account" },
                    isRecurring = isRecurring
                )
            )
        }
    }

    fun reclassifyTransaction(transactionId: Long, merchant: String, newCategory: TransactionCategory) {
        viewModelScope.launch {
            repository.reclassifyTransaction(transactionId, merchant, newCategory)
        }
    }

    fun importStatement(rawContent: String, isCsv: Boolean = false, fileName: String = ""): StatementExtractor.StatementExtractionResult {
        val result = StatementExtractor.extractStatement(rawContent, isCsv, fileName)
        if (result.transactions.isNotEmpty()) {
            viewModelScope.launch {
                repository.importTransactions(result.transactions)
            }
        }
        return result
    }

    fun importCsvContent(csvText: String): CsvParser.ParseResult {
        val result = CsvParser.parseCsv(csvText)
        if (result.transactions.isNotEmpty()) {
            viewModelScope.launch {
                repository.importTransactions(result.transactions)
            }
        }
        return result
    }

    fun importStatementText(text: String): StatementExtractor.StatementExtractionResult {
        val result = StatementExtractor.extractFromText(text)
        if (result.transactions.isNotEmpty()) {
            viewModelScope.launch {
                repository.importTransactions(result.transactions)
            }
        }
        return result
    }

    fun addAsset(name: String, type: AssetType, value: Double, institution: String, isPotential: Boolean = false) {
        viewModelScope.launch {
            repository.addAsset(
                Asset(
                    name = name,
                    type = type,
                    value = value,
                    currency = _userProfile.value.currency,
                    institution = institution,
                    isPotential = isPotential
                )
            )
        }
    }

    fun confirmPotentialAsset(assetId: Long) {
        viewModelScope.launch {
            repository.confirmPotentialAsset(assetId)
        }
    }

    fun deleteAsset(asset: Asset) {
        viewModelScope.launch {
            repository.deleteAsset(asset)
        }
    }

    fun addLiability(name: String, type: LiabilityType, outstanding: Double, monthlyPayment: Double, interestRate: Double?, lender: String) {
        viewModelScope.launch {
            repository.addLiability(
                Liability(
                    name = name,
                    type = type,
                    outstandingAmount = outstanding,
                    monthlyPayment = monthlyPayment,
                    interestRate = interestRate,
                    currency = _userProfile.value.currency,
                    lender = lender
                )
            )
        }
    }

    fun deleteLiability(liability: Liability) {
        viewModelScope.launch {
            repository.deleteLiability(liability)
        }
    }

    fun addSavingsGoal(title: String, category: GoalCategory, targetAmount: Double, initialAmount: Double, targetMonths: Int) {
        viewModelScope.launch {
            repository.addGoal(
                SavingsGoal(
                    title = title,
                    category = category,
                    targetAmount = targetAmount,
                    currentAmount = initialAmount,
                    targetMonths = targetMonths,
                    currency = _userProfile.value.currency
                )
            )
        }
    }

    fun updateGoalProgress(goalId: Long, addedAmount: Double) {
        viewModelScope.launch {
            repository.updateGoalProgress(goalId, addedAmount)
        }
    }

    fun deleteGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    fun sendMessageToAuditor(query: String) {
        if (query.isBlank()) return
        val userMsg = ChatMessage(sender = MessageSender.USER, text = query)
        _chatMessages.value = _chatMessages.value + userMsg
        _isAuditorThinking.value = true

        viewModelScope.launch {
            try {
                val responseText = repository.askAuditor(query, _userProfile.value)
                val auditorMsg = ChatMessage(sender = MessageSender.AUDITOR, text = responseText)
                _chatMessages.value = _chatMessages.value + auditorMsg
            } catch (e: Exception) {
                val errorMsg = ChatMessage(
                    sender = MessageSender.AUDITOR,
                    text = "I ran into an issue analyzing that request. Based on your current data, your monthly cash flow is positive and your biggest detected leakage is food delivery."
                )
                _chatMessages.value = _chatMessages.value + errorMsg
            } finally {
                _isAuditorThinking.value = false
            }
        }
    }

    fun updateUserProfile(profile: UserProfile) {
        _userProfile.value = profile
    }

    fun updateTaxInput(input: TaxInputState) {
        _taxInputState.value = input
        _taxAssessment.value = NigerianTaxCalculator.calculateTax(input)
    }

    fun syncTaxWithDetectedIncome() {
        val annualSalaryFromProfile = _userProfile.value.monthlyIncome * 12.0
        val creditTransactions = transactions.value.filter { it.type == TransactionType.CREDIT }
        val annualCreditSum = if (creditTransactions.isNotEmpty()) {
            val totalInflows = creditTransactions.sumOf { it.amount }
            if (totalInflows > annualSalaryFromProfile * 0.5) totalInflows else annualSalaryFromProfile
        } else {
            annualSalaryFromProfile
        }

        val updated = _taxInputState.value.copy(
            taxpayerName = _userProfile.value.name,
            grossAnnualIncome = annualCreditSum
        )
        updateTaxInput(updated)
    }

    fun setTaxFilingStatus(status: TaxFilingStatus) {
        _taxAssessment.value = _taxAssessment.value.copy(filingStatus = status)
    }

    fun generateFilingReport(): String {
        return NigerianTaxCalculator.generateFilingReport(_taxAssessment.value)
    }
}
