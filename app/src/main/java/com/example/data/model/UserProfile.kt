package com.example.data.model

data class UserProfile(
    val id: String = "default_user",
    val name: String = "Ayodele Elebute",
    val country: String = "Nigeria",
    val currency: String = "NGN",
    val currencySymbol: String = "₦",
    val monthlyIncome: Double = 950000.0,
    val employmentType: String = "Senior Product Consultant",
    val financialGoalDescription: String = "Build 6-month emergency fund and invest in high-yield assets",
    val riskTolerance: RiskTolerance = RiskTolerance.MODERATE,
    val isDemoMode: Boolean = true,
    val isOnboarded: Boolean = true
)
