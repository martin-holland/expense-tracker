package com.example.expensetracker

import androidx.compose.ui.window.ComposeUIViewController
import com.example.theme.com.example.expensetracker.ThemeProvider

fun MainViewController() = ComposeUIViewController { 
    ThemeProvider {
        AppContent()
    }
}