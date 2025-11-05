package com.example.expensetracker.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.ExpenseCategory
import kotlinx.datetime.LocalDateTime

/**
 * ViewModel for managing Expense History state
 * Follows MVVM architecture pattern
 */
class ExpenseHistoryViewModel : ViewModel() {

    // UI State
    var uiState by mutableStateOf(ExpenseHistoryUiState())
        private set

    init {
        loadMockData()
    }

    /**
     * Loads mock expense data for testing UI
     * TODO: Replace with actual data source (Room DB or API) in future implementation
     */
    private fun loadMockData() {
        val mockExpenses = generateMockExpenses()
        uiState = uiState.copy(expenses = mockExpenses)
    }

    /**
     * Filters expenses based on selected criteria
     * @param categories Selected categories to filter by (null = all categories)
     * @param dateRange Date range to filter by (null = all dates)
     * @param amountRange Amount range to filter by (null = all amounts)
     */
    fun applyFilters(
        categories: Set<ExpenseCategory>? = null,
        dateRange: Pair<LocalDateTime, LocalDateTime>? = null,
        amountRange: Pair<Double, Double>? = null
    ) {
        uiState = uiState.copy(
            selectedCategories = categories,
            dateRange = dateRange,
            amountRange = amountRange
        )
    }

    /**
     * Clears all active filters
     */
    fun clearFilters() {
        uiState = uiState.copy(
            selectedCategories = null,
            dateRange = null,
            amountRange = null
        )
    }

    /**
     * Shows the filter dialog
     */
    fun showFilterDialog() {
        uiState = uiState.copy(showFilterDialog = true)
    }

    /**
     * Hides the filter dialog
     */
    fun hideFilterDialog() {
        uiState = uiState.copy(showFilterDialog = false)
    }

    /**
     * Marks an expense for deletion and shows confirmation dialog
     * @param expense The expense to delete
     */
    fun requestDeleteExpense(expense: Expense) {
        uiState = uiState.copy(
            expenseToDelete = expense,
            showDeleteDialog = true
        )
    }

    /**
     * Confirms and executes expense deletion
     * TODO: Implement actual database deletion in future implementation
     */
    fun confirmDeleteExpense() {
        val expenseToDelete = uiState.expenseToDelete
        if (expenseToDelete != null) {
            val updatedExpenses = uiState.expenses.filter { it.id != expenseToDelete.id }
            uiState = uiState.copy(
                expenses = updatedExpenses,
                expenseToDelete = null,
                showDeleteDialog = false
            )
            // TODO: Call repository/database to persist deletion
        }
    }

    /**
     * Cancels expense deletion
     */
    fun cancelDeleteExpense() {
        uiState = uiState.copy(
            expenseToDelete = null,
            showDeleteDialog = false
        )
    }

    /**
     * Opens edit dialog for an expense
     * @param expense The expense to edit
     */
    fun openEditDialog(expense: Expense) {
        uiState = uiState.copy(
            expenseToEdit = expense,
            showEditDialog = true
        )
    }

    /**
     * Closes the edit dialog
     */
    fun closeEditDialog() {
        uiState = uiState.copy(
            expenseToEdit = null,
            showEditDialog = false
        )
    }

    /**
     * Saves or updates an expense
     * @param expense The expense to save
     * TODO: Implement actual database save in future implementation
     */
    fun saveExpense(expense: Expense) {
        val existingIndex = uiState.expenses.indexOfFirst { it.id == expense.id }
        val updatedExpenses = if (existingIndex >= 0) {
            // Update existing expense
            uiState.expenses.toMutableList().apply {
                set(existingIndex, expense)
            }
        } else {
            // Add new expense
            uiState.expenses + expense
        }
        uiState = uiState.copy(expenses = updatedExpenses)
        // TODO: Call repository/database to persist changes
    }

    /**
     * Gets filtered expenses based on current filter criteria
     */
    fun getFilteredExpenses(): List<Expense> {
        var filtered = uiState.expenses

        // Filter by categories
        uiState.selectedCategories?.let { categories ->
            if (categories.isNotEmpty()) {
                filtered = filtered.filter { it.category in categories }
            }
        }

        // Filter by date range
        uiState.dateRange?.let { (start, end) ->
            filtered = filtered.filter { it.date >= start && it.date <= end }
        }

        // Filter by amount range
        uiState.amountRange?.let { (min, max) ->
            filtered = filtered.filter { it.amount >= min && it.amount <= max }
        }

        // Sort by date, newest first
        return filtered.sortedByDescending { it.date }
    }

    /**
     * Generates mock expense data for testing
     * TODO: Remove when actual data source is implemented
     */
    private fun generateMockExpenses(): List<Expense> {
        // Create static dates for mock data (no Clock.System needed for iOS compatibility)
        val nov1 = LocalDateTime(2024, 11, 1, 12, 0)
        val oct31 = LocalDateTime(2024, 10, 31, 14, 30)
        val oct30 = LocalDateTime(2024, 10, 30, 10, 15)
        val oct29 = LocalDateTime(2024, 10, 29, 16, 45)
        val oct28 = LocalDateTime(2024, 10, 28, 9, 0)

        return listOf(
            Expense(
                id = "1",
                category = ExpenseCategory.FOOD,
                description = "Lunch at restaurant",
                amount = 45.50,
                currency = Currency.USD,
                date = nov1
            ),
            Expense(
                id = "2",
                category = ExpenseCategory.TRAVEL,
                description = "Gas station",
                amount = 120.00,
                currency = Currency.USD,
                date = nov1
            ),
            Expense(
                id = "3",
                category = ExpenseCategory.FOOD,
                description = "Coffee shop",
                amount = 15.99,
                currency = Currency.USD,
                date = oct31
            ),
            Expense(
                id = "4",
                category = ExpenseCategory.UTILITIES,
                description = "Electricity bill",
                amount = 85.00,
                currency = Currency.USD,
                date = oct31
            ),
            Expense(
                id = "5",
                category = ExpenseCategory.FOOD,
                description = "Grocery shopping",
                amount = 32.50,
                currency = Currency.USD,
                date = oct30
            ),
            Expense(
                id = "6",
                category = ExpenseCategory.TRAVEL,
                description = "Uber ride",
                amount = 50.00,
                currency = Currency.USD,
                date = oct30
            ),
            Expense(
                id = "7",
                category = ExpenseCategory.OTHER,
                description = "Online subscription",
                amount = 25.99,
                currency = Currency.EUR,
                date = oct29
            ),
            Expense(
                id = "8",
                category = ExpenseCategory.UTILITIES,
                description = "Internet bill",
                amount = 180.00,
                currency = Currency.USD,
                date = oct28
            )
        )
    }
}

/**
 * UI State for Expense History screen
 */
data class ExpenseHistoryUiState(
    val expenses: List<Expense> = emptyList(),
    val selectedCategories: Set<ExpenseCategory>? = null,
    val dateRange: Pair<LocalDateTime, LocalDateTime>? = null,
    val amountRange: Pair<Double, Double>? = null,
    val showFilterDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val expenseToDelete: Expense? = null,
    val expenseToEdit: Expense? = null
)

