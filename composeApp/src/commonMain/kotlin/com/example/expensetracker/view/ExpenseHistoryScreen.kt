package com.example.expensetracker.view

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.model.Expense
import com.example.expensetracker.view.components.DeleteConfirmationDialog
import com.example.expensetracker.view.components.EditExpenseDialog
import com.example.expensetracker.view.components.ExpenseFilterDialog
import com.example.expensetracker.view.components.SwipeableExpenseItem
import com.example.expensetracker.viewmodel.ExpenseHistoryViewModel
import com.example.theme.com.example.expensetracker.LocalAppColors
import kotlinx.datetime.LocalDate

/**
 * Main Expense History Screen
 * Displays a list of expenses grouped by date with swipe actions
 * Includes filtering capabilities, delete confirmation, and edit dialog
 */
@Composable
fun ExpenseHistoryScreen(
    viewModel: ExpenseHistoryViewModel = viewModel { ExpenseHistoryViewModel() },
    onNavigateBack: (() -> Unit)? = null
) {
    val appColors = LocalAppColors.current
    val uiState = viewModel.uiState
    
    // Get filtered and grouped expenses
    val filteredExpenses = viewModel.getFilteredExpenses()
    val groupedExpenses = groupExpensesByDate(filteredExpenses)
    
    // Scroll state for collapsing toolbar
    val listState = rememberLazyListState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.background)
            .statusBarsPadding() // Add padding for system status bar
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Collapsing Header
            CollapsingHeader(
                totalExpenses = filteredExpenses.size,
                hasActiveFilters = uiState.selectedCategories != null ||
                        uiState.dateRange != null ||
                        uiState.amountRange != null,
                onFilterClick = { viewModel.showFilterDialog() },
                isCollapsed = listState.firstVisibleItemIndex > 0
            )
            
            // Content
            if (filteredExpenses.isEmpty()) {
                // Empty state
                EmptyExpenseState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                )
            } else {
                // Expense list
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    groupedExpenses.forEach { (date, expenses) ->
                        // Date header
                        item(key = "header_$date") {
                            Text(
                                text = formatDate(date),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = appColors.foreground,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        // Expense items for this date
                        items(
                            items = expenses,
                            key = { it.id }
                        ) { expense ->
                            SwipeableExpenseItem(
                                expense = expense,
                                onEdit = { viewModel.openEditDialog(it) },
                                onDelete = { viewModel.requestDeleteExpense(it) }
                            )
                        }
                    }
                    
                    // Bottom hint
                    item {
                        Text(
                            text = "← Swipe expenses to reveal actions →",
                            style = MaterialTheme.typography.bodySmall,
                            color = appColors.mutedForeground,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        )
                    }
                }
            }
        }
    }
    
    // Filter Dialog
    if (uiState.showFilterDialog) {
        ExpenseFilterDialog(
            currentCategories = uiState.selectedCategories,
            currentDateRange = uiState.dateRange,
            currentAmountRange = uiState.amountRange,
            onApplyFilters = { categories, dateRange, amountRange ->
                viewModel.applyFilters(categories, dateRange, amountRange)
            },
            onClearFilters = { viewModel.clearFilters() },
            onDismiss = { viewModel.hideFilterDialog() }
        )
    }
    
    // Delete Confirmation Dialog
    if (uiState.showDeleteDialog && uiState.expenseToDelete != null) {
        DeleteConfirmationDialog(
            expense = uiState.expenseToDelete,
            onConfirm = { viewModel.confirmDeleteExpense() },
            onDismiss = { viewModel.cancelDeleteExpense() }
        )
    }
    
    // Edit Expense Dialog
    if (uiState.showEditDialog && uiState.expenseToEdit != null) {
        EditExpenseDialog(
            expense = uiState.expenseToEdit,
            onSave = { updatedExpense ->
                viewModel.saveExpense(updatedExpense)
            },
            onDismiss = { viewModel.closeEditDialog() }
        )
    }
}

/**
 * Collapsing header with smooth animation
 */
@Composable
private fun CollapsingHeader(
    totalExpenses: Int,
    hasActiveFilters: Boolean,
    onFilterClick: () -> Unit,
    isCollapsed: Boolean
) {
    val appColors = LocalAppColors.current
    
    // Animate header size based on collapsed state
    val headerPadding by animateDpAsState(
        targetValue = if (isCollapsed) 8.dp else 16.dp
    )
    val titleSize by animateFloatAsState(
        targetValue = if (isCollapsed) 0.8f else 1f
    )
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = appColors.background,
        shadowElevation = if (isCollapsed) 4.dp else 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = headerPadding, bottom = headerPadding)
        ) {
            // Header with icon and title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = if (isCollapsed) 0.dp else 12.dp)
            ) {
                // App icon
                Box(
                    modifier = Modifier
                        .size(48.dp * titleSize)
                        .clip(CircleShape)
                        .background(Color(0xFF00BCD4)), // Teal color
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = "App Icon",
                        tint = appColors.primaryForeground,
                        modifier = Modifier.size(24.dp * titleSize)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Expense History",
                        style = if (isCollapsed) 
                            MaterialTheme.typography.titleLarge 
                        else 
                            MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = appColors.foreground
                    )
                    if (!isCollapsed) {
                        Text(
                            text = "Expense Tracker",
                            style = MaterialTheme.typography.bodySmall,
                            color = appColors.mutedForeground
                        )
                    }
                }
                
                // Filter button (always visible)
                IconButton(
                    onClick = onFilterClick,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (hasActiveFilters) appColors.primary
                            else appColors.secondary
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = "Filter",
                        tint = if (hasActiveFilters) appColors.primaryForeground
                        else appColors.foreground
                    )
                }
            }
            
            // Total expenses count (hidden when collapsed)
            if (!isCollapsed) {
                Column {
                    Text(
                        text = "Total Expenses",
                        style = MaterialTheme.typography.labelMedium,
                        color = appColors.mutedForeground
                    )
                    Text(
                        text = "$totalExpenses",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = appColors.foreground
                    )
                }
            }
        }
    }
}

/**
 * Empty state when no expenses match filters
 */
@Composable
private fun EmptyExpenseState(modifier: Modifier = Modifier) {
    val appColors = LocalAppColors.current
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No Expenses Found",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = appColors.foreground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Try adjusting your filters or add some expenses to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = appColors.mutedForeground
        )
    }
}

/**
 * Groups expenses by date
 */
private fun groupExpensesByDate(expenses: List<Expense>): Map<LocalDate, List<Expense>> {
    return expenses.groupBy { it.date.date }
}

/**
 * Formats date for display
 * TODO: Implement localized date formatting based on device locale
 */
private fun formatDate(date: LocalDate): String {
    val monthNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    return "${monthNames[date.monthNumber - 1]} ${date.dayOfMonth}"
}
