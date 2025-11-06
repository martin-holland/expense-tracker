package com.example.expensetracker

import androidx.compose.runtime.Composable
import com.example.expensetracker.theme.ThemeProvider

@Composable
fun App() {
    ThemeProvider {
        AddExpenseScreen()
    }
}
