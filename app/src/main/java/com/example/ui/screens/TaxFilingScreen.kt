package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaxBracketBreakdown
import com.example.data.model.TaxFilingAssessment
import com.example.data.model.TaxFilingStatus
import com.example.data.model.TaxInputState
import com.example.data.model.TaxSavingOpportunity
import com.example.ui.theme.CoralRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateMedium
import java.text.DecimalFormat
import java.util.Locale

private val NIGERIAN_TAX_BOARDS = listOf(
    "Lagos (LIRS)",
    "Abuja FCT (FCT-IRS)",
    "Rivers (RIRS)",
    "Ogun (OGIRS)",
    "Oyo (OYSIRS)",
    "Edo (EIRS)",
    "Kaduna (KADIRS)",
    "Delta (DSIRS)",
    "Kano (KIRS)",
    "Enugu (ESIRS)"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxFilingScreen(
    assessment: TaxFilingAssessment,
    inputState: TaxInputState,
    onUpdateInput: (TaxInputState) -> Unit,
    onSyncWithStatement: () -> Unit,
    onMarkFilingStatus: (TaxFilingStatus) -> Unit,
    onGenerateReport: () -> String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showReportDialog by remember { mutableStateOf(false) }
    var generatedReportText by remember { mutableStateOf("") }
    var showCopyNotification by remember { mutableStateOf(false) }

    val currencyFormatter = remember { DecimalFormat("#,##0.00") }
    val compactFormatter = remember { DecimalFormat("#,##0") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("tax_filing_screen")
    ) {
        // App Bar / Top Header
        Surface(
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Annual Tax Assessment",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavy
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFEFF6FF), RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Tax Year ${assessment.taxYear}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D4ED8)
                                )
                            }
                        }
                        Text(
                            text = "PITA Cap P8 LFN 2004 Compliant Assessment (Nigeria)",
                            fontSize = 12.sp,
                            color = SlateMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row {
                        IconButton(
                            onClick = onSyncWithStatement,
                            modifier = Modifier.testTag("sync_tax_statement_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync with Bank Inflows",
                                tint = PrimaryNavy
                            )
                        }

                        IconButton(
                            onClick = {
                                generatedReportText = onGenerateReport()
                                showReportDialog = true
                            },
                            modifier = Modifier.testTag("generate_tax_report_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Generate e-Tax Return Schedule",
                                tint = PrimaryNavy
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = PrimaryNavy,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = PrimaryNavy,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                text = "Assessment",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        modifier = Modifier.testTag("tax_tab_assessment")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                text = "Calculator & Reliefs",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        modifier = Modifier.testTag("tax_tab_calculator")
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                text = "Tax Optimization",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        modifier = Modifier.testTag("tax_tab_optimization")
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = {
                            Text(
                                text = "Filing & Returns",
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        modifier = Modifier.testTag("tax_tab_filing")
                    )
                }
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            when (selectedTab) {
                0 -> TaxAssessmentOverviewTab(
                    assessment = assessment,
                    onOpenAdjustments = { selectedTab = 1 },
                    onViewReport = {
                        generatedReportText = onGenerateReport()
                        showReportDialog = true
                    }
                )
                1 -> TaxCalculatorTab(
                    inputState = inputState,
                    assessment = assessment,
                    onUpdateInput = onUpdateInput,
                    onSyncWithStatement = onSyncWithStatement
                )
                2 -> TaxOptimizationTab(
                    assessment = assessment,
                    onApplyTip = { selectedTab = 1 }
                )
                3 -> TaxFilingReturnTab(
                    assessment = assessment,
                    onMarkStatus = onMarkFilingStatus,
                    onViewFullReport = {
                        generatedReportText = onGenerateReport()
                        showReportDialog = true
                    }
                )
            }
        }
    }

    // Report / Assessment Modal
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "e-Tax Assessment Return",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PrimaryNavy
                    )
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Tax Assessment", generatedReportText)
                            clipboard.setPrimaryClip(clip)
                            showCopyNotification = true
                        },
                        modifier = Modifier.testTag("copy_tax_report_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Return Schedule",
                            tint = PrimaryNavy
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (showCopyNotification) {
                        Surface(
                            color = Color(0xFFECFDF5),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = "Copied to clipboard! Ready to paste into State e-Tax portal.",
                                color = EmeraldGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = generatedReportText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 16.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showReportDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                ) {
                    Text("Done")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Tax Assessment Schedule", generatedReportText)
                        clipboard.setPrimaryClip(clip)
                        showCopyNotification = true
                    }
                ) {
                    Text("Copy Schedule")
                }
            },
            containerColor = Color.White
        )
    }
}

