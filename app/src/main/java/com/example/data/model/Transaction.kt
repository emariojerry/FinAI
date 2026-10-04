package com.example.data.model

import java.text.NumberFormat
import java.util.Locale

data class Transaction(
    val id: Long = 0,
    val date: String, // e.g., "2026-09-12"
    val merchant: String,
    val description: String,
    val amount: Double,
    val currency: String = "NGN",
    val type: TransactionType = TransactionType.DEBIT,
    val category: TransactionCategory = TransactionCategory.OTHER,
    val subcategory: String = "",
    val isRecurring: Boolean = false,
    val isBankFee: Boolean = false,
    val aiConfidence: Float = 0.95f,
    val accountName: String = "Main Checking (GTBank)",
    val notes: String = ""
) {
    val isDebit: Boolean get() = type == TransactionType.DEBIT

    fun formattedAmount(): String {
        val symbol = if (currency == "NGN") "₦" else "$"
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 2
        }
        val prefix = if (isDebit) "-" else "+"
        return "$prefix$symbol${formatter.format(amount)}"
    }
}
