package com.example.expensetracker.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensetracker.model.ExpenseCategory
import com.example.theme.com.example.expensetracker.LocalAppColors
import kotlinx.datetime.LocalDateTime

/**
 * Dialog component for filtering expenses
 * @param currentCategories Currently selected categories
 * @param currentDateRange Currently selected date range
 * @param currentAmountRange Currently selected amount range
 * @param onApplyFilters Callback when filters are applied
 * @param onClearFilters Callback when filters are cleared
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
fun ExpenseFilterDialog(
    currentCategories: Set<ExpenseCategory>?,
    currentDateRange: Pair<LocalDateTime, LocalDateTime>?,
    currentAmountRange: Pair<Double, Double>?,
    onApplyFilters: (Set<ExpenseCategory>?, Pair<LocalDateTime, LocalDateTime>?, Pair<Double, Double>?) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = LocalAppColors.current
    
    // Local state for filter selections
    var selectedCategories by remember { mutableStateOf(currentCategories ?: emptySet()) }
    // TODO: Implement date range picker in future implementation
    // TODO: Implement amount range slider in future implementation
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Filter Expenses",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category Filter Section
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                ExpenseCategory.values().forEach { category ->
                    val isSelected = category in selectedCategories
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                selectedCategories = if (isSelected) {
                                    selectedCategories - category
                                } else {
                                    selectedCategories + category
                                }
                            }
                            .background(
                                if (isSelected) appColors.secondary
                                else appColors.background
                            )
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(category.backgroundColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = category.displayName,
                                    tint = appColors.foreground
                                )
                            }
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Selected",
                                tint = appColors.primary
                            )
                        }
                    }
                }
                
                HorizontalDivider(color = appColors.border)
                
                // Date Range Filter Section
                // TODO: Implement date range picker
                Text(
                    text = "Date Range",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Date range filtering will be implemented in a future update",
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.mutedForeground
                )
                
                HorizontalDivider(color = appColors.border)
                
                // Amount Range Filter Section
                // TODO: Implement amount range slider
                Text(
                    text = "Amount Range",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Amount range filtering will be implemented in a future update",
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.mutedForeground
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApplyFilters(
                        if (selectedCategories.isEmpty()) null else selectedCategories,
                        null, // TODO: Pass date range when implemented
                        null  // TODO: Pass amount range when implemented
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = appColors.primary,
                    contentColor = appColors.primaryForeground
                )
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = {
                    onClearFilters()
                    onDismiss()
                }) {
                    Text("Clear All", color = appColors.destructive)
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = appColors.foreground)
                }
            }
        },
        containerColor = appColors.card
    )
}

