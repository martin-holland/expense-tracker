package com.example.expensetracker.view.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.viewmodel.DashBoardViewModel
import com.example.theme.com.example.expensetracker.LocalAppColors
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.ExpenseCategory

enum class DashboardChartType {
    Category,
    Weekly,
    Trend
}

@Composable
fun DashboardScreen(
    viewModel: DashBoardViewModel = viewModel { DashBoardViewModel() }
) {
    val appColors = LocalAppColors.current
    val uiState by viewModel.uiState.collectAsState()

    var selectedChart by remember { mutableStateOf(DashboardChartType.Category) }

    // If still loading
    if (uiState.isLoading) {
        LoadingDashboard()
        return
    }

    // If there is an error
    if (uiState.error != null) {
        DashboardError(uiState.error)
        return
    }

    val monthly = uiState.monthlyAggregate
    val categories = uiState.categoryTotals
    val weekly = uiState.weeklyAggregates

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.background)
            .verticalScroll(rememberScrollState())
    ) {
        DashboardHeader()

        // Monthly Summary Card
        // Uses real aggregated numbers now
        MonthlySpendCard(
            spend = monthly?.totalExpenses ?: 0.0,
            month = uiState.currentMonth?.toString() ?: "—",
            currency = Currency.USD
        )

        ChartSelectionRow(
            selected = selectedChart,
            onSelect = { selectedChart = it }
        )


        // Dynamic Content (Charts)
        AnimatedContent(targetState = selectedChart) { chart ->
            when (chart) {

                DashboardChartType.Category ->
                    ExpenseBreakdownCard(
                        categoryTotals = uiState.categoryTotals
                    )




                DashboardChartType.Weekly ->
                    WeekBarCard(
                        expenses = uiState.expenses
                    )

                DashboardChartType.Trend ->
                    TrendPlaceholder()
            }
        }
    }
}
@Composable
fun LoadingDashboard() {
    val colors = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = colors.primary,
            strokeWidth = 3.dp
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Loading your dashboard...",
            color = colors.mutedForeground,
            fontSize = 16.sp
        )
    }
}


@Composable
fun DashboardError(message: String?) {
    val colors = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Oops!",
            color = colors.destructive,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = message ?: "Something went wrong.",
            color = colors.mutedForeground,
            fontSize = 16.sp
        )

        Spacer(Modifier.height(20.dp))

        OutlinedButton(
            onClick = { /* TODO: add retry callback if needed */ },
            border = BorderStroke(1.dp, colors.primary),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colors.primary
            )
        ) {
            Text("Retry")
        }
    }
}

