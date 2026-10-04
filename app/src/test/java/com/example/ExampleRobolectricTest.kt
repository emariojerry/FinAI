package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Asset
import com.example.data.model.AssetType
import com.example.data.model.Liability
import com.example.data.model.LiabilityType
import com.example.data.model.Transaction
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionType
import com.example.data.model.UserProfile
import com.example.data.repository.CsvParser
import com.example.domain.engine.FinancialAuditEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("FinAudit AI", appName)
  }

  @Test
  fun `test financial audit deterministic calculations`() {
    val user = UserProfile(monthlyIncome = 1000000.0)
    val assets = listOf(
      Asset(1, "Bank Account", AssetType.BANK_BALANCE, 2000000.0),
      Asset(2, "Mutual Fund", AssetType.MUTUAL_FUNDS, 3000000.0)
    )
    val liabilities = listOf(
      Liability(1, "Car Loan", LiabilityType.VEHICLE_FINANCING, 1000000.0, 50000.0)
    )
    val transactions = listOf(
      Transaction(1, "2026-09-01", "Employer", "Salary", 1000000.0, type = TransactionType.CREDIT),
      Transaction(2, "2026-09-02", "Landlord", "Rent", 200000.0, type = TransactionType.DEBIT, category = TransactionCategory.HOUSING),
      Transaction(3, "2026-09-03", "Chowdeck", "Food Delivery", 160000.0, type = TransactionType.DEBIT, category = TransactionCategory.FOOD)
    )

    val audit = FinancialAuditEngine.performAudit(user, transactions, assets, liabilities)

    // Net worth = 5,000,000 - 1,000,000 = 4,000,000
    assertEquals(4000000.0, audit.netWorth, 0.01)
    assertEquals(1000000.0, audit.totalIncome, 0.01)
    assertEquals(360000.0, audit.totalExpenses, 0.01)
    assertEquals(640000.0, audit.netCashFlow, 0.01)
    assertTrue(audit.healthScore in 10..100)
    assertTrue(audit.moneyLeakages.isNotEmpty())
  }

  @Test
  fun `test csv parser auto detection`() {
    val csv = """
      Date,Description,Merchant,Debit,Credit,Balance
      2026-09-12,TRANSFER TO CHOWDECK,Chowdeck,12500,,485000
      2026-09-14,MONTHLY PAYROLL,Acme,,950000,1435000
    """.trimIndent()

    val result = CsvParser.parseCsv(csv)
    assertEquals(2, result.rowCount)
    assertEquals(12500.0, result.transactions[0].amount, 0.01)
    assertEquals(TransactionType.DEBIT, result.transactions[0].type)
    assertEquals(950000.0, result.transactions[1].amount, 0.01)
    assertEquals(TransactionType.CREDIT, result.transactions[1].type)
  }

  @Test
  fun `test statement extractor with Nigerian bank statement format`() {
    val sampleStatement = """
      GUARANTY TRUST BANK PLC
      ACCOUNT STATEMENT: 0128943892 (AYODELE ELEBUTE)
      PERIOD: 01-SEP-2026 TO 15-SEP-2026
      
      01-09-2026 PAYROLL CREDIT / TECH CORP SALARY 950000.00 CR
      02-09-2026 POS DEBIT / SHOPRITE MALL IKEJA 42000.00 DR
      05-09-2026 ELECTRONIC LEVY CBN STAMP DUTY 50.00 DR
    """.trimIndent()

    val result = com.example.data.repository.StatementExtractor.extractStatement(
      rawContent = sampleStatement,
      isCsv = false,
      fileName = "GTBank_Statement.pdf"
    )

    assertEquals("Guaranty Trust Bank (GTBank)", result.bankName)
    assertEquals(3, result.transactions.size)
    assertEquals(950000.0, result.transactions[0].amount, 0.01)
    assertEquals(TransactionType.CREDIT, result.transactions[0].type)
    assertEquals(42000.0, result.transactions[1].amount, 0.01)
    assertEquals(TransactionType.DEBIT, result.transactions[1].type)
  }

  @Test
  fun `test Nigerian PITA tax calculator exact tier and relief calculations`() {
    val input = com.example.data.model.TaxInputState(
      taxYear = 2026,
      grossAnnualIncome = 12000000.0,
      isPensionEnabled = true,
      isNhfEnabled = false,
      nhisAnnualPremium = 0.0,
      lifeAssurancePremium = 0.0
    )

    val assessment = com.example.domain.tax.NigerianTaxCalculator.calculateTax(input)

    // Gross = 12,000,000
    assertEquals(12000000.0, assessment.grossAnnualIncome, 0.01)
    
    // CRA = Higher of 200,000 or 1%(120,000) + 20%(2,400,000) = 2,600,000
    assertEquals(2600000.0, assessment.craAmount, 0.01)

    // Pension = 8% of 12,000,000 = 960,000
    assertEquals(960000.0, assessment.pensionContribution, 0.01)

    // Total Reliefs = 2,600,000 + 960,000 = 3,560,000
    assertEquals(3560000.0, assessment.totalStatutoryReliefs, 0.01)

    // Chargeable Income = 12,000,000 - 3,560,000 = 8,440,000
    assertEquals(8440000.0, assessment.chargeableTaxableIncome, 0.01)

    // Graduated tiers sum:
    // 300k * 7% = 21,000
    // 300k * 11% = 33,000
    // 500k * 15% = 75,000
    // 500k * 19% = 95,000
    // 1.6m * 21% = 336,000
    // (8.44m - 3.2m) = 5.24m * 24% = 1,257,600
    // Total Annual Tax = 1,817,600
    assertEquals(1817600.0, assessment.totalAnnualTaxPayable, 0.01)
    assertEquals(1817600.0 / 12.0, assessment.monthlyPayeTax, 0.5)
    assertTrue(assessment.bracketBreakdowns.size == 6)

    // Verify report generation contains key statutory tokens
    val report = com.example.domain.tax.NigerianTaxCalculator.generateFilingReport(assessment)
    assertTrue(report.contains("ANNUAL PERSONAL INCOME TAX RETURN"))
    assertTrue(report.contains("₦1,817,600.00"))
    assertTrue(report.contains("PITA CAP P8 LFN 2004"))
  }

  @Test
  fun `test Nigerian tax minimum tax rule for low taxable income`() {
    val input = com.example.data.model.TaxInputState(
      taxYear = 2026,
      grossAnnualIncome = 240000.0, // Below CRA threshold
      isPensionEnabled = false
    )
    val assessment = com.example.domain.tax.NigerianTaxCalculator.calculateTax(input)
    // When graduated tax is below 1% of gross, minimum tax of 1% applies (₦2,400)
    assertTrue(assessment.isMinimumTaxApplied)
    assertEquals(2400.0, assessment.totalAnnualTaxPayable, 0.01)
  }
}

