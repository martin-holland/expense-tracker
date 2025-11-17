package com.example.theme.com.example.expensetracker

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

//  CUSTOM APP COLOR SCHEME (LIGHT + DARK)

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


// =====================================================
//  LIGHT THEME COLORS
// =====================================================

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

    val border = Color(0x1A000000)
    val inputBackground = Color(0xFFF3F3F5)
    val switchBackground = Color(0xFFCBCED4)

    val testMain = Color(0xFF3F53B1)

    val chart1 = Color(0xFFD4843C)
    val chart2 = Color(0xFF45A7D9)
    val chart3 = Color(0xFF3F53B1)
    val chart4 = Color(0xFFE8D24A)
    val chart5 = Color(0xFFE3A345)

    val testChart = Color(0xFFFF5733)
    val testDark = Color(0xFF9C27B0)
}


//  DARK THEME COLORS

object DarkAppColors {

    // Core backgrounds
    val background = Color(0xFF121212)
    val card = Color(0xFF1E1E1E)
    val inputBackground = Color(0xFF1A1A1A)
    val border = Color(0x33FFFFFF)

    // Primary
    val primary = Color(0xFF00BFAE)
    val primaryForeground = Color(0xFF0D1514)

    // Text
    val foreground = Color(0xFFEDEDED)
    val cardForeground = Color(0xFF000000)
    val muted = Color(0xFF2A2A2A)
    val mutedForeground = Color(0xFF9E9E9E)

    // Secondary
    val secondary = Color(0xFF262626)
    val secondaryForeground = Color(0xFFEDEDED)

    // Accent
    val accent = Color(0xFF2D2D2D)
    val accentForeground = Color(0xFFEDEDED)

    // Switch
    val switchBackground = Color(0xFF3A3A3A)

    // Destructive
    val destructive = Color(0xFFD65A6F)
    val destructiveForeground = Color(0xFF450A12)

    // Dark-optimized chart colors
    val chart1 = Color(0xFF5FDBD0)
    val chart2 = Color(0xFFE87F7F)
    val chart3 = Color(0xFFF5D96F)
    val chart4 = Color(0xFF7EB6FF)
    val chart5 = Color(0xFF9D7AFF)

    // Test colors
    val testMain = Color(0xFF6B7FDB)
    val testChart = Color(0xFFFF8A65)
    val testDark = Color(0xFFCE93D8)
}



//  LIGHT SCHEME INSTANCE

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


//  DARK SCHEME INSTANCE

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
        muted = DarkAppColors.muted,
        mutedForeground = DarkAppColors.mutedForeground,
        border = DarkAppColors.border,
        inputBackground = DarkAppColors.inputBackground,
        switchBackground = DarkAppColors.switchBackground,
        chart1 = DarkAppColors.chart1,
        chart2 = DarkAppColors.chart2,
        chart3 = DarkAppColors.chart3,
        chart4 = DarkAppColors.chart4,
        chart5 = DarkAppColors.chart5,
        testMain = DarkAppColors.testMain,
        testChart = DarkAppColors.testChart,
        testDark = DarkAppColors.testDark
    )


//  COMPOSITION LOCAL

val LocalAppColors = compositionLocalOf { LightAppColorScheme }



//  TYPOGRAPHY
object AppTypography {
    val displayLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Medium)
    val titleLarge = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Medium)
    val titleMedium = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium)
    val titleSmall = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium)
    val body = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal)
    val label = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium)
}
