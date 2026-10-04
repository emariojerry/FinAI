package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FinancialAlert
import com.example.data.model.FinancialAudit
import com.example.data.model.Transaction
import com.example.data.model.UserProfile
import com.example.ui.components.FinCard
import com.example.ui.components.HealthScoreGauge
import com.example.ui.components.StatGridCard
import com.example.ui.components.WaterfallCashFlowView
import com.example.ui.components.formatCompact
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CoralRedSubtle
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenSubtle
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldSubtle
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateMedium
import java.util.Locale

@Composable
fun DashboardScreen(
    user: UserProfile,
    audit: FinancialAudit?,
    transactions: List<Transaction>,
    alerts: List<FinancialAlert>,
    onNavigateToAudit: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToTax: () -> Unit = {},
    onOpenAddTransaction: () -> Unit,
    onOpenStatementUpload: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // Header with User Greeting
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good day, ${user.name.split(" ").first()}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SlateDark
                    )
                    Text(
                        text = "Personal Financial Auditor & Intelligence",
                        fontSize = 12.sp,
                        color = SlateMedium
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(EmeraldGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${transactions.size} Records",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                    }
                }
            }
        }

        // Primary Statement Ingestion Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenStatementUpload,
                    modifier = Modifier.weight(1.8f).testTag("quick_upload_statement_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upload Statement (PDF/CSV)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onOpenAddTransaction,
                    modifier = Modifier.weight(1f).testTag("quick_add_entry_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SlateDark)
                ) {
                    Text("Add Entry", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Empty State when no statement is uploaded
        if (transactions.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
                    color = Color(0xFFF8FAFC)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.UploadFile,
                                contentDescription = null,
                                tint = PrimaryNavy,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "No Statement Uploaded Yet",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Upload your bank statement as a PDF or CSV file to start your personal financial audit. The AI will detect money leakages, track recurring commitments, and calculate your health score.",
                            fontSize = 13.sp,
                            color = SlateMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onOpenStatementUpload,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload Bank Statement (PDF / CSV)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Hero Financial Health Score Card
        if (audit != null) {
            item {
                FinCard(
                    modifier = Modifier.testTag("health_score_card"),
                    onClick = onNavigateToAudit
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.padding(bottom = 6.dp)
                                ) {
                                    Text(
                                        text = "FINANCIAL HEALTH SCORE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SlateMedium,
                                        letterSpacing = 0.5.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "Your Financial Health",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Based on your savings rate (${String.format(Locale.US, "%.1f", audit.savingsRate)}%), DTI ratio (${String.format(Locale.US, "%.1f", audit.debtToIncomeRatio)}%), and liquidity buffer.",
                                    fontSize = 12.sp,
                                    color = SlateMedium,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "View Detailed Audit",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryNavy
                                    )
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = PrimaryNavy, modifier = Modifier.size(14.dp))
                                }
                            }

                            HealthScoreGauge(
                                score = audit.healthScore,
                                rating = audit.scoreRating
                            )
                        }
                    }
                }
            }

            // Key Metrics 4-Grid (Net Worth, Income, Expenses, Savings)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatGridCard(
                            title = "NET WORTH",
                            value = "₦" + formatCompact(audit.netWorth),
                            subtitle = "Assets - Liabilities",
                            indicatorColor = PrimaryNavy,
                            modifier = Modifier.weight(1f)
                        )
                        StatGridCard(
                            title = "MONTHLY INFLOW",
                            value = "₦" + formatCompact(audit.totalIncome),
                            subtitle = "Salary & Contracts",
                            indicatorColor = EmeraldGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatGridCard(
                            title = "MONTHLY SPENDING",
                            value = "₦" + formatCompact(audit.totalExpenses),
                            subtitle = "${audit.topSpendingCategories.size} Categories Tracked",
                            indicatorColor = GoldAccent,
                            modifier = Modifier.weight(1f)
                        )
                        StatGridCard(
                            title = "NET CASH FLOW",
                            value = (if (audit.netCashFlow >= 0) "+" else "-") + "₦" + formatCompact(Math.abs(audit.netCashFlow)),
                            subtitle = "${String.format(Locale.US, "%.1f", audit.savingsRate)}% Savings Rate",
                            indicatorColor = if (audit.netCashFlow >= 0) EmeraldGreen else CoralRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Annual Tax Liability & Filing Overview Card
            item {
                FinCard(
                    modifier = Modifier.testTag("dashboard_tax_card"),
                    onClick = onNavigateToTax
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFEFF6FF),
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                Text(
                                    text = "ANNUAL TAX ASSESSMENT • PITA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D4ED8),
                                    letterSpacing = 0.5.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "Personal Income Tax & PAYE",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Calculate exact yearly taxes, claim CRA and pension reliefs, and prepare your State e-Tax return.",
                                fontSize = 12.sp,
                                color = SlateMedium,
                                lineHeight = 16.sp
                            )
                        }

                        Button(
                            onClick = onNavigateToTax,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Calculate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Financial Alerts
            if (alerts.isNotEmpty()) {
                item {
                    val alert = alerts.first()
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                        color = GoldSubtle
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GoldAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = alert.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = alert.message,
                                    fontSize = 11.sp,
                                    color = Color(0xFF78350F),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            // Cash Flow Waterfall Breakdown
            item {
                FinCard {
                    WaterfallCashFlowView(audit = audit)
                }
            }

            // Your Biggest Money Leak
            val topLeak = audit.moneyLeakages.firstOrNull()
            if (topLeak != null) {
                item {
                    FinCard(
                        backgroundColor = Color(0xFFFFFBEB),
                        borderColor = Color(0xFFFDE68A),
                        onClick = onNavigateToAudit
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "AUDIT FINDING: BIGGEST LEAK",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFB45309),
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = CoralRedSubtle
                                ) {
                                    Text(
                                        text = "-${topLeak.formattedMonthly()}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CoralRed,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = topLeak.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = topLeak.whyItMatters,
                                fontSize = 12.sp,
                                color = SlateMedium,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Annual waste: ${topLeak.formattedAnnualSavings()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CoralRed
                                )
                                Text(
                                    text = "See All ${audit.moneyLeakages.size} Leaks →",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryNavy
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Transactions Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onOpenAddTransaction) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = onNavigateToTransactions) {
                        Text("View All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Recent 4 Transactions
        items(transactions.take(4)) { tx ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, SlateBorder, RoundedCornerShape(14.dp)),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (tx.isDebit) Color(0xFFF1F5F9) else EmeraldGreenSubtle),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tx.merchant.take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = if (tx.isDebit) SlateDark else EmeraldGreen,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = tx.merchant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SlateDark
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFF1F5F9)
                                ) {
                                    Text(
                                        text = tx.category.displayName,
                                        fontSize = 10.sp,
                                        color = SlateMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tx.date,
                                    fontSize = 11.sp,
                                    color = SlateLight
                                )
                            }
                        }
                    }

                    Text(
                        text = tx.formattedAmount(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (tx.isDebit) SlateDark else EmeraldGreen
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
