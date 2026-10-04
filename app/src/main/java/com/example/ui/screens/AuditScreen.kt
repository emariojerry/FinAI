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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FinancialAudit
import com.example.data.model.LeakageSeverity
import com.example.data.model.MoneyLeakage
import com.example.ui.components.FinCard
import com.example.ui.components.HealthScoreGauge
import com.example.ui.components.SpendingCategoryBar
import com.example.ui.components.formatCompact
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CoralRedSubtle
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenSubtle
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldSubtle
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateMedium

@Composable
fun AuditScreen(
    audit: FinancialAudit?,
    onNavigateToChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (audit == null) {
        Box(modifier = modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
            Text("Calculating financial audit...", color = SlateMedium)
        }
        return
    }

    var selectedTimeline by remember { mutableStateOf("30 Days") }
    val timelines = listOf("7 Days", "30 Days", "3 Months", "6 Months", "1 Year")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Financial Audit",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SlateDark
                    )
                    Text(
                        text = "Comprehensive diagnostic & leakage detection",
                        fontSize = 12.sp,
                        color = SlateMedium
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = audit.auditDate,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SlateMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Timeline Selector
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(timelines) { time ->
                    val isSelected = selectedTimeline == time
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedTimeline = time },
                        color = if (isSelected) PrimaryNavy else Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = time,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else SlateDark,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Health Score & Diagnostic Summary
        item {
            FinCard(modifier = Modifier.testTag("audit_score_card")) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text(
                                text = "HEALTH SCORE DIAGNOSTIC",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateLight,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = audit.scoreRating,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SlateDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Savings Rate: ${String.format(java.util.Locale.US, "%.1f", audit.savingsRate)}% • DTI: ${String.format(java.util.Locale.US, "%.1f", audit.debtToIncomeRatio)}%",
                                fontSize = 12.sp,
                                color = SlateMedium
                            )
                        }

                        HealthScoreGauge(
                            score = audit.healthScore,
                            rating = audit.scoreRating,
                            size = 110.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(SlateBorder)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Executive Summary",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = audit.executiveSummary,
                        fontSize = 12.sp,
                        color = SlateDark,
                        lineHeight = 17.sp
                    )
                }
            }
        }

        // Strong Areas & Areas to Improve
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Strong
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(14.dp)),
                    color = EmeraldGreenSubtle
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Strengths", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF166534))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        audit.strongAreas.forEach { s ->
                            Text("• $s", fontSize = 11.sp, color = Color(0xFF166534), modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                }

                // Areas to Improve
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFFFECDD3), RoundedCornerShape(14.dp)),
                    color = CoralRedSubtle
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = CoralRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Attention", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF991B1B))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        audit.areasToImprove.forEach { a ->
                            Text("• $a", fontSize = 11.sp, color = Color(0xFF991B1B), modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                }
            }
        }

        // Money Leakage Detector Section
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingDown, contentDescription = null, tint = CoralRed)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Money Leakage Detector",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CoralRedSubtle
                    ) {
                        Text(
                            text = "${audit.moneyLeakages.size} Active Leaks",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CoralRed,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Identified spending patterns draining unnecessary cash every month.",
                    fontSize = 12.sp,
                    color = SlateMedium
                )
            }
        }

        // Leakage Cards
        items(audit.moneyLeakages) { leak ->
            LeakageCard(leak = leak)
        }

        // Potential Savings Banner
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(PrimaryNavy),
                color = PrimaryNavy
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "POTENTIAL ANNUAL SAVINGS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "₦" + formatCompact(audit.potentialAnnualSavings),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "By plugging these detected leakages, you recover ~₦${formatCompact(audit.potentialMonthlySavings)}/month.",
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }
        }

        // 30-Day Financial Reset Action Plan
        item {
            FinCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = GoldAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "30-Day Financial Reset Plan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    audit.resetPlan30Days.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryNavy),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = SlateDark
                                )
                                Text(
                                    text = item.description,
                                    fontSize = 11.sp,
                                    color = SlateMedium,
                                    lineHeight = 15.sp
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = EmeraldGreenSubtle
                            ) {
                                Text(
                                    text = "+₦" + formatCompact(item.potentialMonthlySavings),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Top Spending Categories Breakdown
        item {
            FinCard {
                Column {
                    Text(
                        text = "Category Spending Breakdown",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    audit.topSpendingCategories.forEach { cat ->
                        SpendingCategoryBar(categorySpend = cat, totalExpenses = audit.totalExpenses)
                    }
                }
            }
        }

        // Small Discretionary Purchases & Bank Fees
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, SlateBorder, RoundedCornerShape(14.dp)),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Micro-Purchases", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SlateLight)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("₦" + formatCompact(audit.smallDiscretionaryTotal), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = SlateDark)
                        Text("${audit.smallDiscretionaryCount} transactions < ₦5k", fontSize = 10.sp, color = SlateMedium)
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, SlateBorder, RoundedCornerShape(14.dp)),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Bank & Alert Fees", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SlateLight)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("₦" + formatCompact(audit.bankFeesTotal), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = SlateDark)
                        Text("SMS, stamp duty & levies", fontSize = 10.sp, color = SlateMedium)
                    }
                }
            }
        }

        // Chat CTA Button
        item {
            Button(
                onClick = onNavigateToChat,
                modifier = Modifier.fillMaxWidth().testTag("ask_auditor_cta"),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ask Auditor About This Report", fontWeight = FontWeight.Bold)
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun LeakageCard(leak: MoneyLeakage) {
    val severityColor = when (leak.severity) {
        LeakageSeverity.HIGH -> CoralRed
        LeakageSeverity.MEDIUM -> GoldAccent
        LeakageSeverity.LOW -> ElectricBlue
    }

    FinCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = leak.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateDark,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = severityColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${leak.severity.name} LEAK",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = severityColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = leak.formattedMonthly(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = CoralRed
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(${String.format(java.util.Locale.US, "%.1f", leak.percentageOfIncome)}% of income)",
                    fontSize = 11.sp,
                    color = SlateMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = leak.whyItMatters,
                fontSize = 12.sp,
                color = SlateDark,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = "Recommendation:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateMedium
                    )
                    Text(
                        text = leak.recommendedAction,
                        fontSize = 11.sp,
                        color = SlateDark,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
