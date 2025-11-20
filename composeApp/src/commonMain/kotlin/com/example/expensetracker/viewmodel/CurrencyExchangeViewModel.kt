package com.example.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.repository.ExchangeRateRepository
import com.example.expensetracker.data.repository.IExchangeRateRepository
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.data.repository.IExpenseRepository
import com.example.expensetracker.data.repository.SettingsRepository
import com.example.expensetracker.data.repository.ISettingsRepository
import com.example.expensetracker.domain.CurrencyConverter
import com.example.expensetracker.domain.ICurrencyConverter
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.ExpenseWithConversion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ViewModel for Currency Exchange Screen
 * 
 * Manages state for displaying exchange rates, converted expenses, and handling rate refreshes.
 * Uses CurrencyConverter, SettingsRepository, ExchangeRateRepository, and ExpenseRepository.
 */
class CurrencyExchangeViewModel(
    private val currencyConverter: ICurrencyConverter = CurrencyConverter.getInstance(),
    private val settingsRepository: ISettingsRepository = SettingsRepository.getInstance(),
    private val exchangeRateRepository: IExchangeRateRepository = ExchangeRateRepository.getInstance(),
    private val expenseRepository: IExpenseRepository = ExpenseRepository.getInstance()
) : ViewModel() {

    // Base currency
    private val _baseCurrency = MutableStateFlow<Currency>(Currency.USD)
    val baseCurrency: StateFlow<Currency> = _baseCurrency.asStateFlow()

    // Expenses with converted amounts
    private val _expensesWithConversion = MutableStateFlow<List<ExpenseWithConversion>>(emptyList())
    val expensesWithConversion: StateFlow<List<ExpenseWithConversion>> = _expensesWithConversion.asStateFlow()

    // Exchange rates (for display)
    private val _exchangeRates = MutableStateFlow<Map<Currency, Double>>(emptyMap())
    val exchangeRates: StateFlow<Map<Currency, Double>> = _exchangeRates.asStateFlow()

    // Last update timestamp
    private val _lastUpdateTime = MutableStateFlow<String?>(null)
    val lastUpdateTime: StateFlow<String?> = _lastUpdateTime.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        observeBaseCurrency()
        observeExpenses()
        observeLastUpdate()
        loadExchangeRates()
        // Initial conversion of expenses
        viewModelScope.launch {
            convertExpenses()
        }
    }

    /**
     * Observes base currency changes from settings
     */
    private fun observeBaseCurrency() {
        settingsRepository.getBaseCurrency()
            .onEach { currency ->
                _baseCurrency.value = currency
                // Recalculate conversions when base currency changes
                viewModelScope.launch {
                    convertExpenses()
                    // Reload exchange rates display (may need cross-rate calculation)
                    loadExchangeRates()
                }
            }
            .catch { e ->
                _errorMessage.value = "Error loading base currency: ${e.message}"
            }
            .launchIn(viewModelScope)
    }

    /**
     * Observes expenses from repository
     */
    private fun observeExpenses() {
        expenseRepository.getAllExpenses()
            .onEach { expenses ->
                // Convert expenses when list changes
                viewModelScope.launch {
                    convertExpenses(expenses)
                }
            }
            .catch { e ->
                _errorMessage.value = "Error loading expenses: ${e.message}"
            }
            .launchIn(viewModelScope)
    }

    /**
     * Observes last exchange rate update timestamp
     */
    private fun observeLastUpdate() {
        settingsRepository.getLastExchangeRateUpdate()
            .onEach { timestamp ->
                _lastUpdateTime.value = timestamp?.let { formatTimestamp(it) }
            }
            .catch { e ->
                // Ignore errors for optional field
            }
            .launchIn(viewModelScope)
    }

    /**
     * Converts expenses to base currency
     */
    private suspend fun convertExpenses(expenses: List<Expense>? = null) {
        val expensesToConvert = if (expenses != null) {
            expenses
        } else {
            expenseRepository.getAllExpenses().first()
        }
        val base = _baseCurrency.value

        val converted = expensesToConvert.map { expense ->
            val convertedAmount = if (expense.currency == base) {
                expense.amount
            } else {
                currencyConverter.convertAmountSync(
                    amount = expense.amount,
                    fromCurrency = expense.currency,
                    toCurrency = base,
                    date = expense.date
                )
            }

            ExpenseWithConversion(
                expense = expense,
                convertedAmount = convertedAmount,
                baseCurrency = base
            )
        }

        _expensesWithConversion.value = converted
    }

    /**
     * Loads exchange rates for display
     * Shows rates for currencies that are in the supported Currency enum
     * 
     * Note: This tries to load rates for the current base currency.
     * If rates aren't available for the current base, it will try to calculate
     * them using cross-rates from any available base currency in the database.
     */
    private fun loadExchangeRates() {
        viewModelScope.launch {
            try {
                val base = _baseCurrency.value
                
                // Always use cross-rate calculation to ensure accuracy
                // This ensures we use the latest rates from the stored base currency (USD)
                // rather than potentially stale rates stored with the current base currency
                val calculatedRates = calculateRatesViaCrossRate(base)
                
                // Use calculated rates (they're more accurate as they use cross-rates from stored base)
                _exchangeRates.value = calculatedRates
            } catch (e: Exception) {
                // Silently fail - rates might not be available yet
                println("Error loading exchange rates: ${e.message}")
            }
        }
    }
    
    /**
     * Calculates exchange rates for a base currency using cross-rates
     * from any available base currency in the database
     */
    private suspend fun calculateRatesViaCrossRate(baseCurrency: Currency): Map<Currency, Double> {
        val rates = mutableMapOf<Currency, Double>()
        
        // Try to get rates for each supported currency using cross-rate calculation
        Currency.entries.forEach { targetCurrency ->
            if (targetCurrency != baseCurrency) {
                val rate = exchangeRateRepository.getExchangeRateSync(
                    baseCurrency = baseCurrency,
                    targetCurrency = targetCurrency
                )
                if (rate != null) {
                    rates[targetCurrency] = rate
                }
            } else {
                // Base currency to itself is always 1.0
                rates[targetCurrency] = 1.0
            }
        }
        
        return rates
    }

    /**
     * Refreshes exchange rates from the API
     */
    fun refreshExchangeRates() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val base = _baseCurrency.value
                val result = exchangeRateRepository.refreshExchangeRates(base)

                result.onSuccess {
                    // Reload rates after successful refresh
                    loadExchangeRates()
                    // Update last update timestamp (will be updated via observeLastUpdate flow)
                }.onFailure { error ->
                    _errorMessage.value = "Failed to refresh rates: ${error.message}"
                }

                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Error refreshing rates: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Formats a LocalDateTime to a readable string
     */
    private fun formatTimestamp(timestamp: kotlinx.datetime.LocalDateTime): String {
        return "${timestamp.date} ${timestamp.hour}:${timestamp.minute.toString().padStart(2, '0')}"
    }
}

