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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.Asset
import com.example.data.model.Liability
import com.example.data.model.UserProfile
import com.example.ui.components.FinCard
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
import java.util.Locale

@Composable
fun AssetsLiabilitiesScreen(
    user: UserProfile,
    assets: List<Asset>,
    liabilities: List<Liability>,
    onAddAsset: () -> Unit,
    onConfirmPotentialAsset: (Long) -> Unit,
    onDeleteAsset: (Asset) -> Unit,
    onAddLiability: () -> Unit,
    onDeleteLiability: (Liability) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Overview, 1: Assets, 2: Liabilities

    val totalAssets = assets.sumOf { it.value }
    val liquidAssets = assets.filter { it.isLiquid }.sumOf { it.value }
    val illiquidAssets = totalAssets - liquidAssets

    val totalLiabilities = liabilities.sumOf { it.outstandingAmount }
    val monthlyDebtRepayments = liabilities.sumOf { it.monthlyPayment }
    val netWorth = totalAssets - totalLiabilities
    val debtToIncome = if (user.monthlyIncome > 0) (monthlyDebtRepayments / user.monthlyIncome) * 100 else 0.0

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
                        text = "Balance Sheet & Net Worth",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SlateDark
                    )
                    Text(
                        text = "What you own vs. what you owe",
                        fontSize = 12.sp,
                        color = SlateMedium
                    )
                }
            }
        }

        // Hero Net Worth Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(PrimaryNavy),
                color = PrimaryNavy
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "NET WORTH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₦" + formatCompact(netWorth),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Assets", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                            Text("₦" + formatCompact(totalAssets), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Text("-", fontSize = 18.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        Column {
                            Text("Total Liabilities", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                            Text("₦" + formatCompact(totalLiabilities), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFCA5A5))
                        }
                        Text("=", fontSize = 18.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                        Column {
                            Text("Liquid Cash", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                            Text("₦" + formatCompact(liquidAssets), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        }
                    }
                }
            }
        }

        // Sub-tabs
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFFF1F5F9),
                contentColor = PrimaryNavy,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Summary", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Assets (${assets.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Liabilities (${liabilities.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }
        }

        // Content by Tab
        when (selectedTab) {
            0 -> {
                // Summary & Human Explanation
                item {
                    FinCard {
                        Column {
                            Text(
                                text = "Debt Impact Analysis",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "About ₦${debtToIncome.toInt()} of every ₦100 you earn goes toward debt repayment.",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (debtToIncome > 20) CoralRed else EmeraldGreen
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Monthly debt obligations total ₦${formatCompact(monthlyDebtRepayments)} across ${liabilities.size} accounts. Maintaining this ratio under 20% provides maximum financial flexibility.",
                                fontSize = 12.sp,
                                color = SlateMedium,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                item {
                    FinCard {
                        Column {
                            Text(
                                text = "Asset Liquidity Breakdown",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateDark
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Liquid Assets (Cash, T-Bills, Stocks)", fontSize = 12.sp, color = SlateDark)
                                Text("₦${formatCompact(liquidAssets)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Illiquid Assets (Real Estate, Vehicles)", fontSize = 12.sp, color = SlateDark)
                                Text("₦${formatCompact(illiquidAssets)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateMedium)
                            }
                        }
                    }
                }
            }

            1 -> {
                // Assets Tab
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tracked Assets",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                        Button(
                            onClick = onAddAsset,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Asset", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                items(assets) { asset ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                1.dp,
                                if (asset.isPotential) GoldAccent else SlateBorder,
                                RoundedCornerShape(14.dp)
                            ),
                        color = if (asset.isPotential) Color(0xFFFFFBEB) else MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            if (asset.isPotential) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = GoldSubtle
                                    ) {
                                        Text(
                                            text = "Potential Asset — Confirm Required",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB45309),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Button(
                                        onClick = { onConfirmPotentialAsset(asset.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Confirm Asset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(asset.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateDark)
                                    Text(
                                        text = "${asset.type.displayName} • ${if (asset.isLiquid) "Liquid" else "Illiquid"}",
                                        fontSize = 11.sp,
                                        color = SlateLight
                                    )
                                    if (asset.notes.isNotBlank()) {
                                        Text(asset.notes, fontSize = 11.sp, color = SlateMedium, modifier = Modifier.padding(top = 2.dp))
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = asset.formattedValue(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = EmeraldGreen
                                    )
                                    IconButton(onClick = { onDeleteAsset(asset) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = SlateLight, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Liabilities Tab
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tracked Liabilities",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateDark
                        )
                        Button(
                            onClick = onAddLiability,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Debt", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                items(liabilities) { liab ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, SlateBorder, RoundedCornerShape(14.dp)),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(liab.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateDark)
                                    Text(
                                        text = "${liab.type.displayName} • ${liab.lender}",
                                        fontSize = 11.sp,
                                        color = SlateLight
                                    )
                                    if (liab.interestRate != null) {
                                        Text(
                                            text = "Interest: ${liab.interestRate}% APR",
                                            fontSize = 11.sp,
                                            color = CoralRed,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = liab.formattedOutstanding(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = CoralRed
                                        )
                                        Text(
                                            text = liab.formattedMonthlyPayment(),
                                            fontSize = 11.sp,
                                            color = SlateMedium
                                        )
                                    }
                                    IconButton(onClick = { onDeleteLiability(liab) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = SlateLight, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
