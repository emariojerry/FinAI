package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategorySpend
import com.example.data.model.FinancialAudit
import com.example.ui.theme.CoralRed
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateMedium
import java.text.NumberFormat
import java.util.Locale

@Composable
fun FinCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        color = backgroundColor,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 0.dp
    ) {
        Box(modifier = Modifier.padding(18.dp)) {
            content()
        }
    }
}

@Composable
fun HealthScoreGauge(
    score: Int,
    rating: String,
    modifier: Modifier = Modifier,
    size: Dp = 130.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = score / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "score_anim"
    )

    val arcColor = when {
        score >= 80 -> EmeraldGreen
        score >= 65 -> Color(0xFF10B981)
        score >= 50 -> GoldAccent
        else -> CoralRed
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 14.dp.toPx()
            val canvasSize = this.size.width
            val radius = (canvasSize - strokeWidth) / 2f
            val center = Offset(canvasSize / 2f, canvasSize / 2f)

            // Background Track
            drawArc(
                color = Color(0xFFE2E8F0),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                size = Size(canvasSize - strokeWidth, canvasSize - strokeWidth),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Foreground Progress Arc
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(GoldAccent, arcColor, arcColor)
                ),
                startAngle = 135f,
                sweepAngle = 270f * animatedProgress,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                size = Size(canvasSize - strokeWidth, canvasSize - strokeWidth),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$score",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SlateDark
            )
            Text(
                text = "/100",
                fontSize = 11.sp,
                color = SlateMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = rating,
                fontSize = 11.sp,
                color = arcColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun WaterfallCashFlowView(
    audit: FinancialAudit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "WHERE IS MY MONEY GOING?",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SlateLight,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Waterfall steps
        val steps = listOf(
            WaterfallStep("Total Income", audit.totalIncome, EmeraldGreen, true, "Inflow from salary & contracts"),
            WaterfallStep("Fixed Essentials", -audit.fixedExpenses, SlateMedium, false, "Housing, utilities & groceries"),
            WaterfallStep("Variable Spending", -audit.variableExpenses, GoldAccent, false, "Dining, ride-hailing & shopping"),
            WaterfallStep("Debt Payments", -audit.debtPayments, CoralRed, false, "Loans & BNPL obligations"),
            WaterfallStep("Net Cash Flow", audit.netCashFlow, if (audit.netCashFlow >= 0) EmeraldGreen else CoralRed, true, "Free liquidity for savings & growth")
        )

        steps.forEachIndexed { index, step ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(step.color)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = step.label,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = SlateDark
                    )
                    Text(
                        text = step.subtext,
                        fontSize = 11.sp,
                        color = SlateLight
                    )
                }
                Text(
                    text = (if (step.amount < 0) "-" else if (step.isPositive) "+" else "") + "₦" + formatCompact(Math.abs(step.amount)),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = step.color
                )
            }

            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .width(2.dp)
                        .height(10.dp)
                        .background(Color(0xFFCBD5E1))
                )
            }
        }
    }
}

data class WaterfallStep(
    val label: String,
    val amount: Double,
    val color: Color,
    val isPositive: Boolean,
    val subtext: String
)

@Composable
fun SpendingCategoryBar(
    categorySpend: CategorySpend,
    totalExpenses: Double,
    modifier: Modifier = Modifier
) {
    val pct = if (totalExpenses > 0) (categorySpend.amount / totalExpenses).toFloat() else 0f

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = categorySpend.category.displayName,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = SlateDark
            )
            Text(
                text = "₦" + formatCompact(categorySpend.amount) + " (${String.format(Locale.US, "%.1f", categorySpend.percentageOfSpend)}%)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = SlateDark
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFE2E8F0))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct.coerceIn(0.01f, 1f))
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        when {
                            pct > 0.25f -> CoralRed
                            pct > 0.15f -> GoldAccent
                            else -> EmeraldGreen
                        }
                    )
            )
        }
    }
}

@Composable
fun StatGridCard(
    title: String,
    value: String,
    subtitle: String,
    indicatorColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, SlateBorder, RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SlateLight,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SlateDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = indicatorColor
            )
        }
    }
}

fun formatCompact(amount: Double): String {
    val abs = Math.abs(amount)
    return when {
        abs >= 1_000_000 -> String.format(Locale.US, "%.2fM", amount / 1_000_000)
        abs >= 1_000 -> String.format(Locale.US, "%.0fK", amount / 1_000)
        else -> NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 }.format(amount)
    }
}
