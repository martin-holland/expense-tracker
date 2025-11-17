package com.example.expensetracker.view.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.model.Expense
import kotlin.time.Clock
import kotlinx.datetime.*
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime
import network.chaintech.cmpcharts.axis.AxisData
import network.chaintech.cmpcharts.common.model.Point
import network.chaintech.cmpcharts.ui.barchart.BarChart
import network.chaintech.cmpcharts.ui.barchart.models.BarChartData
import network.chaintech.cmpcharts.ui.barchart.models.BarData
import network.chaintech.cmpcharts.ui.barchart.models.BarStyle
import kotlin.time.ExperimentalTime

/* ─────────────── WEEKLY CARD ─────────────── */

@Composable
fun WeekBarCard(
    expenses: List<Expense>
) {
    var weekType by remember { mutableStateOf("this") }

    val shown = remember(expenses, weekType) {
        if (weekType == "this") {
            getExpensesForWeek(expenses, 0)
        } else {
            getExpensesForWeek(expenses, -1)
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Weekly Overview", fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(8.dp))

            Row {
                listOf("this" to "This Week", "last" to "Last Week").forEach { (key, name) ->
                    Button(
                        onClick = { weekType = key },
                        modifier = Modifier.padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (weekType == key) Color(0xFF00BFAE) else Color(0xFFF0F0F0),
                            contentColor = if (weekType == key) Color.White else Color.Black
                        )
                    ) {
                        Text(name, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            BarChartExample(
                expenses = shown,
                isLastWeek = weekType == "last"
            )
        }
    }
}

/* ─────────────── BAR CHART ─────────────── */

@Composable
fun BarChartExample(
    expenses: List<Expense>,
    isLastWeek: Boolean
) {
    val data = getBarChartData(expenses)
    val maxY = (data.maxOfOrNull { it.point.y } ?: 0f).coerceAtLeast(10f)

    val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    val xAxis = AxisData.Builder()
        .steps(7)
        .startDrawPadding(24.dp)
        .bottomPadding(0.dp)            // keep bars touching x-axis
        .axisLabelColor(Color.Gray)
        .axisLineColor(Color.Gray)
        .labelData { index ->
            weekDays.getOrNull(index).orEmpty()
        }
        .build()

    val yAxis = AxisData.Builder()
        .steps(5)
        .startDrawPadding(24.dp)
        .bottomPadding(0.dp)
        .axisLabelColor(Color.Gray)
        .axisLineColor(Color.Gray)
        .labelData { step ->
            val value = (maxY / 5f) * step
            value.toInt().toString()
        }
        .build()

    val barColor = if (isLastWeek) {
        Color(0xFF009688)
    } else {
        Color(0xFF009688)
    }

    val barData = data.map { it.copy(color = barColor) }

    BarChart(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        barChartData = BarChartData(
            chartData = barData,
            xAxisData = xAxis,
            yAxisData = yAxis,
            barStyle = BarStyle(
                barWidth = 30.dp,
                cornerRadius = 6.dp,
                paddingBetweenBars = 12.dp
            ),
            backgroundColor = Color.Transparent
        )
    )
}

/* ─────────────── DATA HELPERS (SAME LOGIC) ─────────────── */

@OptIn(ExperimentalTime::class)
fun getExpensesForWeek(
    list: List<Expense>,
    offset: Int
): List<Expense> {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val monday = today.minus(DatePeriod(days = today.dayOfWeek.isoDayNumber - 1))
    val start = monday.plus(DatePeriod(days = offset * 7))
    val end = start.plus(DatePeriod(days = 6))
    return list.filter { it.date.date in start..end }
}

fun getBarChartData(
    expenses: List<Expense>
): List<BarData> {
    val days = (1..7)
    val grouped = expenses
        .groupBy { it.date.date.dayOfWeek.isoDayNumber }
        .mapValues { (_, v) -> v.sumOf { it.amount } }

    val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    return days.map { d ->
        BarData(
            point = Point(
                x = d.toFloat(),
                y = (grouped[d] ?: 0.0).toFloat()
            ),
            label = labels[d - 1],
            color = Color(0xFF009688)
        )
    }
}
