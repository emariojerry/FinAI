package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Asset
import com.example.data.model.AssetType
import com.example.data.model.GoalCategory
import com.example.data.model.Liability
import com.example.data.model.LiabilityType
import com.example.data.model.SavingsGoal
import com.example.data.model.Transaction
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val merchant: String,
    val description: String,
    val amount: Double,
    val currency: String = "NGN",
    val type: String = "DEBIT", // DEBIT / CREDIT
    val category: String = "OTHER",
    val subcategory: String = "",
    val isRecurring: Boolean = false,
    val isBankFee: Boolean = false,
    val aiConfidence: Float = 0.95f,
    val accountName: String = "Main Checking (GTBank)",
    val notes: String = ""
) {
    fun toDomain(): Transaction = Transaction(
        id = id,
        date = date,
        merchant = merchant,
        description = description,
        amount = amount,
        currency = currency,
        type = if (type == "CREDIT") TransactionType.CREDIT else TransactionType.DEBIT,
        category = TransactionCategory.fromString(category),
        subcategory = subcategory,
        isRecurring = isRecurring,
        isBankFee = isBankFee,
        aiConfidence = aiConfidence,
        accountName = accountName,
        notes = notes
    )

    companion object {
        fun fromDomain(t: Transaction): TransactionEntity = TransactionEntity(
            id = t.id,
            date = t.date,
            merchant = t.merchant,
            description = t.description,
            amount = t.amount,
            currency = t.currency,
            type = t.type.name,
            category = t.category.name,
            subcategory = t.subcategory,
            isRecurring = t.isRecurring,
            isBankFee = t.isBankFee,
            aiConfidence = t.aiConfidence,
            accountName = t.accountName,
            notes = t.notes
        )
    }
}

@Entity(tableName = "assets")
data class AssetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val value: Double,
    val currency: String = "NGN",
    val isPotential: Boolean = false,
    val institution: String = "",
    val notes: String = ""
) {
    fun toDomain(): Asset = Asset(
        id = id,
        name = name,
        type = try { AssetType.valueOf(type) } catch (_: Exception) { AssetType.OTHER },
        value = value,
        currency = currency,
        isPotential = isPotential,
        institution = institution,
        notes = notes
    )

    companion object {
        fun fromDomain(a: Asset): AssetEntity = AssetEntity(
            id = a.id,
            name = a.name,
            type = a.type.name,
            value = a.value,
            currency = a.currency,
            isPotential = a.isPotential,
            institution = a.institution,
            notes = a.notes
        )
    }
}

@Entity(tableName = "liabilities")
data class LiabilityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val outstandingAmount: Double,
    val monthlyPayment: Double,
    val interestRate: Double? = null,
    val currency: String = "NGN",
    val lender: String = "",
    val dueDate: String = ""
) {
    fun toDomain(): Liability = Liability(
        id = id,
        name = name,
        type = try { LiabilityType.valueOf(type) } catch (_: Exception) { LiabilityType.OTHER },
        outstandingAmount = outstandingAmount,
        monthlyPayment = monthlyPayment,
        interestRate = interestRate,
        currency = currency,
        lender = lender,
        dueDate = dueDate
    )

    companion object {
        fun fromDomain(l: Liability): LiabilityEntity = LiabilityEntity(
            id = l.id,
            name = l.name,
            type = l.type.name,
            outstandingAmount = l.outstandingAmount,
            monthlyPayment = l.monthlyPayment,
            interestRate = l.interestRate,
            currency = l.currency,
            lender = l.lender,
            dueDate = l.dueDate
        )
    }
}

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetMonths: Int = 12,
    val targetDate: String = "",
    val currency: String = "NGN"
) {
    fun toDomain(): SavingsGoal = SavingsGoal(
        id = id,
        title = title,
        category = try { GoalCategory.valueOf(category) } catch (_: Exception) { GoalCategory.OTHER },
        targetAmount = targetAmount,
        currentAmount = currentAmount,
        targetMonths = targetMonths,
        targetDate = targetDate,
        currency = currency
    )

    companion object {
        fun fromDomain(g: SavingsGoal): SavingsGoalEntity = SavingsGoalEntity(
            id = g.id,
            title = g.title,
            category = g.category.name,
            targetAmount = g.targetAmount,
            currentAmount = g.currentAmount,
            targetMonths = g.targetMonths,
            targetDate = g.targetDate,
            currency = g.currency
        )
    }
}

@Entity(tableName = "category_rules")
data class CategoryRuleEntity(
    @PrimaryKey val pattern: String, // lowercase keyword or merchant name
    val category: String,
    val confidence: Float = 1.0f
)
