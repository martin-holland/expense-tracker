package com.example.expensetracker.view.dashboard.analytics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.domain.analytics.DailyAggregate
import com.example.expensetracker.model.ExpenseCategory
import com.example.expensetracker.viewmodel.DashBoardViewModel
import com.example.expensetracker.view.dashboard.DashboardError
import com.example.expensetracker.view.dashboard.LoadingDashboard
import com.example.expensetracker.view.dashboard.formatAmount
import com.example.expensetracker.view.dashboard.getCategoryChartColor
import com.example.theme.com.example.expensetracker.LocalAppColors


enum class TrendChartMode {
    Line, Stacked
}

@Composable
fun TrendScreen(
    viewModel: DashBoardViewModel = viewModel { DashBoardViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalAppColors.current

    if (uiState.isLoading) {
        LoadingDashboard()
        return
    }
    if (uiState.error != null) {
        DashboardError(uiState.error)
        return
    }

    val monthly = uiState.monthlyAggregate
    val daily = uiState.dailyAggregates
    val momChange = uiState.monthOverMonthChange

    var selectedMode by remember { mutableStateOf(TrendChartMode.Line) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Spending Trends",
            style = MaterialTheme.typography.titleLarge,
            color = colors.foreground
        )

        Spacer(Modifier.height(12.dp))

        TrendSummaryRow(
            total = monthly?.totalExpenses ?: 0.0,
            averageDaily = monthly?.averageDaily ?: 0.0,
            averageWeekly = monthly?.averageWeekly ?: 0.0,
            monthOverMonthChange = momChange
        )

        Spacer(Modifier.height(16.dp))

        TrendChartModeSelector(
            selected = selectedMode,
            onSelect = { selectedMode = it }
        )

        Spacer(Modifier.height(12.dp))

        DailyTrendChart(
            mode = selectedMode,
            data = daily
        )
    }
}

@Composable
fun TrendChartModeSelector(
    selected: TrendChartMode,
    onSelect: (TrendChartMode) -> Unit
) {
    val colors = LocalAppColors.current

    // Updated modes list → Stacked replaces Area
    val modes = listOf(
        TrendChartMode.Line to "Line",
        TrendChartMode.Stacked to "Stacked"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        modes.forEach { (mode, label) ->
            val isSelected = selected == mode

            Button(
                onClick = { onSelect(mode) },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) colors.primary else colors.secondary,
                    contentColor = if (isSelected) colors.primaryForeground else colors.secondaryForeground
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) colors.primary else colors.border
                )
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}