// -----------------------------------------------------------------------------
// TAB 0: ASSESSMENT OVERVIEW & BRACKETS
// -----------------------------------------------------------------------------
@Composable
private fun TaxAssessmentOverviewTab(
    assessment: TaxFilingAssessment,
    onOpenAdjustments: () -> Unit,
    onViewReport: () -> Unit
) {
    val currencyFormatter = remember { DecimalFormat("#,##0.00") }
    val compactFormatter = remember { DecimalFormat("#,##0") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Tax Payable Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PrimaryNavy),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_tax_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL ANNUAL TAX PAYABLE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 1.sp
                        )

                        Box(
                            modifier = Modifier
                                .background(EmeraldGreen.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = assessment.filingStatus.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6EE7B7)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "₦${currencyFormatter.format(assessment.totalAnnualTaxPayable)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.testTag("annual_tax_payable_text")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Equivalent to ₦${currencyFormatter.format(assessment.monthlyPayeTax)} / month PAYE",
                        fontSize = 14.sp,
                        color = Color(0xFFCBD5E1),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Metrics Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Effective Rate", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = "${assessment.effectiveTaxRate}%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Column {
                            Text("Top Bracket", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = "${assessment.marginalTaxRate.toInt()}%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFDE047)
                            )
                        }

                        Column {
                            Text("Gross Income", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = "₦${compactFormatter.format(assessment.grossAnnualIncome)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Column {
                            Text("Tax-Free Reliefs", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(
                                text = "₦${compactFormatter.format(assessment.totalStatutoryReliefs)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6EE7B7)
                            )
                        }
                    }

                    if (assessment.taxesAlreadyDeducted > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Taxes Already Deducted / WHT:", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                            Text("₦${currencyFormatter.format(assessment.taxesAlreadyDeducted)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Net Tax Balance Due:", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                            Text("₦${currencyFormatter.format(assessment.netTaxDue)}", color = Color(0xFFFDE047), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenAdjustments,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF475569))),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("adjust_tax_inputs_button")
                        ) {
                            Text("Adjust Reliefs", fontSize = 12.sp)
                        }

                        Button(
                            onClick = onViewReport,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("view_tax_schedule_button")
                        ) {
                            Text("View Return", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Statutory Reliefs Shield Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Statutory Tax Reliefs Claimed",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = PrimaryNavy
                            )
                        }

                        Text(
                            text = "₦${currencyFormatter.format(assessment.totalStatutoryReliefs)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = EmeraldGreen
                        )
                    }

                    Text(
                        text = "Income shielded 100% from income tax under Section 33 of PITA",
                        fontSize = 12.sp,
                        color = SlateMedium,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    // Line items
                    ReliefLineItem(
                        label = "Consolidated Relief Allowance (CRA)",
                        detail = "Higher of ₦200k or 1% Gross + 20% of Gross",
                        amount = assessment.craAmount
                    )

                    if (assessment.pensionContribution > 0) {
                        ReliefLineItem(
                            label = "Pension Contribution (PRA 2014)",
                            detail = "8% mandatory employee pension",
                            amount = assessment.pensionContribution
                        )
                    }

                    if (assessment.nhfContribution > 0) {
                        ReliefLineItem(
                            label = "National Housing Fund (NHF)",
                            detail = "2.5% statutory contribution",
                            amount = assessment.nhfContribution
                        )
                    }

                    if (assessment.nhisContribution > 0) {
                        ReliefLineItem(
                            label = "Health Insurance (NHIS / HMO)",
                            detail = "Qualified health insurance deduction",
                            amount = assessment.nhisContribution
                        )
                    }

                    if (assessment.lifeAssurancePremium > 0) {
                        ReliefLineItem(
                            label = "Life Assurance Premium",
                            detail = "Approved life assurance deduction",
                            amount = assessment.lifeAssurancePremium
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Chargeable Taxable Income:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = PrimaryNavy)
                            Text("₦${currencyFormatter.format(assessment.chargeableTaxableIncome)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryNavy)
                        }
                    }
                }
            }
        }

        // Graduated Tax Brackets (PITA 6th Schedule)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = PrimaryNavy,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Graduated Tax Brackets (Sixth Schedule)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = PrimaryNavy
                            )
                        }
                    }

                    Text(
                        text = "Progressive rates applied to chargeable income under Nigerian law",
                        fontSize = 12.sp,
                        color = SlateMedium,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    assessment.bracketBreakdowns.forEach { tier ->
                        TierRowItem(tier = tier)
                    }

                    if (assessment.isMinimumTaxApplied) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFEF3C7), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Minimum Tax Rule Applied: Your calculated tax is capped at minimum 1% of Gross Annual Income (₦${currencyFormatter.format(assessment.totalAnnualTaxPayable)}).",
                                fontSize = 12.sp,
                                color = Color(0xFF92400E),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Net Take-Home Pay Summary Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ESTIMATED TAKE-HOME PAY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF166534),
                        letterSpacing = 0.5.sp
                    )

                    val annualTakeHome = assessment.grossAnnualIncome - assessment.totalAnnualTaxPayable - assessment.pensionContribution - assessment.nhfContribution
                    val monthlyTakeHome = annualTakeHome / 12.0

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "₦${currencyFormatter.format(monthlyTakeHome)}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF14532D)
                            )
                            Text(
                                text = "per month after PAYE tax and pension deductions",
                                fontSize = 12.sp,
                                color = Color(0xFF15803D)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Annual Net", fontSize = 11.sp, color = Color(0xFF15803D))
                            Text(
                                text = "₦${compactFormatter.format(annualTakeHome)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF14532D)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReliefLineItem(
    label: String,
    detail: String,
    amount: Double
) {
    val currencyFormatter = remember { DecimalFormat("#,##0.00") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = PrimaryNavy)
            Text(detail, fontSize = 11.sp, color = SlateMedium)
        }
        Text(
            text = "₦${currencyFormatter.format(amount)}",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F766E)
        )
    }
}

