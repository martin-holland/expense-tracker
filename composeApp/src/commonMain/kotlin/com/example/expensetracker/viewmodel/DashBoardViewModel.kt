package com.example.expensetracker.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.data.repository.SettingsRepository
import com.example.expensetracker.domain.CurrencyConverter
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.ExpenseWithConversion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ViewModel for managing Dashboard state
 * Follows MVVM architecture pattern
 * Uses Repository pattern to access data from Room database
 */
class DashBoardViewModel(
    private val repository: ExpenseRepository = ExpenseRepository.getInstance(),
    private val currencyConverter: CurrencyConverter = CurrencyConverter.getInstance(),
    private val settingsRepository: SettingsRepository = SettingsRepository.getInstance()
) : ViewModel() {
    
    var uiState by mutableStateOf(DashBoardUiState())
        private set

    // Converted expenses with currency conversion
    private val _convertedExpenses = MutableStateFlow<List<ExpenseWithConversion>>(emptyList())
    val convertedExpenses: StateFlow<List<ExpenseWithConversion>> = _convertedExpenses.asStateFlow()

    // Base currency for display
    private val _baseCurrency = MutableStateFlow<Currency>(Currency.USD)
    val baseCurrency: StateFlow<Currency> = _baseCurrency.asStateFlow()

    init {
        loadExpenses()
        observeBaseCurrency()
    }

    /**
     * Loads expenses from the repository
     * Observes the database and updates UI state when data changes
     */
    private fun loadExpenses() {
        viewModelScope.launch {
            repository.getAllExpenses()
                .catch { exception ->
                    // Handle error - for now just log it
                    println("Error loading expenses: ${exception.message}")
                    uiState = uiState.copy(
                        expenses = emptyList(),
                        isLoading = false,
                        error = exception.message
                    )
                    _convertedExpenses.value = emptyList()
                }
                .collect { expenses ->
                    uiState = uiState.copy(
                        expenses = expenses,
                        isLoading = false,
                        error = null
                    )
                    // Convert expenses when list changes
                    convertExpenses(expenses)
                }
        }
    }

    /**
     * Observes base currency changes and updates UI
     */
    private fun observeBaseCurrency() {
        settingsRepository.getBaseCurrency()
            .onEach { currency ->
                _baseCurrency.value = currency
                println("💱 Base currency changed to: ${currency.code}, reconverting expenses...")
                // Reconvert expenses when base currency changes
                viewModelScope.launch { convertExpenses(uiState.expenses) }
            }
            .catch { e -> println("Error observing base currency: ${e.message}") }
            .launchIn(viewModelScope)
    }

    /**
     * Converts expenses to base currency for accurate calculations
     */
    private suspend fun convertExpenses(expenses: List<Expense>? = null) {
        val expensesToConvert = expenses ?: uiState.expenses
        val baseCurrency = settingsRepository.getBaseCurrencySync()

        println("💱 Converting ${expensesToConvert.size} expenses to base currency: ${baseCurrency.code}")

        val converted = expensesToConvert.map { expense ->
            val convertedAmount = if (expense.currency == baseCurrency) {
                expense.amount
            } else {
                currencyConverter.convertAmountSync(
                    amount = expense.amount,
                    fromCurrency = expense.currency,
                    toCurrency = baseCurrency,
                    date = expense.date
                ) ?: expense.amount // Fallback to original amount if conversion fails
            }

            ExpenseWithConversion(
                expense = expense,
                convertedAmount = convertedAmount,
                baseCurrency = baseCurrency
            )
        }

        _convertedExpenses.value = converted
        println("💱 Conversion complete. Emitting ${converted.size} converted expenses")
    }
}

data class DashBoardUiState(
    val expenses: List<Expense> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)