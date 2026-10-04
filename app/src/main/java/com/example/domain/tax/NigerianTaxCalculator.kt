package com.example.domain.tax

import com.example.data.model.TaxBracketBreakdown
import com.example.data.model.TaxFilingAssessment
import com.example.data.model.TaxFilingStatus
import com.example.data.model.TaxInputState
import com.example.data.model.TaxSavingOpportunity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Deterministic calculation engine for Nigerian Personal Income Tax (PITA / PAYE).
 * Conforms to Personal Income Tax Act (PITA Cap P8 LFN 2004 as amended by Finance Acts).
 */
object NigerianTaxCalculator {

    // Graduated Tax Tiers under PITA Sixth Schedule
    private val TAX_TIERS = listOf(
        TierDefinition(1, "First ₦300,000", 0.07, 300_000.0),
        TierDefinition(2, "Next ₦300,000", 0.11, 300_000.0),
        TierDefinition(3, "Next ₦500,000", 0.15, 500_000.0),
        TierDefinition(4, "Next ₦500,000", 0.19, 500_000.0),
        TierDefinition(5, "Next ₦1,600,000", 0.21, 1_600_000.0),
        TierDefinition(6, "Above ₦3,200,000 (Remaining)", 0.24, Double.MAX_VALUE)
    )

    private data class TierDefinition(
        val tierNumber: Int,
        val label: String,
        val rate: Double,
        val capacity: Double
    )