@Composable
private fun TierRowItem(tier: TaxBracketBreakdown) {
    val currencyFormatter = remember { DecimalFormat("#,##0.00") }
    val isActive = tier.taxableAmountInBracket > 0

    val bgColor = if (isActive) Color(0xFFF8FAFC) else Color.White
    val textColor = if (isActive) PrimaryNavy else Color(0xFF94A3B8)
    val rateBadgeColor = when (tier.ratePercent.toInt()) {
        7 -> Color(0xFFDBEAFE)
        11 -> Color(0xFFE0E7FF)
        15 -> Color(0xFFEDE9FE)
        19 -> Color(0xFFFCE7F3)
        21 -> Color(0xFFFFEDD5)
        else -> Color(0xFFFEE2E2)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(rateBadgeColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${tier.ratePercent.toInt()}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavy
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = tier.tierLabel,
                    fontSize = 13.sp,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                    color = textColor
                )
                if (isActive) {
                    Text(
                        text = "Taxable: ₦${currencyFormatter.format(tier.taxableAmountInBracket)}",
                        fontSize = 11.sp,
                        color = SlateMedium
                    )
                }
            }
        }

        Text(
            text = if (isActive) "₦${currencyFormatter.format(tier.taxChargedInBracket)}" else "₦0.00",
            fontSize = 13.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) PrimaryNavy else Color(0xFFCBD5E1)
        )
    }
}

