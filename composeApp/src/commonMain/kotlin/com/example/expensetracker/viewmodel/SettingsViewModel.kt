package com.example.expensetracker.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.model.Currency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing Settings state
 * Follows MVVM architecture pattern
 * Initially uses mock data, will be updated to use SettingsRepository later
 */
class SettingsViewModel : ViewModel() {

    // UI State
    var uiState by mutableStateOf(SettingsUiState())
        private set

    // State flows for reactive updates (mock initially)
    private val _baseCurrency = MutableStateFlow<Currency>(Currency.USD)
    val baseCurrency: StateFlow<Currency> = _baseCurrency.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _apiKey = MutableStateFlow<String>("")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _apiBaseUrl = MutableStateFlow<String>("https://v6.exchangerate-api.com/v6")
    val apiBaseUrl: StateFlow<String> = _apiBaseUrl.asStateFlow()

    private val _isApiConfigured = MutableStateFlow<Boolean>(false)
    val isApiConfigured: StateFlow<Boolean> = _isApiConfigured.asStateFlow()

    private val _apiTestResult = MutableStateFlow<String?>(null)
    val apiTestResult: StateFlow<String?> = _apiTestResult.asStateFlow()

    // Available currencies (all supported currencies)
    val availableCurrencies: List<Currency> = Currency.entries

    init {
        loadSettings()
    }

    /**
     * Loads settings (mock implementation)
     * Will be replaced with SettingsRepository integration later
     */
    fun loadSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Mock: Load default settings
                _baseCurrency.value = Currency.USD
                _apiKey.value = ""
                _apiBaseUrl.value = "https://v6.exchangerate-api.com/v6"
                _isApiConfigured.value = false
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Updates base currency (mock implementation)
     * Will be replaced with SettingsRepository integration later
     * @param currency The currency to set as base currency
     */
    fun updateBaseCurrency(currency: Currency) {
        viewModelScope.launch {
            try {
                _baseCurrency.value = currency
                // Mock: Update local state only
                println("Base currency updated to: ${currency.code}")
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    /**
     * Updates API key (mock implementation)
     * Will be replaced with SettingsRepository integration later
     * @param apiKey The API key to set
     */
    fun updateApiKey(apiKey: String) {
        viewModelScope.launch {
            try {
                _apiKey.value = apiKey
                _isApiConfigured.value = apiKey.isNotBlank()
                // Mock: Update local state only
                println("API key updated (length: ${apiKey.length})")
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    /**
     * Updates API base URL (mock implementation)
     * Will be replaced with SettingsRepository integration later
     * @param baseUrl The API base URL to set
     */
    fun updateApiBaseUrl(baseUrl: String) {
        viewModelScope.launch {
            try {
                _apiBaseUrl.value = baseUrl
                // Mock: Update local state only
                println("API base URL updated to: $baseUrl")
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    /**
     * Tests API connection (mock implementation)
     * Will be replaced with real API service integration later
     */
    fun testApiConnection() {
        viewModelScope.launch {
            _isLoading.value = true
            _apiTestResult.value = null
            try {
                // Mock: Simulate API test
                kotlinx.coroutines.delay(1000) // Simulate network delay
                
                if (_apiKey.value.isBlank()) {
                    _apiTestResult.value = "Error: API key is required"
                } else {
                    _apiTestResult.value = "Success: Connection test passed (mock)"
                    println("API connection test completed (mock)")
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _apiTestResult.value = "Error: ${e.message}"
                _errorMessage.value = e.message
                _isLoading.value = false
            }
        }
    }
}

/** UI State for Settings screen */
data class SettingsUiState(
    val baseCurrency: Currency = Currency.USD,
    val apiKey: String = "",
    val apiBaseUrl: String = "https://v6.exchangerate-api.com/v6",
    val isApiConfigured: Boolean = false,
    val lastExchangeRateUpdate: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