    /**
     * Calculates complete tax assessment from user inputs.
     */
    fun calculateTax(input: TaxInputState): TaxFilingAssessment {
        val grossIncome = if (input.isBreakdownEmoluments) {
            input.basicSalary + input.housingAllowance + input.transportAllowance + input.otherAllowances + input.otherAnnualIncome
        } else {
            max(0.0, input.grossAnnualIncome + input.otherAnnualIncome)
        }

        // 1. Consolidated Relief Allowance (CRA)
        // PITA Section 33(1): Higher of ₦200,000 or 1% of Gross, PLUS 20% of Gross
        val higherOfBaseOrOnePercent = max(200_000.0, grossIncome * 0.01)
        val twentyPercentOfGross = grossIncome * 0.20
        val craAmount = if (grossIncome > 0) higherOfBaseOrOnePercent + twentyPercentOfGross else 0.0

        // 2. Pension Contribution (PRA 2014)
        // Default 8% of emoluments, or custom amount
        val pensionAmount = when {
            !input.isPensionEnabled -> 0.0
            input.pensionCustomAmount != null && input.pensionCustomAmount > 0 -> input.pensionCustomAmount
            input.isBreakdownEmoluments -> (input.basicSalary + input.housingAllowance + input.transportAllowance) * 0.08
            else -> grossIncome * 0.08
        }

        // 3. National Housing Fund (NHF)
        // 2.5% of basic salary (or 2.5% of ~50% of gross if not broken down)
        val basicForNhf = if (input.isBreakdownEmoluments) input.basicSalary else grossIncome * 0.50
        val nhfAmount = if (input.isNhfEnabled) basicForNhf * 0.025 else 0.0

        // 4. NHIS & Health Insurance
        val nhisAmount = max(0.0, input.nhisAnnualPremium)

        // 5. Life Assurance Premium
        val lifeAssuranceAmount = max(0.0, input.lifeAssurancePremium)

        // 6. Mortgage Interest Relief
        val mortgageAmount = max(0.0, input.mortgageInterest)

        // Total Statutory Reliefs
        val totalReliefs = craAmount + pensionAmount + nhfAmount + nhisAmount + lifeAssuranceAmount + mortgageAmount

        // 7. Chargeable / Taxable Income
        val chargeableIncome = max(0.0, grossIncome - totalReliefs)

        // 8. Calculate Graduated Tax by Tier
        var remainingChargeable = chargeableIncome
        val bracketBreakdowns = mutableListOf<TaxBracketBreakdown>()
        var calculatedGraduatedTax = 0.0
        var marginalRate = 0.0

        for (tier in TAX_TIERS) {
            if (remainingChargeable <= 0.0) {
                bracketBreakdowns.add(
                    TaxBracketBreakdown(
                        tierNumber = tier.tierNumber,
                        tierLabel = tier.label,
                        ratePercent = tier.rate * 100,
                        bracketCapacity = tier.capacity,
                        taxableAmountInBracket = 0.0,
                        taxChargedInBracket = 0.0
                    )
                )
            } else {
                val taxableInThisTier = if (tier.capacity == Double.MAX_VALUE) {
                    remainingChargeable
                } else {
                    remainingChargeable.coerceAtMost(tier.capacity)
                }

                val taxInThisTier = taxableInThisTier * tier.rate
                calculatedGraduatedTax += taxInThisTier
                marginalRate = tier.rate * 100
                remainingChargeable -= taxableInThisTier

                bracketBreakdowns.add(
                    TaxBracketBreakdown(
                        tierNumber = tier.tierNumber,
                        tierLabel = tier.label,
                        ratePercent = tier.rate * 100,
                        bracketCapacity = tier.capacity,
                        taxableAmountInBracket = taxableInThisTier,
                        taxChargedInBracket = taxInThisTier
                    )
                )
            }
        }

        // 9. Minimum Tax Rule: Minimum 1% of Gross Income if calculated graduated tax is lower
        val minimumTax = if (grossIncome > 30_000.0) grossIncome * 0.01 else 0.0
        val isMinimumTaxApplied = (calculatedGraduatedTax < minimumTax) && grossIncome > 30_000.0
        val finalAnnualTax = if (isMinimumTaxApplied) minimumTax else calculatedGraduatedTax

        val monthlyPaye = finalAnnualTax / 12.0
        val effectiveRate = if (grossIncome > 0) (finalAnnualTax / grossIncome) * 100.0 else 0.0

        // 10. Net Tax Due (accounting for WHT or PAYE already deducted)
        val taxesAlreadyPaid = max(0.0, input.taxesAlreadyPaidOrWht)
        val netTaxDue = finalAnnualTax - taxesAlreadyPaid

        // 11. Optimization Opportunities
        val savingTips = generateSavingTips(grossIncome, input, pensionAmount, nhisAmount, lifeAssuranceAmount)

        // Reference number
        val refCode = "NG-TAX-${input.taxYear}-${(10000..99999).random()}"

        return TaxFilingAssessment(
            taxYear = input.taxYear,
            stateRevenueBoard = input.stateRevenueBoard,
            taxpayerName = input.taxpayerName,
            taxpayerTin = input.taxpayerTin,
            employmentType = if (input.isBreakdownEmoluments) "Full Breakdown (Base + Allowances)" else "Standard Gross Income",
            grossAnnualIncome = roundTwoDecimals(grossIncome),
            basicSalary = roundTwoDecimals(if (input.isBreakdownEmoluments) input.basicSalary else grossIncome * 0.50),
            housingAllowance = roundTwoDecimals(if (input.isBreakdownEmoluments) input.housingAllowance else grossIncome * 0.30),
            transportAllowance = roundTwoDecimals(if (input.isBreakdownEmoluments) input.transportAllowance else grossIncome * 0.10),
            otherAllowances = roundTwoDecimals(if (input.isBreakdownEmoluments) input.otherAllowances else grossIncome * 0.10),
            annualBonusAndCommission = 0.0,
            otherIncome = roundTwoDecimals(input.otherAnnualIncome),
            isPensionEnabled = input.isPensionEnabled,
            pensionRatePercent = 8.0,
            pensionContribution = roundTwoDecimals(pensionAmount),
            isNhfEnabled = input.isNhfEnabled,
            nhfContribution = roundTwoDecimals(nhfAmount),
            nhisContribution = roundTwoDecimals(nhisAmount),
            lifeAssurancePremium = roundTwoDecimals(lifeAssuranceAmount),
            mortgageInterestRelief = roundTwoDecimals(mortgageAmount),
            craAmount = roundTwoDecimals(craAmount),
            totalStatutoryReliefs = roundTwoDecimals(totalReliefs),
            chargeableTaxableIncome = roundTwoDecimals(chargeableIncome),
            totalAnnualTaxPayable = roundTwoDecimals(finalAnnualTax),
            monthlyPayeTax = roundTwoDecimals(monthlyPaye),
            effectiveTaxRate = roundOneDecimal(effectiveRate),
            marginalTaxRate = marginalRate,
            isMinimumTaxApplied = isMinimumTaxApplied,
            taxesAlreadyDeducted = roundTwoDecimals(taxesAlreadyPaid),
            netTaxDue = roundTwoDecimals(netTaxDue),
            bracketBreakdowns = bracketBreakdowns,
            savingTips = savingTips,
            filingStatus = TaxFilingStatus.ASSESSED,
            filingReference = refCode,
            lastCalculatedTimestamp = System.currentTimeMillis()
        )
    }

