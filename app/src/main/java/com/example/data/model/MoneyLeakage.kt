package com.example.data.model

import java.text.NumberFormat
import java.util.Locale

data class MoneyLeakage(
    val id: String,
    val title: String, // e.g. "Food delivery & Takeout"
    val monthlyAmount: Double, // e.g. 74500.0
    val percentageOfIncome: Double, // e.g. 8.3
    val severity: LeakageSeverity, // HIGH, MEDIUM, LOW
    val whyItMatters: String,
    val recommendedAction: String,
    val potentialAnnualSavings: Double, // e.g. 894000.0
    val currency: String = "NGN"
) {
    fun formattedMonthly(): String {
        val symbol = if (currency == "NGN") "₦" else "$"
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
        return "$symbol${formatter.format(monthlyAmount)}/mo"
    }

    fun formattedAnnualSavings(): String {
        val symbol = if (currency == "NGN") "₦" else "$"
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
        return "$symbol${formatter.format(potentialAnnualSavings)}/yr"
    }
}
