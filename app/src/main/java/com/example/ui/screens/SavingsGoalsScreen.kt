package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FinancialAudit
import com.example.data.model.SavingsGoal
import com.example.ui.components.FinCard
import com.example.ui.components.formatCompact
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
fun SavingsGoalsScreen(
    goals: List<SavingsGoal>,
    audit: FinancialAudit?,
    onAddGoal: () -> Unit,
    onAddProgress: (goalId: Long, amount: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthsCovered = if (audit != null && audit.totalExpenses > 0) {
        audit.liquidAssets / audit.totalExpenses
    } else 0.0

    val hasHighDebt = audit != null && audit.debtToIncomeRatio > 25.0
    val isReadyToInvest = monthsCovered >= 3.0 && !hasHighDebt

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
                        text = "Savings & Investment",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SlateDark
                    )
                    Text(
                        text = "Goals, runway & investment readiness",
                        fontSize = 12.sp,
                        color = SlateMedium
                    )
                }

                Button(
                    onClick = onAddGoal,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("create_goal_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Goal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Emergency Runway Evaluation Card
        item {
            FinCard {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "EMERGENCY FUND STATUS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateLight,
                            letterSpacing = 0.5.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (monthsCovered >= 3.0) EmeraldGreenSubtle else GoldSubtle
                        ) {
                            Text(
                                text = "${String.format(java.util.Locale.US, "%.1f", monthsCovered)} Months Covered",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (monthsCovered >= 3.0) EmeraldGreen else Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Liquid Cash Buffer: ₦${formatCompact(audit?.liquidAssets ?: 0.0)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SlateDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Recommended target is 3 to 6 months of living expenses (~₦${formatCompact((audit?.totalExpenses ?: 0.0) * 3)} - ₦${formatCompact((audit?.totalExpenses ?: 0.0) * 6)}).",
                        fontSize = 12.sp,
                        color = SlateMedium,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Investment Readiness Diagnostic
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        1.dp,
                        if (isReadyToInvest) Color(0xFFBBF7D0) else Color(0xFFFED7AA),
                        RoundedCornerShape(16.dp)
                    ),
                color = if (isReadyToInvest) EmeraldGreenSubtle else Color(0xFFFFF7ED)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isReadyToInvest) EmeraldGreen else GoldAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isReadyToInvest) Icons.Default.TrendingUp else Icons.Default.Info,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isReadyToInvest) "Investment Ready" else "Build Buffer Before High-Risk Investing",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SlateDark
                        )
                        Text(
                            text = if (isReadyToInvest) {
                                "You have a solid 3+ month cash reserve and manageable debt. You can safely allocate discretionary surplus to Treasury Bills (15%+ p.a.) or Mutual Funds."
                            } else {
                                "Focus first on securing a 3-month emergency cushion and paying down high-interest BNPL/personal loans before equities."
                            },
                            fontSize = 11.sp,
                            color = SlateDark,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Goals List Header
        item {
            Text(
                text = "Active Financial Goals",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = SlateDark
            )
        }

        // Goals items
        items(goals) { goal ->
            FinCard {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = goal.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = SlateDark
                            )
                            Text(
                                text = "${goal.category.displayName} • Target: ${goal.targetMonths} months",
                                fontSize = 11.sp,
                                color = SlateLight
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (goal.progressPercentage >= 100) EmeraldGreenSubtle else Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = "${goal.progressPercentage}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (goal.progressPercentage >= 100) EmeraldGreen else PrimaryNavy,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { (goal.progressPercentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = EmeraldGreen,
                        trackColor = Color(0xFFE2E8F0)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Saved: ${goal.formattedCurrent()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateDark
                        )
                        Text(
                            text = "Target: ${goal.formattedTarget()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Monthly & Weekly recommendation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8FAFC))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Recommended Savings Rate:", fontSize = 10.sp, color = SlateLight)
                            Text("${goal.formattedMonthly()}/mo • ${goal.formattedWeekly()}/wk", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateDark)
                        }

                        OutlinedButton(
                            onClick = { onAddProgress(goal.id, 50000.0) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("+₦50k", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Asset Class Knowledge Guide
        item {
            FinCard {
                Column {
                    Text(
                        text = "Asset Class Primer (Nigeria & Global)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    AssetPrimerItem("Treasury Bills (T-Bills)", "Risk: Very Low | Returns: ~15-20% p.a.", "Short-term government-backed debt issued by the Central Bank of Nigeria. Safe haven for capital preservation.")
                    AssetPrimerItem("Mutual Funds / Money Market", "Risk: Low-Moderate | Returns: ~12-18% p.a.", "Pooled investment managed by institutional custodians like Stanbic or United Capital. High liquidity with better yield than standard savings.")
                    AssetPrimerItem("Equities / Stocks (NGX & US)", "Risk: Moderate-High | Returns: Variable", "Ownership in publicly traded enterprises. Best suited for long horizons (3+ years) to beat inflation.")
                    AssetPrimerItem("Real Estate", "Risk: Moderate | High Initial Capital", "Tangible property generating rental yield and long-term capital appreciation. Illiquid but inflation-resistant.")
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun AssetPrimerItem(title: String, subtitle: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateDark)
        Text(subtitle, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = EmeraldGreen)
        Text(desc, fontSize = 11.sp, color = SlateMedium, lineHeight = 15.sp)
    }
}
