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
import com.example.expensetracker.domain.analytics.CategoryTotal
import com.example.expensetracker.model.ExpenseCategory
import com.example.theme.com.example.expensetracker.LocalAppColors
import network.chaintech.cmpcharts.common.model.PlotType
import network.chaintech.cmpcharts.ui.piechart.charts.PieChart
import network.chaintech.cmpcharts.ui.piechart.models.PieChartConfig
import network.chaintech.cmpcharts.ui.piechart.models.PieChartData
import kotlin.math.pow
import kotlin.math.round


fun Double.formatAmount(decimals: Int = 2): String {
    val factor = 10.0.pow(decimals)
    val rounded = round(this * factor) / factor
    return rounded.toString()
}


fun getCategoryChartColor(category: ExpenseCategory): Color =
    when (category) {
        ExpenseCategory.FOOD      -> Color(0xFFFF5A5A)  // red
        ExpenseCategory.TRAVEL    -> Color(0xFF3A98FF)  // blue
        ExpenseCategory.UTILITIES -> Color(0xFF3EC6BA)  // teal
        ExpenseCategory.OTHER     -> Color(0xFFFFBC5B)  // orange
    }



/*                          MAIN CARD                              */

@Composable
fun ExpenseBreakdownCard(
    categoryTotals: List<CategoryTotal>
) {
    val total = categoryTotals.sumOf { it.total }
    val appColors = LocalAppColors.current

    Column(Modifier.padding(horizontal = 16.dp)) {

        // PIE CHART CARD
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
                    DonutPieChart(categoryTotals)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // BREAKDOWN CARD
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
                    BreakdownList(categoryTotals)
                }
            }
        }
    }
}



/*                           PIE CHART                             */

@Composable
fun DonutPieChart(
    categoryTotals: List<CategoryTotal>
) {
    val appColors = LocalAppColors.current
    val totalAmount = categoryTotals.sumOf { it.total }

    val slices = categoryTotals.map { item ->
        val percent = if (totalAmount == 0.0) 0f
        else ((item.total / totalAmount) * 100f).toFloat()

        PieChartData.Slice(
            label = item.category.displayName,
            value = percent,
            color = getCategoryChartColor(item.category)
        )
    }

    var activeSlice by remember { mutableStateOf<PieChartData.Slice?>(null) }

    val pieData = PieChartData(slices = slices, plotType = PlotType.Pie)

    val config = PieChartConfig(
        showSliceLabels = false,
        isAnimationEnable = true,
        animationDuration = 900,
        labelVisible = false,
        backgroundColor = Color.Transparent
    )

    // CHART + DONUT HOLE
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
            activeSlice =
                if (activeSlice?.label == clicked.label) null else clicked
        }

        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(appColors.background)
        )
    }

    // SELECTED SLICE INFO
    activeSlice?.let { slice ->

        val matchingCategory = categoryTotals.firstOrNull {
            it.category.displayName == slice.label
        }

        val amount = matchingCategory?.total ?: 0.0

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
                "${slice.label} — $${amount.formatAmount()} (${slice.value.toInt()}%)",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = appColors.foreground
            )
        }
    }
}



/*                         BREAKDOWN LIST                          */

@Composable
fun BreakdownList(categoryTotals: List<CategoryTotal>) {
    val total = categoryTotals.sumOf { it.total }

    categoryTotals.forEach { item ->
        BreakdownRow(
            name = item.category.displayName,
            percent = if (total == 0.0) 0.0 else (item.total / total * 100),
            amount = item.total,
            color = getCategoryChartColor(item.category)
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
            text = "$${amount.formatAmount(2)}",
            color = appColors.foreground,
            fontWeight = FontWeight.Bold
        )
    }
}
