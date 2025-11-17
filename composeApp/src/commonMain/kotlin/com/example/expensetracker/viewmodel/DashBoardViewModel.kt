package com.example.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.model.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class DashBoardUiState(
    val expenses: List<Expense> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class DashBoardViewModel : ViewModel() {

    private val repository = ExpenseRepository.getInstance()

    private val _uiState = MutableStateFlow(DashBoardUiState())
    val uiState: StateFlow<DashBoardUiState> = _uiState.asStateFlow()

    init {
        loadExpensesFromDb()
    }

    private fun loadExpensesFromDb() {
        viewModelScope.launch {
            try {
                repository.getAllExpenses().collectLatest { expenses ->
                    _uiState.value = DashBoardUiState(
                        expenses = expenses,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = DashBoardUiState(
                    expenses = emptyList(),
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}