@Composable
fun DailyTrendChart(
    mode: TrendChartMode,
    data: List<DailyAggregate>
) {
    val colors = LocalAppColors.current

    if (data.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(colors.card),
            contentAlignment = Alignment.Center
        ) {
            Text("No data for this month yet.", color = colors.mutedForeground)
        }
        return
    }

    val maxY = (data.maxOf { it.total }.toFloat() * 1.2f).coerceAtLeast(10f)
    val yStep = maxY / 4f
    val xLabels = data.map { it.date.day.toString() }

    // =============================
    // STACKED MODE (FIXED VERSION)
    // =============================
    if (mode == TrendChartMode.Stacked) {

        val categoryColors = ExpenseCategory.entries.associateWith { getCategoryChartColor(it) }

        val animatedMaxY = animateFloatAsState(
            targetValue = maxY,
            animationSpec = tween(650)
        ).value

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(colors.card)
        ) {

            // ---- Y-AXIS (LEFT) ----
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
                    .fillMaxHeight()
                    .padding(bottom = 40.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(5) { i ->
                    Text(
                        text = ((yStep * (4 - i))).toInt().toString(),
                        color = colors.foreground,
                        fontSize = 11.sp
                    )
                }
            }

            // ---- CANVAS (CENTER) ----
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .padding(start = 40.dp, end = 20.dp, top = 12.dp, bottom = 52.dp)
            ) {
                val width = size.width
                val height = size.height

                val barWidth = width / (data.size * 1.6f)
                val gap = barWidth * 0.6f

                val dashed = floatArrayOf(12f, 12f)
                val gridColor = colors.accent.copy(alpha = 0.35f)

                // horizontal grid
                repeat(5) { i ->
                    val y = height - (height / 4f * i)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(dashed)
                    )
                }

                // draw stacked bars
                data.forEachIndexed { index, day ->
                    val xLeft = index * (barWidth + gap)
                    var currentTop = height

                    ExpenseCategory.entries.forEach { category ->
                        val value = day.categoryTotals[category] ?: 0.0
                        if (value <= 0) return@forEach

                        val segHeight = ((value.toFloat() / animatedMaxY) * height)
                            .coerceAtLeast(4.dp.toPx())

                        val top = currentTop - segHeight
                        val color = categoryColors[category]!!

                        drawRoundRect(
                            color = color,
                            topLeft = Offset(xLeft, top),
                            size = Size(barWidth, segHeight),
                            cornerRadius = CornerRadius(10f)
                        )

                        currentTop = top
                    }
                }
            }

            // ---- X-AXIS LABELS ----
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 40.dp, end = 20.dp, bottom = 4.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                xLabels.forEach {
                    Text(it, color = colors.foreground, fontSize = 11.sp)
                }
            }
        }

        // ---- LEGEND ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            ExpenseCategory.entries.forEach { category ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(12.dp)
                            .background(getCategoryChartColor(category))
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        category.displayName,
                        color = colors.foreground,
                        fontSize = 12.sp
                    )
                }
            }
        }

        return
    }

    // =============================
    // LINE MODE (FIXED VERSION)
    // =============================
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(colors.card)
    ) {

        // ---- Y-AXIS ----
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
                .padding(bottom = 32.dp)
                .height(200.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(5) { i ->
                Text(
                    text = ((yStep * (4 - i))).toInt().toString(),
                    color = colors.foreground,
                    fontSize = 11.sp
                )
            }
        }

        // ---- CANVAS ----
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .padding(start = 40.dp, end = 12.dp, top = 12.dp, bottom = 32.dp)
        ) {
            val width = size.width
            val height = size.height
            val xStep = if (data.size > 1) width / (data.size - 1) else 0f

            val points = data.mapIndexed { idx, day ->
                val x = xStep * idx
                val y = height - (day.total.toFloat() / maxY * height)
                Offset(x, y)
            }

            val dashed = floatArrayOf(10f, 10f)
            val gridColor = colors.accent.copy(alpha = 0.4f)

            // horizontal grid
            repeat(5) { i ->
                val y = height - (height / 4f * i)
                drawLine(
                    gridColor,
                    Offset(0f, y),
                    Offset(width, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(dashed)
                )
            }

            // line path
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val p0 = points[i - 1]
                    val p1 = points[i]
                    val mx = (p0.x + p1.x) / 2f
                    val my = (p0.y + p1.y) / 2f
                    quadraticBezierTo(p0.x, p0.y, mx, my)
                    quadraticBezierTo(mx, my, p1.x, p1.y)
                }
            }

            drawPath(path, colors.primary, style = Stroke(4f))

            points.forEach { p -> drawCircle(colors.primary, 5f, p) }
        }

        // ---- X-AXIS ----
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 40.dp, end = 12.dp, bottom = 4.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            xLabels.forEach {
                Text(it, color = colors.foreground, fontSize = 11.sp)
            }
        }
    }
}






/* ───────────── SUMMARY ROW (FIXED) ───────────── */

@Composable
private fun TrendSummaryRow(
    total: Double,
    averageDaily: Double,
    averageWeekly: Double,
    monthOverMonthChange: Double?
) {
    val colors = LocalAppColors.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnalyticsStatCard(
            title = "This Month",
            value = "$${total.formatAmount(2)}",
            modifier = Modifier.weight(1f)
        )
        AnalyticsStatCard(
            title = "Avg / Day",
            value = "$${averageDaily.formatAmount(2)}",
            modifier = Modifier.weight(1f)
        )
        AnalyticsStatCard(
            title = "Avg / Week",
            value = "$${averageWeekly.formatAmount(2)}",
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(Modifier.height(12.dp))

    monthOverMonthChange?.let { change ->
        val isIncrease = change > 0
        val sign = if (isIncrease) "+" else ""
        val color = if (isIncrease) colors.destructive else colors.primary

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(colors.card),
            border = BorderStroke(1.dp, colors.border)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Month-over-month",
                    color = colors.mutedForeground,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$sign${change.formatAmount(1)}%",
                    color = color,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun AnalyticsStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, color = colors.mutedForeground, fontSize = 12.sp)
            Text(value, color = colors.foreground, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
