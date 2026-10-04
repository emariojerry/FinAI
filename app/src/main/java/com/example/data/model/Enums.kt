package com.example.data.model

enum class TransactionType {
    DEBIT,
    CREDIT
}

enum class TransactionCategory(val displayName: String, val isEssential: Boolean) {
    HOUSING("Housing", true),
    FOOD("Food & Dining", true),
    TRANSPORTATION("Transportation", true),
    UTILITIES("Utilities", true),
    HEALTHCARE("Healthcare", true),
    EDUCATION("Education", true),
    SHOPPING("Shopping", false),
    ENTERTAINMENT("Entertainment", false),
    SUBSCRIPTIONS("Subscriptions", false),
    FAMILY("Family & Dependents", true),
    DEBT("Debt Repayment", true),
    BANK_FEES("Bank & Transfer Fees", false),
    INVESTMENT("Investments", false),
    SAVINGS("Savings", false),
    TRANSFERS("Transfers", false),
    BUSINESS("Business", true),
    OTHER("Other", false);

    companion object {
        fun fromString(name: String?): TransactionCategory {
            if (name == null) return OTHER
            return entries.firstOrNull { 
                it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true)
            } ?: OTHER
        }
    }
}

enum class AssetType(val displayName: String, val isLiquid: Boolean) {
    CASH("Cash in Hand", true),
    BANK_BALANCE("Bank Account Balance", true),
    SAVINGS_ACCOUNT("High-Yield Savings", true),
    TREASURY_BILLS("Treasury Bills", true),
    MUTUAL_FUNDS("Mutual Funds", true),
    STOCKS("Stocks & Equities", true),
    REAL_ESTATE("Real Estate & Property", false),
    VEHICLE("Vehicles", false),
    CRYPTO("Cryptocurrency", true),
    BUSINESS("Business Ownership", false),
    OTHER("Other Asset", false)
}

enum class LiabilityType(val displayName: String) {
    PERSONAL_LOAN("Personal Loan"),
    BANK_LOAN("Commercial Bank Loan"),
    CREDIT_CARD("Credit Card Balance"),
    BNPL("Buy Now Pay Later (BNPL)"),
    MORTGAGE("Home Mortgage"),
    VEHICLE_FINANCING("Vehicle Auto Finance"),
    BUSINESS_DEBT("Business Debt"),
    INDIVIDUAL_DEBT("Owed to Individual"),
    OTHER("Other Debt")
}

enum class LeakageSeverity {
    HIGH,
    MEDIUM,
    LOW
}

enum class AlertType {
    SPENDING,
    SUBSCRIPTION,
    DEBT,
    LIFESTYLE,
    SAVINGS_OPPORTUNITY
}

enum class GoalCategory(val displayName: String) {
    EMERGENCY_FUND("Emergency Fund"),
    RENT("Annual Rent"),
    CAR("Car / Vehicle"),
    HOUSE("House Purchase / Land"),
    SCHOOL_FEES("School Fees"),
    VACATION("Vacation & Travel"),
    BUSINESS("Business Startup / Capital"),
    WEDDING("Wedding"),
    EQUIPMENT("Work Equipment"),
    OTHER("Other Goal")
}

enum class RiskTolerance {
    CONSERVATIVE,
    MODERATE,
    AGGRESSIVE
}
