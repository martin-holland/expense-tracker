package com.example.expensetracker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.expensetracker.view.DashboardScreen
import com.example.expensetracker.view.ExpenseHistoryScreen
import com.example.theme.com.example.expensetracker.LocalAppColors
import com.example.theme.com.example.expensetracker.ThemeProvider
import expensetracker.composeapp.generated.resources.Res
import expensetracker.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/** Enum representing different screens in the app with navigation management */
enum class AppScreen(val title: String, val icon: ImageVector) {
    DASHBOARD("Dashboard", Icons.Filled.Dashboard),
    ADD("Add", Icons.Filled.Add),
    HISTORY("History", Icons.Filled.History),
    SETTINGS("Settings", Icons.Filled.Settings)
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
    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }

    Scaffold(
            containerColor = appColors.background,
            bottomBar = {
                BottomNavigationBar(
                        currentScreen = currentScreen,
                        onScreenSelected = { screen -> currentScreen = screen }
                )
            }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (currentScreen) {
                AppScreen.DASHBOARD ->DashboardScreen()
                AppScreen.ADD -> BlankScreen("Add")
                AppScreen.HISTORY -> ExpenseHistoryScreen()
                AppScreen.SETTINGS -> BlankScreen("Settings")
            }
        }
    }
}

/** Bottom Navigation Bar */
@Composable
fun BottomNavigationBar(currentScreen: AppScreen, onScreenSelected: (AppScreen) -> Unit) {
    val appColors = LocalAppColors.current

    NavigationBar(
            containerColor = appColors.card,
            contentColor = appColors.foreground,
            tonalElevation = 8.dp
    ) {
        AppScreen.entries.forEach { screen ->
            NavigationBarItem(
                    icon = { Icon(imageVector = screen.icon, contentDescription = screen.title) },
                    label = { Text(screen.title) },
                    selected = currentScreen == screen,
                    onClick = { onScreenSelected(screen) },
                    colors =
                            NavigationBarItemDefaults.colors(
                                    selectedIconColor = appColors.chart2,
                                    selectedTextColor = appColors.chart2,
                                    unselectedIconColor = appColors.mutedForeground,
                                    unselectedTextColor = appColors.mutedForeground,
                                    indicatorColor = appColors.secondary
                            )
            )
        }
    }
}

/** Blank screen placeholder for screens that don't have content yet */
@Composable
fun BlankScreen(@Suppress("UNUSED_PARAMETER") screenName: String) {
    val appColors = LocalAppColors.current

    Box(
            modifier = Modifier.fillMaxSize().background(appColors.background),
            contentAlignment = Alignment.Center
    ) {
        // Empty screen - just background color
    }
}
