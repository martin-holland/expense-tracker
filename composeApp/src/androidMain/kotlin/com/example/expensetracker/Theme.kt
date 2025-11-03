package com.example.theme.com.example.expensetracker

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ==========================================
// Theme definitions extracted from globals.css
// ==========================================

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

    // Charts
    val chart1 = Color(0xFFD4843C)
    val chart2 = Color(0xFF45A7D9)
    val chart3 = Color(0xFF3F53B1)
    val chart4 = Color(0xFFE8D24A)
    val chart5 = Color(0xFFE3A345)
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
    val chart1 = Color(0xFF7A66E0)
    val chart2 = Color(0xFFA8C95B)
    val chart3 = Color(0xFFE3A345)
    val chart4 = Color(0xFFA05FF5)
    val chart5 = Color(0xFFC28148)
}

object AppTypography {
    val displayLarge = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Medium
    )
    val titleLarge = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Medium
    )
    val titleMedium = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium
    )
    val titleSmall = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium
    )
    val body = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal
    )
    val label = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    )
}