    private fun generateSavingTips(
        grossIncome: Double,
        input: TaxInputState,
        currentPension: Double,
        currentNhis: Double,
        currentLifeAssurance: Double
    ): List<TaxSavingOpportunity> {
        val tips = mutableListOf<TaxSavingOpportunity>()

        // Voluntary Pension tip
        val maxVoluntaryPension = grossIncome * 0.20 // up to 20% voluntary
        if (currentPension < maxVoluntaryPension) {
            val potentialAdditional = (maxVoluntaryPension - currentPension).coerceAtMost(1_000_000.0)
            val potentialSavings = potentialAdditional * 0.24 // taxed at top bracket
            tips.add(
                TaxSavingOpportunity(
                    title = "Additional Voluntary Pension Contribution (AVC)",
                    explanation = "Under Pension Reform Act (PRA 2014), voluntary pension contributions are 100% tax-exempt. Contributing an extra ₦${formatCompact(potentialAdditional)} could shield it from the 24% marginal tax tier.",
                    potentialTaxSaved = potentialSavings,
                    actionTag = "PENSION_AVC"
                )
            )
        }

        // Life Assurance Relief tip
        if (currentLifeAssurance == 0.0) {
            val samplePremium = 300_000.0
            val potentialSavings = samplePremium * 0.21
            tips.add(
                TaxSavingOpportunity(
                    title = "Claim Life Assurance Premium Relief",
                    explanation = "PITA Section 33(4)(d) allows 100% tax deductions on annual life assurance premiums paid on your life or your spouse's life with licensed Nigerian insurers.",
                    potentialTaxSaved = potentialSavings,
                    actionTag = "LIFE_INSURANCE"
                )
            )
        }

        // NHIS / Health Insurance tip
        if (currentNhis < 150_000.0) {
            tips.add(
                TaxSavingOpportunity(
                    title = "National Health Insurance (NHIS / HMO) Deductions",
                    explanation = "Annual health insurance premiums paid to registered HMOs and the NHIS are fully tax deductible under Nigerian law. Keep your payment receipts for annual filing verification.",
                    potentialTaxSaved = 40_000.0,
                    actionTag = "NHIS_RELIEF"
                )
            )
        }

        // WHT Credit tip
        if (input.otherAnnualIncome > 0 && input.taxesAlreadyPaidOrWht == 0.0) {
            tips.add(
                TaxSavingOpportunity(
                    title = "Offset Withholding Tax (WHT) Credit Notes",
                    explanation = "If clients or dividend-paying companies deducted 5% or 10% WHT from your consulting fees or investments, request credit notes to offset directly against your annual tax due.",
                    potentialTaxSaved = input.otherAnnualIncome * 0.05,
                    actionTag = "WHT_CREDIT"
                )
            )
        }

        return tips
    }

    /**
     * Generates standard Form A / Electronic Filing Return Summary string suitable for
     * submission to State Internal Revenue Services (e.g., LIRS e-Tax, FCT-IRS, etc.).
     */
    fun generateFilingReport(assessment: TaxFilingAssessment): String {
        val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale.US).format(Date(assessment.lastCalculatedTimestamp))
        val sb = StringBuilder()

