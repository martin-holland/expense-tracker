package com.example.theme.com.example.expensetracker

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ==========================================
// Theme definitions extracted from globals.css
// ==========================================

/** Data class containing all custom theme colors for easy access */
data class AppColorScheme(
    val background: Color,
    val foreground: Color,
    val card: Color,
    val cardForeground: Color,
    val primary: Color,
    val primaryForeground: Color,
    val secondary: Color,
    val secondaryForeground: Color,
    val accent: Color,
    val accentForeground: Color,
    val destructive: Color,
    val destructiveForeground: Color,
    val muted: Color,
    val mutedForeground: Color,
    val border: Color,
    val inputBackground: Color,
    val switchBackground: Color,
    val chart1: Color,
    val chart2: Color,
    val chart3: Color,
    val chart4: Color,
    val chart5: Color,
    val testMain: Color,
    val testChart: Color,
    val testDark: Color,
)

object AppColors {
    val background = Color(0xFFFFFFFF)
    val foreground = Color(0xFF252525)
    val card = Color(0xFFFFFFFF)
    val cardForeground = Color(0xFF252525)
    val primary = Color(0xFF030213)
    val primaryForeground = Color(0xFFFFFFFF)
    val secondary = Color(0xFFF2F2FB)
    val secondaryForeground = Color(0xFF030213)
    val accent = Color(0xFFE9EBEF)
    val accentForeground = Color(0xFF030213)
    val destructive = Color(0xFFD4183D)
    val destructiveForeground = Color(0xFFFFFFFF)
    val muted = Color(0xFFECECF0)
    val mutedForeground = Color(0xFF717182)
    val border = Color(0x1A000000) // rgba(0,0,0,0.1)
    val inputBackground = Color(0xFFF3F3F5)
    val switchBackground = Color(0xFFCBCED4)
    val testMain = Color(0xFF3F53B1)

    // Charts
    val chart1 = Color(0xFFD4843C)
    val chart2 = Color(0xFF45A7D9)
    val chart3 = Color(0xFF3F53B1)
    val chart4 = Color(0xFFE8D24A)
    val chart5 = Color(0xFFE3A345)
    val testChart = Color(0xFFFF5733) // Orange-red test color
    val testDark = Color(0xFF9C27B0) // Purple test color
}

object DarkAppColors {
    val background = Color(0xFF252525)
    val foreground = Color(0xFFFBFBFB)
    val card = Color(0xFF252525)
    val cardForeground = Color(0xFFFBFBFB)
    val primary = Color(0xFFFBFBFB)
    val primaryForeground = Color(0xFF2E2E2E)
    val secondary = Color(0xFF454545)
    val secondaryForeground = Color(0xFFFBFBFB)
    val accent = Color(0xFF454545)
    val accentForeground = Color(0xFFFBFBFB)
    val destructive = Color(0xFFA83E2E)
    val destructiveForeground = Color(0xFFF28A7A)
    val mutedForeground = Color(0xFFB3B3B3)
    val chart1 = Color(0xFF6449EE)
    val chart2 = Color(0xFFA8C95B)
    val chart3 = Color(0xFFE3A345)
    val chart4 = Color(0xFFA05FF5)
    val chart5 = Color(0xFFC28148)
    val testMain = Color(0xFF6B7FDB) // Lighter blue for dark theme
    val testChart = Color(0xFFFF8A65) // Lighter orange for dark theme
    val testDark = Color(0xFFCE93D8) // Lighter purple for dark theme
}

// Create color scheme instances
val LightAppColorScheme =
    AppColorScheme(
        background = AppColors.background,
        foreground = AppColors.foreground,
        card = AppColors.card,
        cardForeground = AppColors.cardForeground,
        primary = AppColors.primary,
        primaryForeground = AppColors.primaryForeground,
        secondary = AppColors.secondary,
        secondaryForeground = AppColors.secondaryForeground,
        accent = AppColors.accent,
        accentForeground = AppColors.accentForeground,
        destructive = AppColors.destructive,
        destructiveForeground = AppColors.destructiveForeground,
        muted = AppColors.muted,
        mutedForeground = AppColors.mutedForeground,
        border = AppColors.border,
        inputBackground = AppColors.inputBackground,
        switchBackground = AppColors.switchBackground,
        chart1 = AppColors.chart1,
        chart2 = AppColors.chart2,
        chart3 = AppColors.chart3,
        chart4 = AppColors.chart4,
        chart5 = AppColors.chart5,
        testMain = AppColors.testMain,
        testChart = AppColors.testChart,
        testDark = AppColors.testDark
    )

val DarkAppColorScheme =
    AppColorScheme(
        background = DarkAppColors.background,
        foreground = DarkAppColors.foreground,
        card = DarkAppColors.card,
        cardForeground = DarkAppColors.cardForeground,
        primary = DarkAppColors.primary,
        primaryForeground = DarkAppColors.primaryForeground,
        secondary = DarkAppColors.secondary,
        secondaryForeground = DarkAppColors.secondaryForeground,
        accent = DarkAppColors.accent,
        accentForeground = DarkAppColors.accentForeground,
        destructive = DarkAppColors.destructive,
        destructiveForeground = DarkAppColors.destructiveForeground,
        muted = Color(0xFF454545), // Adding missing muted color for dark theme
        mutedForeground = DarkAppColors.mutedForeground,
        border = Color(0x33FFFFFF), // rgba(255,255,255,0.2) for dark theme
        inputBackground = Color(0xFF353535), // Dark theme equivalent
        switchBackground = Color(0xFF555555), // Dark theme equivalent
        chart1 = DarkAppColors.chart1,
        chart2 = DarkAppColors.chart2,
        chart3 = DarkAppColors.chart3,
        chart4 = DarkAppColors.chart4,
        chart5 = DarkAppColors.chart5,
        testMain = DarkAppColors.testMain,
        testChart = DarkAppColors.testChart,
        testDark = DarkAppColors.testDark,
    )

/** CompositionLocal to access custom app colors */
val LocalAppColors = compositionLocalOf { LightAppColorScheme }

object AppTypography {
    val displayLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Medium)
    val titleLarge = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Medium)
    val titleMedium = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium)
    val titleSmall = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium)
    val body = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal)
    val label = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium)
}