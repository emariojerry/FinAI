package com.example.data.model

data class FinancialAlert(
    val id: String,
    val type: AlertType,
    val title: String,
    val message: String,
    val severity: LeakageSeverity = LeakageSeverity.MEDIUM,
    val actionableTip: String = ""
)