        sb.appendLine("================================================================")
        sb.appendLine("     ANNUAL PERSONAL INCOME TAX RETURN (FORM A ASSESSMENT)      ")
        sb.appendLine("           CONFORMING TO PITA CAP P8 LFN 2004 (AS AMENDED)      ")
        sb.appendLine("================================================================")
        sb.appendLine("Tax Year:                  ${assessment.taxYear}")
        sb.appendLine("Assessment Reference:      ${assessment.filingReference}")
        sb.appendLine("Date Prepared:             $dateStr")
        sb.appendLine("Revenue Authority:         ${assessment.stateRevenueBoard}")
        sb.appendLine("Taxpayer Name:             ${assessment.taxpayerName}")
        sb.appendLine("Taxpayer TIN:              ${assessment.taxpayerTin}")
        sb.appendLine("----------------------------------------------------------------")
        sb.appendLine("1. GROSS ANNUAL INCOME & EMOLUMENTS")
        sb.appendLine("   - Gross Annual Earnings:            ₦${formatNumber(assessment.grossAnnualIncome)}")
        if (assessment.otherIncome > 0) {
            sb.appendLine("   - Other / Consulting Income:        ₦${formatNumber(assessment.otherIncome)}")
        }
        sb.appendLine("----------------------------------------------------------------")
        sb.appendLine("2. STATUTORY RELIEFS & ALLOWANCES (PITA SEC. 33)")
        sb.appendLine("   - Consolidated Relief Allowance:    ₦${formatNumber(assessment.craAmount)}")
        if (assessment.pensionContribution > 0) {
            sb.appendLine("   - Pension Contribution (PRA 2014):  ₦${formatNumber(assessment.pensionContribution)}")
        }
        if (assessment.nhfContribution > 0) {
            sb.appendLine("   - National Housing Fund (NHF):      ₦${formatNumber(assessment.nhfContribution)}")
        }
        if (assessment.nhisContribution > 0) {
            sb.appendLine("   - Health Insurance (NHIS):          ₦${formatNumber(assessment.nhisContribution)}")
        }
        if (assessment.lifeAssurancePremium > 0) {
            sb.appendLine("   - Life Assurance Relief:            ₦${formatNumber(assessment.lifeAssurancePremium)}")
        }
        if (assessment.mortgageInterestRelief > 0) {
            sb.appendLine("   - Mortgage Interest Relief:         ₦${formatNumber(assessment.mortgageInterestRelief)}")
        }
        sb.appendLine("   TOTAL STATUTORY RELIEFS:            ₦${formatNumber(assessment.totalStatutoryReliefs)}")
        sb.appendLine("----------------------------------------------------------------")
        sb.appendLine("3. TAXABLE / CHARGEABLE INCOME")
        sb.appendLine("   - Net Chargeable Income:            ₦${formatNumber(assessment.chargeableTaxableIncome)}")
        sb.appendLine("----------------------------------------------------------------")
        sb.appendLine("4. PITA GRADUATED TAX COMPUTATION (SIXTH SCHEDULE)")
        for (tier in assessment.bracketBreakdowns) {
            if (tier.taxableAmountInBracket > 0) {
                sb.appendLine("   - ${tier.tierLabel.padEnd(28)} @ ${tier.ratePercent.toInt()}% = ₦${formatNumber(tier.taxChargedInBracket)}")
            }
        }
        if (assessment.isMinimumTaxApplied) {
            sb.appendLine("   * Note: Minimum Tax Rule applied (1% of Gross Annual Income)")
        }
        sb.appendLine("----------------------------------------------------------------")
        sb.appendLine("5. FINAL TAX SUMMARY")
        sb.appendLine("   TOTAL ANNUAL TAX PAYABLE:           ₦${formatNumber(assessment.totalAnnualTaxPayable)}")
        sb.appendLine("   MONTHLY PAYE EQUIVALENT:            ₦${formatNumber(assessment.monthlyPayeTax)}")
        sb.appendLine("   EFFECTIVE TAX RATE:                 ${assessment.effectiveTaxRate}%")
        sb.appendLine("   MARGINAL TAX RATE:                  ${assessment.marginalTaxRate.toInt()}%")
        if (assessment.taxesAlreadyDeducted > 0) {
            sb.appendLine("   Advance Taxes / WHT Credits Paid:   ₦${formatNumber(assessment.taxesAlreadyDeducted)}")
            sb.appendLine("   NET TAX DUE / BALANCE REMAINING:    ₦${formatNumber(assessment.netTaxDue)}")
        }
        sb.appendLine("================================================================")
        sb.appendLine("Certified prepared by FinAudit AI Personal Tax Assessment Engine.")
        sb.appendLine("Please upload or present this schedule to your State Internal Revenue Service.")
        return sb.toString()
    }

    private fun formatNumber(value: Double): String {
        return String.format(Locale.US, "%,.2f", value)
    }

    private fun formatCompact(value: Double): String {
        return when {
            value >= 1_000_000 -> String.format(Locale.US, "%.1fM", value / 1_000_000)
            value >= 1_000 -> String.format(Locale.US, "%.0fK", value / 1_000)
            else -> String.format(Locale.US, "%.0f", value)
        }
    }

    private fun roundTwoDecimals(value: Double): Double {
        return (value * 100.0).roundToInt() / 100.0
    }

    private fun roundOneDecimal(value: Double): Double {
        return (value * 10.0).roundToInt() / 10.0
    }
}
