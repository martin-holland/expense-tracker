package com.example.expensetracker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensetracker.view.ExpenseHistoryScreen
import com.example.theme.com.example.expensetracker.LocalAppColors
import com.example.theme.com.example.expensetracker.ThemeProvider
import expensetracker.composeapp.generated.resources.Res
import expensetracker.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/** Enum representing different screens in the app Useful for navigation management */
enum class AppScreen {
    HOME,
    EXPENSE_HISTORY,
    DASHBOARD
}

@Composable
@Preview
fun App() {
    ThemeProvider { AppContent() }
}

@Composable
fun AppContent() {
    val appColors = LocalAppColors.current
    // Navigation state - controls which screen is shown
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

    when (currentScreen) {
        AppScreen.HOME ->
                HomeScreen(
                        onNavigateToExpenseHistory = { currentScreen = AppScreen.EXPENSE_HISTORY }
                )
        AppScreen.EXPENSE_HISTORY ->
                ExpenseHistoryScreen(onNavigateBack = { currentScreen = AppScreen.HOME })
        AppScreen.DASHBOARD ->

    }
}

/** Home screen with navigation to Expense History */
@Composable
private fun HomeScreen(onNavigateToExpenseHistory: () -> Unit) {
    val appColors = LocalAppColors.current
    var showContent by remember { mutableStateOf(false) }

    Column(
            modifier =
                    Modifier.background(appColors.background)
                            .safeContentPadding()
                            .fillMaxSize()
                            .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
                text = "Expense Tracker",
                style = MaterialTheme.typography.headlineLarge,
                color = appColors.foreground
        )

        // Navigation button to Expense History
        Button(onClick = onNavigateToExpenseHistory, modifier = Modifier.fillMaxWidth()) {
            Text("View Expense History")
        }

        // Original demo content
        Button(onClick = { showContent = !showContent }) { Text("Toggle Demo Content") }

        AnimatedVisibility(showContent) {
            val greeting = remember { Greeting().greet() }
            Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(painterResource(Res.drawable.compose_multiplatform), null)
                Text("Compose: $greeting", color = appColors.mutedForeground)
            }
        }
    }
}
