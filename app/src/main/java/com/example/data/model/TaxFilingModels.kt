package com.example.data.model

/**
 * Data structures for Nigerian Personal Income Tax (PITA) assessment and filing.
 */
data class TaxFilingAssessment(
    val taxYear: Int = 2026,
    val stateRevenueBoard: String = "Lagos State Internal Revenue Service (LIRS)",
    val taxpayerName: String = "Ayodele Elebute",
    val taxpayerTin: String = "23891048-0001",
    val employmentType: String = "Salaried & Professional",
    val grossAnnualIncome: Double = 11400000.0, // ₦950,000/mo * 12
    val basicSalary: Double = 5700000.0,
    val housingAllowance: Double = 3420000.0,
    val transportAllowance: Double = 1140000.0,
    val otherAllowances: Double = 1140000.0,
    val annualBonusAndCommission: Double = 0.0,
    val otherIncome: Double = 0.0,
    val isPensionEnabled: Boolean = true,
    val pensionRatePercent: Double = 8.0,
    val pensionContribution: Double = 912000.0, // 8% of emoluments
    val isNhfEnabled: Boolean = true,
    val nhfContribution: Double = 142500.0, // 2.5% of basic
    val nhisContribution: Double = 120000.0,
    val lifeAssurancePremium: Double = 180000.0,
    val mortgageInterestRelief: Double = 0.0,
    val craAmount: Double = 2394000.0, // Consolidated Relief Allowance: max(200k, 1%) + 20%
    val totalStatutoryReliefs: Double = 3748500.0,
    val chargeableTaxableIncome: Double = 7651500.0,
    val totalAnnualTaxPayable: Double = 1687360.0,
    val monthlyPayeTax: Double = 140613.33,
    val effectiveTaxRate: Double = 14.8, // %
    val marginalTaxRate: Double = 24.0, // %
    val isMinimumTaxApplied: Boolean = false,
    val taxesAlreadyDeducted: Double = 0.0, // e.g. WHT or PAYE remitted
    val netTaxDue: Double = 1687360.0,
    val bracketBreakdowns: List<TaxBracketBreakdown> = emptyList(),
    val savingTips: List<TaxSavingOpportunity> = emptyList(),
    val filingStatus: TaxFilingStatus = TaxFilingStatus.ASSESSED,
    val filingReference: String = "NG-TAX-2026-89410",
    val lastCalculatedTimestamp: Long = System.currentTimeMillis()
)

data class TaxBracketBreakdown(
    val tierNumber: Int,
    val tierLabel: String,
    val ratePercent: Double,
    val bracketCapacity: Double,
    val taxableAmountInBracket: Double,
    val taxChargedInBracket: Double
)

data class TaxSavingOpportunity(
    val title: String,
    val explanation: String,
    val potentialTaxSaved: Double,
    val actionTag: String
)

enum class TaxFilingStatus(val label: String) {
    ASSESSED("Assessment Complete"),
    READY_TO_FILE("Ready to File"),
    FILED("Filed with State Revenue"),
    ARCHIVED("Archived")
}

data class TaxInputState(
    val taxYear: Int = 2026,
    val stateRevenueBoard: String = "Lagos (LIRS)",
    val taxpayerName: String = "Ayodele Elebute",
    val taxpayerTin: String = "23891048-0001",
    val grossAnnualIncome: Double = 11400000.0,
    val isBreakdownEmoluments: Boolean = false,
    val basicSalary: Double = 5700000.0,
    val housingAllowance: Double = 3420000.0,
    val transportAllowance: Double = 1140000.0,
    val otherAllowances: Double = 1140000.0,
    val otherAnnualIncome: Double = 0.0,
    val isPensionEnabled: Boolean = true,
    val pensionCustomAmount: Double? = null,
    val isNhfEnabled: Boolean = false,
    val nhisAnnualPremium: Double = 120000.0,
    val lifeAssurancePremium: Double = 0.0,
    val mortgageInterest: Double = 0.0,
    val taxesAlreadyPaidOrWht: Double = 0.0
)
