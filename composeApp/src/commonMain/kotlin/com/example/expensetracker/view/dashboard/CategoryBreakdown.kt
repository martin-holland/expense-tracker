package com.example.expensetracker.view.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.model.ExpenseCategory
import com.example.theme.com.example.expensetracker.LocalAppColors
import network.chaintech.cmpcharts.common.model.PlotType
import network.chaintech.cmpcharts.ui.piechart.charts.PieChart
import network.chaintech.cmpcharts.ui.piechart.models.PieChartConfig
import network.chaintech.cmpcharts.ui.piechart.models.PieChartData

/* ─────────────── COLOR HELPERS ─────────────── */

fun Color.isDark(): Boolean {
    val r = (red * 255).toInt()
    val g = (green * 255).toInt()
    val b = (blue * 255).toInt()

    val brightness = (r * 299 + g * 587 + b * 114) / 1000
    return brightness < 128
}

@Composable
fun categoryColor(name: String, index: Int): Color {
    val appColors = LocalAppColors.current
    val isDark = appColors.background.isDark()

    val darkColors = listOf(
        appColors.chart1,
        appColors.chart2,
        appColors.chart3,
        appColors.chart4,
        appColors.chart5
    )

    val lightColors = listOf(
        Color(0xFFA1E9E2),
        Color(0xFFFFE97A),
        Color(0xFFFF7B7B),
        Color(0xFF37C7C4)
    )

    val colors = if (isDark) darkColors else lightColors
    return colors[index % colors.size]
}

/* ─────────────── CATEGORY CARD + PIE ─────────────── */

@Composable
fun ExpenseBreakdownCard(
    categorySum: Map<ExpenseCategory, Double>
) {
    val total = categorySum.values.sum()
    val appColors = LocalAppColors.current

    Column(Modifier.padding(horizontal = 16.dp)) {

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(appColors.card),
            border = BorderStroke(1.dp, appColors.border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    "Spending by Category",
                    color = appColors.foreground,
                    fontWeight = FontWeight.SemiBold
                )

                if (total == 0.0) {
                    Text("No data available", color = appColors.mutedForeground)
                } else {
                    DonutPieChart(categorySum = categorySum)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(appColors.card),
            border = BorderStroke(1.dp, appColors.border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    "Category Breakdown",
                    fontWeight = FontWeight.Bold,
                    color = appColors.foreground
                )

                if (total == 0.0) {
                    Text("No expenses yet.", color = appColors.mutedForeground)
                } else {
                    BreakdownList(categorySum)
                }
            }
        }
    }
}

/* ─────────────── PIE CHART WITH CLICK ─────────────── */

@Composable
fun DonutPieChart(
    categorySum: Map<ExpenseCategory, Double>
) {
    val appColors = LocalAppColors.current
    val total = categorySum.values.sumOf { it }

    val slices = categorySum.entries.mapIndexed { index, (category, amount) ->
        val percent = if (total == 0.0) 0f else ((amount / total) * 100f).toFloat()
        PieChartData.Slice(
            label = category.displayName,
            value = percent,
            color = categoryColor(category.displayName, index)
        )
    }

    var activeSlice by remember { mutableStateOf<PieChartData.Slice?>(null) }

    val pieData = PieChartData(
        slices = slices,
        plotType = PlotType.Pie
    )

    val config = PieChartConfig(
        showSliceLabels = false,
        isAnimationEnable = true,
        animationDuration = 900,
        labelVisible = false,
        backgroundColor = Color.Transparent
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(appColors.card),
        contentAlignment = Alignment.Center
    ) {
        PieChart(
            modifier = Modifier.size(220.dp),
            pieChartData = pieData,
            pieChartConfig = config
        ) { clicked ->
            activeSlice = if (activeSlice?.label == clicked.label) null else clicked
        }

        // Donut center
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(appColors.background)
        )
    }

    // Selected slice info (same logic)
    activeSlice?.let { slice ->
        val category = slice.label
        val percent = slice.value.toInt()
        val amount = categorySum.entries
            .firstOrNull { it.key.displayName == category }
            ?.value ?: 0.0

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(appColors.card, RoundedCornerShape(10.dp))
                .border(1.dp, appColors.border, RoundedCornerShape(10.dp))
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$category — $${amount.format(2)} ($percent%)",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = appColors.foreground
            )
        }
    }
}

/* ─────────────── BREAKDOWN LIST ─────────────── */

@Composable
fun BreakdownList(
    categorySum: Map<ExpenseCategory, Double>
) {
    val total = categorySum.values.sum()
    categorySum.entries.forEachIndexed { idx, (cat, amount) ->
        BreakdownRow(
            name = cat.displayName,
            percent = amount / total * 100,
            amount = amount,
            color = categoryColor(cat.displayName, idx)
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun BreakdownRow(
    name: String,
    percent: Double,
    amount: Double,
    color: Color
) {
    val appColors = LocalAppColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(color, CircleShape)
        )

        Spacer(Modifier.width(12.dp))

        Text(
            text = name,
            modifier = Modifier.weight(1f),
            color = appColors.foreground
        )

        Text(
            text = "${percent.toInt()}%",
            modifier = Modifier.width(40.dp),
            color = appColors.mutedForeground
        )

        Text(
            text = "$${amount.format(2)}",
            color = appColors.foreground,
            fontWeight = FontWeight.Bold
        )
    }
}