// -----------------------------------------------------------------------------
// TAB 1: CALCULATOR & RELIEFS CUSTOMIZATION
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaxCalculatorTab(
    inputState: TaxInputState,
    assessment: TaxFilingAssessment,
    onUpdateInput: (TaxInputState) -> Unit,
    onSyncWithStatement: () -> Unit
) {
    var grossInput by remember(inputState.grossAnnualIncome) {
        mutableStateOf(String.format(Locale.US, "%.0f", inputState.grossAnnualIncome))
    }
    var nhisInput by remember(inputState.nhisAnnualPremium) {
        mutableStateOf(String.format(Locale.US, "%.0f", inputState.nhisAnnualPremium))
    }
    var lifeAssuranceInput by remember(inputState.lifeAssurancePremium) {
        mutableStateOf(String.format(Locale.US, "%.0f", inputState.lifeAssurancePremium))
    }
    var advanceTaxInput by remember(inputState.taxesAlreadyPaidOrWht) {
        mutableStateOf(String.format(Locale.US, "%.0f", inputState.taxesAlreadyPaidOrWht))
    }
    var tinInput by remember(inputState.taxpayerTin) {
        mutableStateOf(inputState.taxpayerTin)
    }

    var stateMenuExpanded by remember { mutableStateOf(false) }

    val currencyFormatter = remember { DecimalFormat("#,##0.00") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Sync Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Sync with Statement Inflows",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF1E40AF)
                        )
                        Text(
                            text = "Extract annual income directly from statement salary deposits",
                            fontSize = 12.sp,
                            color = Color(0xFF3B82F6)
                        )
                    }

                    Button(
                        onClick = onSyncWithStatement,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                        modifier = Modifier.testTag("auto_sync_tax_salary_button")
                    ) {
                        Text("Sync", fontSize = 12.sp)
                    }
                }
            }
        }

        // Annual Gross Income Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Gross Annual Income (₦)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PrimaryNavy
                    )
                    Text(
                        text = "Total yearly earnings from salary, business profits, allowances, and bonuses.",
                        fontSize = 12.sp,
                        color = SlateMedium,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = grossInput,
                        onValueChange = {
                            grossInput = it
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            onUpdateInput(inputState.copy(grossAnnualIncome = parsed))
                        },
                        label = { Text("Annual Gross Income (₦)") },
                        leadingIcon = { Text("₦", fontWeight = FontWeight.Bold, color = PrimaryNavy) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryNavy,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gross_income_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    val monthlyVal = (grossInput.toDoubleOrNull() ?: 0.0) / 12.0
                    Text(
                        text = "Approx. ₦${currencyFormatter.format(monthlyVal)} / month gross",
                        fontSize = 12.sp,
                        color = EmeraldGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Tax Reliefs & Deductions
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. Statutory Reliefs & Deductions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PrimaryNavy
                    )
                    Text(
                        text = "Toggle deductions allowed under Nigerian Tax Law to lower taxable income.",
                        fontSize = 12.sp,
                        color = SlateMedium,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    // Pension (8%)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Statutory Pension Contribution (8%)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = PrimaryNavy)
                            Text("Mandatory employee contribution (PRA 2014) - 100% Tax Exempt", fontSize = 11.sp, color = SlateMedium)
                        }
                        Switch(
                            checked = inputState.isPensionEnabled,
                            onCheckedChange = { onUpdateInput(inputState.copy(isPensionEnabled = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = EmeraldGreen),
                            modifier = Modifier.testTag("pension_toggle")
                        )
                    }

                    // National Housing Fund (2.5%)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("National Housing Fund (NHF 2.5%)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = PrimaryNavy)
                            Text("Federal housing scheme statutory deduction", fontSize = 11.sp, color = SlateMedium)
                        }
                        Switch(
                            checked = inputState.isNhfEnabled,
                            onCheckedChange = { onUpdateInput(inputState.copy(isNhfEnabled = it)) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = EmeraldGreen),
                            modifier = Modifier.testTag("nhf_toggle")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // NHIS / HMO
                    OutlinedTextField(
                        value = nhisInput,
                        onValueChange = {
                            nhisInput = it
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            onUpdateInput(inputState.copy(nhisAnnualPremium = parsed))
                        },
                        label = { Text("Health Insurance / NHIS Annual (₦)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryNavy,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("nhis_input")
                    )

                    // Life Assurance
                    OutlinedTextField(
                        value = lifeAssuranceInput,
                        onValueChange = {
                            lifeAssuranceInput = it
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            onUpdateInput(inputState.copy(lifeAssurancePremium = parsed))
                        },
                        label = { Text("Life Assurance Premium Annual (₦)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryNavy,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("life_assurance_input")
                    )

                    // Taxes already paid / WHT
                    OutlinedTextField(
                        value = advanceTaxInput,
                        onValueChange = {
                            advanceTaxInput = it
                            val parsed = it.toDoubleOrNull() ?: 0.0
                            onUpdateInput(inputState.copy(taxesAlreadyPaidOrWht = parsed))
                        },
                        label = { Text("Taxes Already Paid / WHT Deducted (₦)") },
                        supportingText = { Text("Offsets directly against total tax payable") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryNavy,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("advance_tax_input")
                    )
                }
            }
        }

        // Tax Jurisdiction & Identification
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. Tax Authority & Filing Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PrimaryNavy
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ExposedDropdownMenuBox(
                        expanded = stateMenuExpanded,
                        onExpandedChange = { stateMenuExpanded = !stateMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = inputState.stateRevenueBoard,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("State Internal Revenue Service") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = stateMenuExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryNavy,
                                unfocusedBorderColor = SlateBorder
                            ),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = stateMenuExpanded,
                            onDismissRequest = { stateMenuExpanded = false }
                        ) {
                            NIGERIAN_TAX_BOARDS.forEach { board ->
                                DropdownMenuItem(
                                    text = { Text(board) },
                                    onClick = {
                                        onUpdateInput(inputState.copy(stateRevenueBoard = board))
                                        stateMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = tinInput,
                        onValueChange = {
                            tinInput = it
                            onUpdateInput(inputState.copy(taxpayerTin = it))
                        },
                        label = { Text("Tax Identification Number (TIN)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryNavy,
                            unfocusedBorderColor = SlateBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tin_input")
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// TAB 2: TAX OPTIMIZATION & LEGAL SAVINGS
// -----------------------------------------------------------------------------
@Composable
private fun TaxOptimizationTab(
    assessment: TaxFilingAssessment,
    onApplyTip: () -> Unit
) {
    val currencyFormatter = remember { DecimalFormat("#,##0.00") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Legal Tax Optimization Opportunities",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF166534)
                        )
                    }

                    val totalPotential = assessment.savingTips.sumOf { it.potentialTaxSaved }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You can legally reduce your annual tax liability by up to ₦${currencyFormatter.format(totalPotential)} utilizing Nigerian statutory reliefs.",
                        fontSize = 13.sp,
                        color = Color(0xFF15803D),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        items(assessment.savingTips) { tip ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = tip.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PrimaryNavy,
                            modifier = Modifier.weight(1f)
                        )

                        Box(
                            modifier = Modifier
                                .background(Color(0xFFDCFCE7), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Save ₦${currencyFormatter.format(tip.potentialTaxSaved)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = tip.explanation,
                        fontSize = 12.sp,
                        color = SlateDark,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onApplyTip,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryNavy),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Adjust Reliefs in Calculator", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// TAB 3: FILING STATUS & RETURN GENERATOR
// -----------------------------------------------------------------------------
@Composable
private fun TaxFilingReturnTab(
    assessment: TaxFilingAssessment,
    onMarkStatus: (TaxFilingStatus) -> Unit,
    onViewFullReport: () -> Unit
) {
    val currencyFormatter = remember { DecimalFormat("#,##0.00") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Filing Status: ${assessment.filingStatus.label}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PrimaryNavy
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Reference: ${assessment.filingReference} • ${assessment.stateRevenueBoard}",
                        fontSize = 12.sp,
                        color = SlateMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onMarkStatus(TaxFilingStatus.FILED) },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mark_filed_button")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mark as Filed", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onViewFullReport,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("view_full_report_button")
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("View Return", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Annual Return Checklist (Form A)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PrimaryNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ChecklistItem(title = "Gross Annual Income & Benefits Computed", isComplete = assessment.grossAnnualIncome > 0)
                    ChecklistItem(title = "Consolidated Relief Allowance (CRA) Claimed", isComplete = assessment.craAmount > 0)
                    ChecklistItem(title = "Pension (PRA 2014) Exemption Calculated", isComplete = assessment.pensionContribution > 0)
                    ChecklistItem(title = "Graduated PITA Rates Applied", isComplete = assessment.totalAnnualTaxPayable > 0)
                    ChecklistItem(title = "Tax Identification Number (TIN) Configured", isComplete = assessment.taxpayerTin.isNotBlank())
                    ChecklistItem(title = "State Revenue Authority Identified", isComplete = assessment.stateRevenueBoard.isNotBlank())
                }
            }
        }
    }
}

@Composable
private fun ChecklistItem(title: String, isComplete: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isComplete) Icons.Default.CheckCircle else Icons.Default.Info,
            contentDescription = null,
            tint = if (isComplete) EmeraldGreen else Color(0xFFCBD5E1),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            color = if (isComplete) PrimaryNavy else SlateMedium,
            fontWeight = if (isComplete) FontWeight.Medium else FontWeight.Normal
        )
    }
}
