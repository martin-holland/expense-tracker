package com.example.theme.com.example.expensetracker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

/**
 * Extension property to easily access custom app colors from any composable
 * 
 * Usage:
 * ```kotlin
 * val appColors = LocalAppColors.current
 * Text("Hello", color = appColors.mutedForeground)
 * ```
 */
@Composable
fun appColors(): AppColorScheme = LocalAppColors.current

