package com.example.data.model

import java.text.NumberFormat
import java.util.Locale

data class SavingsGoal(
    val id: Long = 0,
    val title: String,
    val category: GoalCategory = GoalCategory.EMERGENCY_FUND,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetMonths: Int = 12,
    val targetDate: String = "",
    val currency: String = "NGN"
) {
    val remainingAmount: Double get() = (targetAmount - currentAmount).coerceAtLeast(0.0)
    val progressPercentage: Int get() = if (targetAmount > 0) ((currentAmount / targetAmount) * 100).toInt().coerceIn(0, 100) else 0

    val recommendedMonthlySaving: Double get() = if (targetMonths > 0) remainingAmount / targetMonths else remainingAmount
    val recommendedWeeklySaving: Double get() = recommendedMonthlySaving / 4.0

    fun formattedTarget(): String = formatMoney(targetAmount, currency)
    fun formattedCurrent(): String = formatMoney(currentAmount, currency)
    fun formattedRemaining(): String = formatMoney(remainingAmount, currency)
    fun formattedMonthly(): String = formatMoney(recommendedMonthlySaving, currency)
    fun formattedWeekly(): String = formatMoney(recommendedWeeklySaving, currency)

    private fun formatMoney(amount: Double, curr: String): String {
        val symbol = if (curr == "NGN") "₦" else "$"
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
        return "$symbol${formatter.format(amount)}"
    }
}
