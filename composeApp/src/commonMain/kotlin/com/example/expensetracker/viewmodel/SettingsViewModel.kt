package com.example.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.repository.ExchangeRateRepository
import com.example.expensetracker.data.repository.SettingsRepository
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.ThemeOption
import com.example.expensetracker.service.getMicrophoneService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime

/**
 * ViewModel for managing application settings.
 *
 * Combines persisted settings (base currency, API credentials) with UI-only preferences (theme,
 * voice input) required by the Settings screen.
 */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository = SettingsRepository.getInstance(),
    private val exchangeRateRepository: ExchangeRateRepository = ExchangeRateRepository.getInstance()
) : ViewModel() {

    private val _baseCurrency = MutableStateFlow(Currency.USD)
    val baseCurrency: StateFlow<Currency> = _baseCurrency.asStateFlow()
    val selectedCurrency: StateFlow<Currency> = baseCurrency

    private val _isVoiceInputEnabled = MutableStateFlow(false)
    val isVoiceInputEnabled: StateFlow<Boolean> = _isVoiceInputEnabled.asStateFlow()

    private val _selectedThemeOption = MutableStateFlow(ThemeOption.SYSTEM)
    val selectedThemeOption: StateFlow<ThemeOption> = _selectedThemeOption.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _apiKey = MutableStateFlow("")
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _apiBaseUrl = MutableStateFlow("https://v6.exchangerate-api.com/v6")
    val apiBaseUrl: StateFlow<String> = _apiBaseUrl.asStateFlow()

    private val _isApiConfigured = MutableStateFlow(false)
    val isApiConfigured: StateFlow<Boolean> = _isApiConfigured.asStateFlow()

    private val _apiTestResult = MutableStateFlow<String?>(null)
    val apiTestResult: StateFlow<String?> = _apiTestResult.asStateFlow()

    private val _lastExchangeRateUpdate = MutableStateFlow<String?>(null)
    val lastExchangeRateUpdate: StateFlow<String?> = _lastExchangeRateUpdate.asStateFlow()

    val availableCurrencies: List<Currency> = Currency.entries

    init {
        loadSettings()
        observeSettings()
    }

    private fun observeSettings() {
        settingsRepository.getBaseCurrency()
            .onEach { currency ->
                _baseCurrency.value = currency
            }
            .catch { e ->
                _errorMessage.value = "Error loading base currency: ${e.message}"
            }
            .launchIn(viewModelScope)

        settingsRepository.getApiKey()
            .onEach { key ->
                _apiKey.value = key
                _isApiConfigured.value = key.isNotBlank()
            }
            .catch { e ->
                _errorMessage.value = "Error loading API key: ${e.message}"
            }
            .launchIn(viewModelScope)

        settingsRepository.getApiBaseUrl()
            .onEach { url ->
                _apiBaseUrl.value = url
            }
            .catch { e ->
                _errorMessage.value = "Error loading API base URL: ${e.message}"
            }
            .launchIn(viewModelScope)

        settingsRepository.getLastExchangeRateUpdate()
            .onEach { timestamp ->
                _lastExchangeRateUpdate.value = timestamp?.let { formatTimestamp(it) }
            }
            .catch { _ ->
                // Optional field - ignore errors.
            }
            .launchIn(viewModelScope)
        
        settingsRepository.getThemeOption()
            .onEach { themeOption ->
                _selectedThemeOption.value = themeOption
            }
            .catch { e ->
                _errorMessage.value = "Error loading theme option: ${e.message}"
            }
            .launchIn(viewModelScope)
        
        settingsRepository.getVoiceInputEnabled()
            .onEach { isEnabled ->
                _isVoiceInputEnabled.value = isEnabled
            }
            .catch { e ->
                _errorMessage.value = "Error loading voice input setting: ${e.message}"
            }
            .launchIn(viewModelScope)
    }

    fun loadSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val settings = settingsRepository.getSettingsSync()
                _baseCurrency.value = settings.baseCurrency
                _apiKey.value = settings.exchangeRateApiKey
                _apiBaseUrl.value = settings.exchangeRateApiBaseUrl
                _isApiConfigured.value = settings.isApiConfigured()
                _lastExchangeRateUpdate.value = settings.lastExchangeRateUpdate?.let { formatTimestamp(it) }
                _selectedThemeOption.value = settings.themeOption
                _isVoiceInputEnabled.value = settings.isVoiceInputEnabled
            } catch (e: Exception) {
                _errorMessage.value = "Error loading settings: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateBaseCurrency(currency: Currency) {
        val previous = _baseCurrency.value
        if (previous == currency) return
        _baseCurrency.value = currency
        viewModelScope.launch {
            try {
                settingsRepository.setBaseCurrency(currency)
            } catch (e: Exception) {
                _baseCurrency.value = previous
                _errorMessage.value = "Error updating base currency: ${e.message}"
            }
        }
    }

    fun updateApiKey(apiKey: String) {
        viewModelScope.launch {
            try {
                settingsRepository.setApiKey(apiKey)
            } catch (e: Exception) {
                _errorMessage.value = "Error updating API key: ${e.message}"
            }
        }
    }

    fun updateApiBaseUrl(baseUrl: String) {
        viewModelScope.launch {
            try {
                settingsRepository.setApiBaseUrl(baseUrl)
            } catch (e: Exception) {
                _errorMessage.value = "Error updating API base URL: ${e.message}"
            }
        }
    }

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
                    return@launch
                }

                val result = exchangeRateRepository.refreshExchangeRates(currentBaseCurrency)

                result.onSuccess {
                    val rates = exchangeRateRepository.getAllRatesForBase(currentBaseCurrency)
                    _apiTestResult.value = "Success: API connection successful. Base currency: ${currentBaseCurrency.code}, Rates fetched: ${rates.size}"
                }.onFailure { error ->
                    _apiTestResult.value = "Error: ${error.message}"
                    _errorMessage.value = error.message
                }
            } catch (e: Exception) {
                _apiTestResult.value = "Error: ${e.message}"
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

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
                    loadSettings()
                }.onFailure { error ->
                    _apiTestResult.value = "Error: ${error.message}"
                    _errorMessage.value = error.message
                }
            } catch (e: Exception) {
                _apiTestResult.value = "Error: ${e.message}"
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private val _hasMicrophonePermission = MutableStateFlow(false)
    val hasMicrophonePermission: StateFlow<Boolean> = _hasMicrophonePermission

    fun checkMicrophonePermission() {
        _hasMicrophonePermission.value = getMicrophoneService().hasMicrophonePermission()
        println("🔊 ViewModel - Microphone permission: ${_hasMicrophonePermission.value}")

        if (_hasMicrophonePermission.value) {
            _isVoiceInputEnabled.value = true
            println("🔊 Auto-enabling voice input (permission granted)")
        }
    }

    fun toggleVoiceInput(enabled: Boolean) {
        if (enabled && !_hasMicrophonePermission.value) {
            println("🔊 No permission - requesting microphone access")
            getMicrophoneService().requestMicrophonePermission()
            return
        }
        val previous = _isVoiceInputEnabled.value
        _isVoiceInputEnabled.value = enabled
        viewModelScope.launch {
            try {
                settingsRepository.setVoiceInputEnabled(enabled)
                println("🔊 Voice input setting saved to database: $enabled")
            } catch (e: Exception) {
                _isVoiceInputEnabled.value = previous
                _errorMessage.value = "Error saving voice input setting: ${e.message}"
            }
        }
    }

    fun setThemeOption(option: ThemeOption) {
        val previous = _selectedThemeOption.value
        _selectedThemeOption.value = option
        viewModelScope.launch {
            try {
                settingsRepository.setThemeOption(option)
                println("🎨 Theme option saved to database: $option")
            } catch (e: Exception) {
                _selectedThemeOption.value = previous
                _errorMessage.value = "Error saving theme option: ${e.message}"
            }
        }
    }

    fun setCurrency(currency: Currency) {
        updateBaseCurrency(currency)
    }

    private fun formatTimestamp(timestamp: LocalDateTime): String {
        val minutes = timestamp.minute.toString().padStart(2, '0')
        return "${timestamp.date} ${timestamp.hour}:$minutes"
    }
}
