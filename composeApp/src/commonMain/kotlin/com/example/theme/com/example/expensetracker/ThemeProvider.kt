package com.example.theme.com.example.expensetracker

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.model.ThemeOption
import com.example.expensetracker.viewmodel.SettingsViewModel

@Composable
fun ThemeProvider(
    settingsViewModel: SettingsViewModel = viewModel(),
    content: @Composable () -> Unit
) {
    val selectedThemeOption by settingsViewModel.selectedThemeOption.collectAsState()
    val systemDark = isSystemInDarkTheme()

    val useDarkTheme = when (selectedThemeOption) {
        ThemeOption.LIGHT -> false
        ThemeOption.DARK -> true
        ThemeOption.SYSTEM -> systemDark
    }

    val appColorScheme =
        if (useDarkTheme) DarkAppColorScheme
        else LightAppColorScheme

    val materialColors =
        if (useDarkTheme)
            darkColorScheme(
                background = DarkAppColors.background,
                surface = DarkAppColors.card,
                primary = DarkAppColors.primary,
                onPrimary = DarkAppColors.primaryForeground,
                secondary = DarkAppColors.secondary,
                onSecondary = DarkAppColors.secondaryForeground,
                error = DarkAppColors.destructive,
                onError = DarkAppColors.destructiveForeground,
                onBackground = DarkAppColors.foreground,
                onSurface = DarkAppColors.cardForeground,
                outline = DarkAppColors.border,
                surfaceVariant = DarkAppColors.inputBackground
            )
        else
            lightColorScheme(
                background = AppColors.background,
                surface = AppColors.card,
                primary = AppColors.primary,
                onPrimary = AppColors.primaryForeground,
                secondary = AppColors.secondary,
                onSecondary = AppColors.secondaryForeground,
                error = AppColors.destructive,
                onError = AppColors.destructiveForeground,
                onBackground = AppColors.foreground,
                onSurface = AppColors.cardForeground,
                outline = AppColors.border,
                surfaceVariant = AppColors.inputBackground
            )

    CompositionLocalProvider(LocalAppColors provides appColorScheme) {
        MaterialTheme(
            colorScheme = materialColors,
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
