package com.example.expensetracker.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Enum representing different expense categories
 * Each category has an associated icon and color
 */
enum class ExpenseCategory(
    val displayName: String,
    val icon: ImageVector,
    val backgroundColor: Color
) {
    FOOD(
        displayName = "Food",
        icon = Icons.Filled.Restaurant,
        backgroundColor = Color(0xFFFFE5E5)
    ),
    TRAVEL(
        displayName = "Travel",
        icon = Icons.Filled.DirectionsCar,
        backgroundColor = Color(0xFFE5F6FF)
    ),
    UTILITIES(
        displayName = "Utilities",
        icon = Icons.Filled.Bolt,
        backgroundColor = Color(0xFFE5F9FF)
    ),
    OTHER(
        displayName = "Other",
        icon = Icons.Filled.MoreHoriz,
        backgroundColor = Color(0xFFFFF9E5)
    );

    companion object {
        fun fromDisplayName(name: String): ExpenseCategory {
            return values().find { it.displayName.equals(name, ignoreCase = true) } ?: OTHER
        }
    }
}

