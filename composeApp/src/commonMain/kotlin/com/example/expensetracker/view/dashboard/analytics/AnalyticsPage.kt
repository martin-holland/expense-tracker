package com.example.expensetracker.view.dashboard.analytics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.domain.analytics.CategoryTotal
import com.example.expensetracker.domain.analytics.WeeklyAggregate
import com.example.expensetracker.viewmodel.DashBoardViewModel
import com.example.expensetracker.view.dashboard.DashboardError
import com.example.expensetracker.view.dashboard.LoadingDashboard
import com.example.expensetracker.view.dashboard.formatAmount
import com.example.theme.com.example.expensetracker.LocalAppColors

@Composable
fun AnalyticsPage(
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
    val categories = uiState.categoryTotals
    val weekly = uiState.weeklyAggregates
    val daily = uiState.dailyAggregates
    val momChange = uiState.monthOverMonthChange

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Analytics",
            style = MaterialTheme.typography.titleLarge,
            color = colors.foreground
        )

        Spacer(Modifier.height(12.dp))

        // Monthly summary
        AnalyticsSummaryCard(
            total = monthly?.totalExpenses ?: 0.0,
            txCount = monthly?.transactionCount ?: 0,
            avgDaily = monthly?.averageDaily ?: 0.0,
            avgWeekly = monthly?.averageWeekly ?: 0.0,
            momChange = momChange
        )

        Spacer(Modifier.height(16.dp))

        // Top categories
        TopCategoriesCard(categories)

        Spacer(Modifier.height(16.dp))

        // Weekly breakdown
        WeeklyBreakdownCard(weekly)

        Spacer(Modifier.height(16.dp))

        // Daily stats
        val maxDaily = daily.maxByOrNull { it.total }

        DailyStatsCard(
            dailyCount = daily.size,
            maxDay = maxDaily?.date.toString(),
            maxDayAmount = maxDaily?.total
        )

    }
}

/* ─────────────── SUMMARY CARD ─────────────── */

@Composable
private fun AnalyticsSummaryCard(
    total: Double,
    txCount: Int,
    avgDaily: Double,
    avgWeekly: Double,
    momChange: Double?
) {
    val colors = LocalAppColors.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Monthly Summary",
                color = colors.foreground,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total spent", color = colors.mutedForeground, fontSize = 12.sp)
                    Text("$${total.formatAmount(2)}", color = colors.foreground, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Transactions", color = colors.mutedForeground, fontSize = 12.sp)
                    Text("$txCount", color = colors.foreground, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Avg / day", color = colors.mutedForeground, fontSize = 12.sp)
                    Text("$${avgDaily.formatAmount(2)}", color = colors.foreground, fontSize = 16.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Avg / week", color = colors.mutedForeground, fontSize = 12.sp)
                    Text("$${avgWeekly.formatAmount(2)}", color = colors.foreground, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(8.dp))

            momChange?.let { change ->
                val changeText = when {
                    change > 0 -> "+${change.formatAmount(1)}%"
                    change < 0 -> "${change.formatAmount(1)}%"
                    else -> "${change.formatAmount(1)}%"
                }

                val changeColor =
                    if (change > 0) LocalAppColors.current.destructive
                    else LocalAppColors.current.primary

                Text(
                    text = "Month-over-month: $changeText",
                    color = changeColor,
                    fontSize = 14.sp
                )
            } ?: run {
                Text(
                    text = "Month-over-month: No previous data",
                    color = LocalAppColors.current.mutedForeground,
                    fontSize = 14.sp
                )
            }

        }
    }
}

/* ─────────────── TOP CATEGORIES CARD ─────────────── */

@Composable
private fun TopCategoriesCard(
    categories: List<CategoryTotal>
) {
    val colors = LocalAppColors.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Top Categories",
                color = colors.foreground,
                fontWeight = FontWeight.SemiBold
            )

            if (categories.isEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("No data yet.", color = colors.mutedForeground)
                return@Column
            }

            Spacer(Modifier.height(8.dp))

            categories
                .sortedByDescending { it.total }
                .take(5)
                .forEachIndexed { index, cat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${index + 1}. ${cat.category}", color = colors.foreground)
                        Text(
                            "$${cat.total.formatAmount(2)} (${cat.percent.formatAmount(1)}%)",
                            color = colors.mutedForeground
                        )
                    }
                }
        }
    }
}

/* ─────────────── WEEKLY BREAKDOWN CARD ─────────────── */

@Composable
private fun WeeklyBreakdownCard(
    weekly: List<WeeklyAggregate>
) {
    val colors = LocalAppColors.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Weekly Breakdown",
                color = colors.foreground,
                fontWeight = FontWeight.SemiBold
            )

            if (weekly.isEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("No weekly data.", color = colors.mutedForeground)
                return@Column
            }

            Spacer(Modifier.height(8.dp))

            weekly.sortedBy { it.weekOfMonth }.forEach { week ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Week ${week.weekOfMonth}", color = colors.foreground)
                    Text(
                        "$${week.total.formatAmount(2)}",
                        color = colors.mutedForeground
                    )
                }
            }
        }
    }
}

/* ─────────────── DAILY STATS CARD ─────────────── */

@Composable
private fun DailyStatsCard(
    dailyCount: Int,
    maxDay: String?,
    maxDayAmount: Double?
) {
    val colors = LocalAppColors.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(colors.card),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Daily Insights",
                color = colors.foreground,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Days with spending: $dailyCount",
                color = colors.foreground
            )

            if (maxDay != null && maxDayAmount != null) {
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Highest spending day:",
                    color = colors.mutedForeground
                )

                Text(
                    text = "$maxDay — $${maxDayAmount.toTwoDecimals()}",
                    color = colors.foreground,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
fun Double.toTwoDecimals(): String {
    val scaled = kotlin.math.round(this * 100) / 100
    return scaled.toString()
}

