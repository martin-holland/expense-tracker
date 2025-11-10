package com.example.expensetracker.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.repository.ExchangeRateRepository
import com.example.expensetracker.data.repository.SettingsRepository
import com.example.expensetracker.model.Currency
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ViewModel for managing Settings state
 * Follows MVVM architecture pattern
 * Uses SettingsRepository for data persistence and ExchangeRateRepository for API testing
 */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository = SettingsRepository.getInstance(),
    private val exchangeRateRepository: ExchangeRateRepository = ExchangeRateRepository.getInstance()
) : ViewModel() {

    // UI State
    var uiState by mutableStateOf(SettingsUiState())
        private set

    // State flows for reactive updates
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

    private val _lastExchangeRateUpdate = MutableStateFlow<String?>(null)
    val lastExchangeRateUpdate: StateFlow<String?> = _lastExchangeRateUpdate.asStateFlow()

    // Available currencies (all supported currencies)
    val availableCurrencies: List<Currency> = Currency.entries

    init {
        loadSettings()
        observeSettings()
    }

    /**
     * Observes settings changes from repository
     * Updates local state flows when settings change
     */
    private fun observeSettings() {
        // Observe base currency
        settingsRepository.getBaseCurrency()
            .onEach { currency ->
                _baseCurrency.value = currency
            }
            .catch { e ->
                _errorMessage.value = "Error loading base currency: ${e.message}"
            }
            .launchIn(viewModelScope)

        // Observe API key
        settingsRepository.getApiKey()
            .onEach { key ->
                _apiKey.value = key
                _isApiConfigured.value = key.isNotBlank()
            }
            .catch { e ->
                _errorMessage.value = "Error loading API key: ${e.message}"
            }
            .launchIn(viewModelScope)

        // Observe API base URL
        settingsRepository.getApiBaseUrl()
            .onEach { url ->
                _apiBaseUrl.value = url
            }
            .catch { e ->
                _errorMessage.value = "Error loading API base URL: ${e.message}"
            }
            .launchIn(viewModelScope)

        // Observe last exchange rate update
        settingsRepository.getLastExchangeRateUpdate()
            .onEach { timestamp ->
                _lastExchangeRateUpdate.value = timestamp?.let {
                    formatTimestamp(it)
                }
            }
            .catch { e ->
                // Ignore errors for optional field
            }
            .launchIn(viewModelScope)
    }

    /**
     * Loads settings from repository
     */
    fun loadSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val settings = settingsRepository.getSettingsSync()
                _baseCurrency.value = settings.baseCurrency
                _apiKey.value = settings.exchangeRateApiKey
                _apiBaseUrl.value = settings.exchangeRateApiBaseUrl
                _isApiConfigured.value = settings.isApiConfigured()
                _lastExchangeRateUpdate.value = settings.lastExchangeRateUpdate?.let {
                    formatTimestamp(it)
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Error loading settings: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Updates base currency
     * @param currency The currency to set as base currency
     */
    fun updateBaseCurrency(currency: Currency) {
        viewModelScope.launch {
            try {
                settingsRepository.setBaseCurrency(currency)
                // State will be updated via Flow observation
            } catch (e: Exception) {
                _errorMessage.value = "Error updating base currency: ${e.message}"
            }
        }
    }

    /**
     * Updates API key
     * @param apiKey The API key to set
     */
    fun updateApiKey(apiKey: String) {
        viewModelScope.launch {
            try {
                settingsRepository.setApiKey(apiKey)
                // State will be updated via Flow observation
            } catch (e: Exception) {
                _errorMessage.value = "Error updating API key: ${e.message}"
            }
        }
    }

    /**
     * Updates API base URL
     * @param baseUrl The API base URL to set
     */
    fun updateApiBaseUrl(baseUrl: String) {
        viewModelScope.launch {
            try {
                settingsRepository.setApiBaseUrl(baseUrl)
                // State will be updated via Flow observation
            } catch (e: Exception) {
                _errorMessage.value = "Error updating API base URL: ${e.message}"
            }
        }
    }

    /**
     * Tests API connection by attempting to refresh exchange rates
     * This uses ExchangeRateRepository which has a properly configured HTTP client
     */
    fun testApiConnection() {
        viewModelScope.launch {
            _isLoading.value = true
            _apiTestResult.value = null
            _errorMessage.value = null
            
            try {
                val currentApiKey = _apiKey.value
                val currentBaseCurrency = _baseCurrency.value
                
                if (currentApiKey.isBlank()) {
                    _apiTestResult.value = "Error: API key is required"
                    _isLoading.value = false
                    return@launch
                }
                
                // Test API connection by attempting to refresh rates
                // This will use the API key and base URL from settings
                val result = exchangeRateRepository.refreshExchangeRates(currentBaseCurrency)
                
                result.onSuccess {
                    // Get the count of rates that were fetched
                    val rates = exchangeRateRepository.getAllRatesForBase(currentBaseCurrency)
                    val ratesCount = rates.size
                    _apiTestResult.value = "Success: API connection successful. Base currency: ${currentBaseCurrency.code}, Rates fetched: $ratesCount"
                }.onFailure { error ->
                    _apiTestResult.value = "Error: ${error.message}"
                    _errorMessage.value = error.message
                }
                
                _isLoading.value = false
            } catch (e: Exception) {
                _apiTestResult.value = "Error: ${e.message}"
                _errorMessage.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Refreshes exchange rates from the API
     * Uses the current base currency and API configuration from settings
     */
    fun refreshExchangeRates() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _apiTestResult.value = null
            
            try {
                val currentBaseCurrency = _baseCurrency.value
                val result = exchangeRateRepository.refreshExchangeRates(currentBaseCurrency)
                
                result.onSuccess {
                    _apiTestResult.value = "Success: Exchange rates refreshed successfully"
                    // Reload settings to update last update timestamp
                    loadSettings()
                }.onFailure { error ->
                    _apiTestResult.value = "Error: ${error.message}"
                    _errorMessage.value = error.message
                }
                
                _isLoading.value = false
            } catch (e: Exception) {
                _apiTestResult.value = "Error: ${e.message}"
                _errorMessage.value = e.message
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

