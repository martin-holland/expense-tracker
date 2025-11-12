package com.example.expensetracker.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.ExpenseCategory
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime

/**
 * ViewModel for managing Expense History state Follows MVVM architecture pattern Uses Repository
 * pattern to access data from Room database
 */
class ExpenseHistoryViewModel(
        private val repository: ExpenseRepository = ExpenseRepository.getInstance()
) : ViewModel() {

    // UI State
    var uiState by mutableStateOf(ExpenseHistoryUiState())
        private set

    init {
        loadExpenses()
    }

    /**
     * Loads expenses from the repository Observes the database and updates UI state when data
     * changes
     */
    private fun loadExpenses() {
        viewModelScope.launch {
            repository
                    .getAllExpenses()
                    .catch { exception ->
                        // Handle error - for now just log it
                        println("Error loading expenses: ${exception.message}")
                        uiState =
                                uiState.copy(
                                        expenses = emptyList(),
                                        isLoading = false,
                                        error = exception.message
                                )
                    }
                    .collect { expenses ->
                        uiState = uiState.copy(expenses = expenses, isLoading = false, error = null)
                    }
        }
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
        uiState =
                uiState.copy(
                        selectedCategories = categories,
                        dateRange = dateRange,
                        amountRange = amountRange
                )
    }

    /** Clears all active filters */
    fun clearFilters() {
        uiState = uiState.copy(selectedCategories = null, dateRange = null, amountRange = null)
    }

    /** Shows the filter dialog */
    fun showFilterDialog() {
        uiState = uiState.copy(showFilterDialog = true)
    }

    /** Hides the filter dialog */
    fun hideFilterDialog() {
        uiState = uiState.copy(showFilterDialog = false)
    }

    /**
     * Marks an expense for deletion and shows confirmation dialog
     * @param expense The expense to delete
     */
    fun requestDeleteExpense(expense: Expense) {
        uiState = uiState.copy(expenseToDelete = expense, showDeleteDialog = true)
    }

    /** Confirms and executes expense deletion Deletes the expense from the database */
    fun confirmDeleteExpense() {
        val expenseToDelete = uiState.expenseToDelete
        if (expenseToDelete != null) {
            viewModelScope.launch {
                try {
                    repository.deleteExpense(expenseToDelete)
                    // UI will update automatically through the Flow
                    uiState = uiState.copy(expenseToDelete = null, showDeleteDialog = false)
                } catch (e: Exception) {
                    println("Error deleting expense: ${e.message}")
                    uiState = uiState.copy(error = e.message)
                }
            }
        }
    }

    /** Cancels expense deletion */
    fun cancelDeleteExpense() {
        uiState = uiState.copy(expenseToDelete = null, showDeleteDialog = false)
    }

    /**
     * Opens edit dialog for an expense
     * @param expense The expense to edit
     */
    fun openEditDialog(expense: Expense) {
        uiState = uiState.copy(expenseToEdit = expense, showEditDialog = true)
    }

    /** Closes the edit dialog */
    fun closeEditDialog() {
        uiState = uiState.copy(expenseToEdit = null, showEditDialog = false)
    }

    /**
     * Saves or updates an expense
     * @param expense The expense to save Persists changes to the database
     */
    fun saveExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                repository.insertExpense(expense)
                // UI will update automatically through the Flow
                closeEditDialog()
            } catch (e: Exception) {
                println("Error saving expense: ${e.message}")
                uiState = uiState.copy(error = e.message)
            }
        }
    }

    /** Gets filtered expenses based on current filter criteria */
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
}

/** UI State for Expense History screen */
data class ExpenseHistoryUiState(
        val expenses: List<Expense> = emptyList(),
        val selectedCategories: Set<ExpenseCategory>? = null,
        val dateRange: Pair<LocalDateTime, LocalDateTime>? = null,
        val amountRange: Pair<Double, Double>? = null,
        val showFilterDialog: Boolean = false,
        val showDeleteDialog: Boolean = false,
        val showEditDialog: Boolean = false,
        val expenseToDelete: Expense? = null,
        val expenseToEdit: Expense? = null,
        val isLoading: Boolean = true,
        val error: String? = null
)
