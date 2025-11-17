package com.example.expensetracker.view.dashboard


import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.ExpenseCategory
import com.example.expensetracker.viewmodel.DashBoardViewModel
import com.example.theme.com.example.expensetracker.LocalAppColors

/* ─────────────── CHART TYPE MODEL ─────────────── */

enum class DashboardChartType {
    Category,
    Weekly,
    Trend
}

/* ─────────────── DASHBOARD ENTRY POINT ─────────────── */

@Composable
fun DashboardScreen(
    viewModel: DashBoardViewModel = viewModel { DashBoardViewModel() }
) {
    val appColors = LocalAppColors.current
    val uiState by viewModel.uiState.collectAsState()

    // Local screen state (same logic as before, just type-safe)
    var selectedChart by remember { mutableStateOf(DashboardChartType.Category) }

    // Derived values: SAME LOGIC as before
    val totalSpent by remember(uiState.expenses) {
        mutableStateOf(uiState.expenses.sumOf { it.amount })
    }

    val categorySumMap by remember(uiState.expenses) {
        mutableStateOf(
            uiState.expenses
                .groupBy { it.category }
                .mapValues { (_, list) -> list.sumOf { it.amount } }
                .toSortedMap(compareBy<ExpenseCategory> { it.displayName })
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.background)
            .verticalScroll(rememberScrollState())
    ) {
        DashboardHeader()

        MonthlySpendCard(
            spend = totalSpent,
            month = "November 2025",
            currency = Currency.USD
        )

        ChartSelectionRow(
            selected = selectedChart,
            onSelect = { selectedChart = it }
        )

        AnimatedContent(targetState = selectedChart) { chart ->
            when (chart) {
                DashboardChartType.Category ->
                    ExpenseBreakdownCard(categorySum = categorySumMap)

                DashboardChartType.Weekly ->
                    WeekBarCard(expenses = uiState.expenses)

                DashboardChartType.Trend ->
                    TrendPlaceholder()
            }
        }
    }
}
