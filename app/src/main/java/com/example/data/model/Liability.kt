package com.example.data.model

import java.text.NumberFormat
import java.util.Locale

data class Liability(
    val id: Long = 0,
    val name: String,
    val type: LiabilityType,
    val outstandingAmount: Double,
    val monthlyPayment: Double,
    val interestRate: Double? = null, // e.g. 18.5%
    val currency: String = "NGN",
    val lender: String = "",
    val dueDate: String = ""
) {
    fun formattedOutstanding(): String {
        val symbol = if (currency == "NGN") "₦" else "$"
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
        return "$symbol${formatter.format(outstandingAmount)}"
    }

    fun formattedMonthlyPayment(): String {
        val symbol = if (currency == "NGN") "₦" else "$"
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
        return "$symbol${formatter.format(monthlyPayment)}/mo"
    }
}
