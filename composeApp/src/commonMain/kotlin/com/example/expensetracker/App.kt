package com.example.expensetracker

import androidx.compose.runtime.*
import com.example.expensetracker.view.SettingsScreen
import com.example.theme.com.example.expensetracker.ThemeProvider
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    ThemeProvider {
        SettingsScreen()
    }

}