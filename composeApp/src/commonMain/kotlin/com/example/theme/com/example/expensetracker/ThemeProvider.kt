package com.example.theme.com.example.expensetracker

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun ThemeProvider(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val appColorScheme = if (useDarkTheme) {
        DarkAppColorScheme
    } else {
        LightAppColorScheme
    }

    val colors = if (useDarkTheme) {
        darkColorScheme(
            background = DarkAppColors.background,
            surface = DarkAppColors.card,
            primary = DarkAppColors.primary,
            onPrimary = DarkAppColors.primaryForeground,
            secondary = DarkAppColors.secondary,
            onSecondary = DarkAppColors.secondaryForeground,
            error = DarkAppColors.destructive,
            onError = DarkAppColors.destructiveForeground
        )
    } else {
        lightColorScheme(
            background = AppColors.background,
            surface = AppColors.card,
            primary = AppColors.primary,
            onPrimary = AppColors.primaryForeground,
            secondary = AppColors.secondary,
            onSecondary = AppColors.secondaryForeground,
            error = AppColors.destructive,
            onError = AppColors.destructiveForeground
        )
    }

    CompositionLocalProvider(LocalAppColors provides appColorScheme) {
        MaterialTheme(
            colorScheme = colors,
            typography = Typography(
                displayLarge = AppTypography.displayLarge,
                titleLarge = AppTypography.titleLarge,
                titleMedium = AppTypography.titleMedium,
                titleSmall = AppTypography.titleSmall,
                bodyLarge = AppTypography.body,
                bodyMedium = AppTypography.body,
                labelLarge = AppTypography.label
            ),
            content = content
        )
    }
}

