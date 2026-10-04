package com.example.data.model

import java.text.NumberFormat
import java.util.Locale

data class Asset(
    val id: Long = 0,
    val name: String,
    val type: AssetType,
    val value: Double,
    val currency: String = "NGN",
    val isPotential: Boolean = false, // "Potential Asset — Confirm Required"
    val institution: String = "",
    val notes: String = ""
) {
    val isLiquid: Boolean get() = type.isLiquid

    fun formattedValue(): String {
        val symbol = if (currency == "NGN") "₦" else "$"
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 0
        }
        return "$symbol${formatter.format(value)}"
    }
}
