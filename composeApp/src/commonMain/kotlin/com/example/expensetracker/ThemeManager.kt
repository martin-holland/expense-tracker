package com.example.expensetracker

import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.StateFlow

data class ThemeState(
    val selectedThemeOption: String,
    val setThemeOption: (String) -> Unit
)

val LocalThemeState = compositionLocalOf<ThemeState> {
    error("No ThemeState provided")
}
