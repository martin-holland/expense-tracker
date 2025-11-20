package com.example.expensetracker.fakes

import com.example.expensetracker.data.repository.ISettingsRepository
import com.example.expensetracker.model.AppSettings
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.ThemeOption
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

/**
 * Fake implementation of ISettingsRepository for testing
 * 
 * This fake maintains in-memory settings and provides full control
 * over behavior for comprehensive testing.
 */
class FakeSettingsRepository : ISettingsRepository {
    
    // In-memory settings state
    private val _settings = MutableStateFlow(AppSettings())
    
    // Separate StateFlows for each property to ensure reactivity
    private val _baseCurrency = MutableStateFlow(_settings.value.baseCurrency)
    private val _apiKey = MutableStateFlow(_settings.value.exchangeRateApiKey)
    private val _apiBaseUrl = MutableStateFlow(_settings.value.exchangeRateApiBaseUrl)
    private val _lastUpdate = MutableStateFlow(_settings.value.lastExchangeRateUpdate)
    private val _themeOption = MutableStateFlow(_settings.value.themeOption)
    private val _voiceInputEnabled = MutableStateFlow(_settings.value.isVoiceInputEnabled)
    
    // Test control flags
    var shouldThrowError = false
    var errorMessage = "Test error"
    var delayMs = 0L
    
    // Test observability
    var getSettingsCalled = 0
    var getSettingsSyncCalled = 0
    var updateBaseCurrencyCalled = 0
    var updateApiKeyCalled = 0
    var saveSettingsCalled = 0
    var lastSavedSettings: AppSettings? = null
    
    /**
     * Update all derived flows when settings change
     */
    private fun updateDerivedFlows() {
        val settings = _settings.value
        _baseCurrency.value = settings.baseCurrency
        _apiKey.value = settings.exchangeRateApiKey
        _apiBaseUrl.value = settings.exchangeRateApiBaseUrl
        _lastUpdate.value = settings.lastExchangeRateUpdate
        _themeOption.value = settings.themeOption
        _voiceInputEnabled.value = settings.isVoiceInputEnabled
    }
    
    /**
     * Get all settings as a Flow
     */
    override fun getSettings(): Flow<AppSettings> {
        getSettingsCalled++
        return _settings
    }
    
    /**
     * Get settings synchronously
     */
    override suspend fun getSettingsSync(): AppSettings {
        getSettingsSyncCalled++
        if (delayMs > 0) delay(delayMs)
        if (shouldThrowError) throw Exception(errorMessage)
        return _settings.value
    }
    
    /**
     * Get base currency as a Flow
     */
    override fun getBaseCurrency(): Flow<Currency> {
        return _baseCurrency
    }
    
    /**
     * Get base currency synchronously
     */
    override suspend fun getBaseCurrencySync(): Currency {
        if (delayMs > 0) delay(delayMs)
        if (shouldThrowError) throw Exception(errorMessage)
        return _settings.value.baseCurrency
    }
    
    /**
     * Set base currency
     */
    override suspend fun setBaseCurrency(currency: Currency) {
        updateBaseCurrencyCalled++
        if (delayMs > 0) delay(delayMs)
        if (shouldThrowError) throw Exception(errorMessage)
        _settings.value = _settings.value.copy(baseCurrency = currency)
        updateDerivedFlows()
    }
    
    /**
     * Update base currency (alias)
     */
    override suspend fun updateBaseCurrency(currency: Currency) {
        setBaseCurrency(currency)
    }
    
    /**
     * Get API key as a Flow
     */
    override fun getApiKey(): Flow<String> {
        return _apiKey
    }
    
    /**
     * Get API key synchronously
     */
    override suspend fun getApiKeySync(): String {
        if (delayMs > 0) delay(delayMs)
        if (shouldThrowError) throw Exception(errorMessage)
        return _settings.value.exchangeRateApiKey
    }
    
    /**
     * Set API key
     */
    override suspend fun setApiKey(apiKey: String) {
        updateApiKeyCalled++
        if (delayMs > 0) delay(delayMs)
        if (shouldThrowError) throw Exception(errorMessage)
        _settings.value = _settings.value.copy(exchangeRateApiKey = apiKey)
        updateDerivedFlows()
    }
    
    /**
     * Update API key (alias)
     */
    override suspend fun updateApiKey(apiKey: String) {
        setApiKey(apiKey)
    }
    
    /**
     * Get API base URL as a Flow
     */
    override fun getApiBaseUrl(): Flow<String> {
        return _apiBaseUrl
    }
    
    /**
     * Get API base URL synchronously
     */
    override suspend fun getApiBaseUrlSync(): String {
        if (delayMs > 0) delay(delayMs)
        if (shouldThrowError) throw Exception(errorMessage)
        return _settings.value.exchangeRateApiBaseUrl
    }
    
    /**
     * Set API base URL
     */
    override suspend fun setApiBaseUrl(baseUrl: String) {
        if (delayMs > 0) delay(delayMs)
        if (shouldThrowError) throw Exception(errorMessage)
        _settings.value = _settings.value.copy(exchangeRateApiBaseUrl = baseUrl)
        updateDerivedFlows()
    }
    
    /**
     * Update API base URL (alias)
     */
    override suspend fun updateApiBaseUrl(baseUrl: String) {
        setApiBaseUrl(baseUrl)
    }
    
    /**
     * Check if API is configured
     */
    override suspend fun isApiConfigured(): Boolean {
        if (delayMs > 0) delay(delayMs)
        if (shouldThrowError) throw Exception(errorMessage)
        return _settings.value.exchangeRateApiKey.isNotBlank()
    }
    
    /**
     * Get last exchange rate update as a Flow
     */
    override fun getLastExchangeRateUpdate(): Flow<LocalDateTime?> {
        return _lastUpdate
    }
    
    /**
     * Update last exchange rate update timestamp
     */
    override suspend fun updateLastExchangeRateUpdate(timestamp: LocalDateTime) {
        if (delayMs > 0) delay(delayMs)
        if (shouldThrowError) throw Exception(errorMessage)
        _settings.value = _settings.value.copy(lastExchangeRateUpdate = timestamp)
        updateDerivedFlows()
    }
    
    /**
     * Update last exchange rate update to now
     */
    @OptIn(ExperimentalTime::class)
    override suspend fun updateLastExchangeRateUpdate() {
        val now = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        updateLastExchangeRateUpdate(now)
    }
    
    /**
     * Get theme option as a Flow
     */
    override fun getThemeOption(): Flow<ThemeOption> {
        return _themeOption
    }
    
    /**
     * Get theme option synchronously
     */
    override suspend fun getThemeOptionSync(): ThemeOption {
        if (delayMs > 0) delay(delayMs)
        if (shouldThrowError) throw Exception(errorMessage)
        return _settings.value.themeOption
    }
    
    /**
     * Set theme option
     */
    override suspend fun setThemeOption(themeOption: ThemeOption) {
        if (delayMs > 0) delay(delayMs)
        if (shouldThrowError) throw Exception(errorMessage)
        _settings.value = _settings.value.copy(themeOption = themeOption)
        updateDerivedFlows()
    }
    
    /**
     * Update theme option (alias)
     */
    override suspend fun updateThemeOption(themeOption: ThemeOption) {
        setThemeOption(themeOption)
    }
    
    /**
     * Get voice input enabled as a Flow
     */
    override fun getVoiceInputEnabled(): Flow<Boolean> {
        return _voiceInputEnabled
    }
    
    /**
     * Get voice input enabled synchronously
     */
    override suspend fun getVoiceInputEnabledSync(): Boolean {
        if (delayMs > 0) delay(delayMs)
        if (shouldThrowError) throw Exception(errorMessage)
        return _settings.value.isVoiceInputEnabled
    }
    
    /**
     * Set voice input enabled
     */
    override suspend fun setVoiceInputEnabled(isEnabled: Boolean) {
        if (delayMs > 0) delay(delayMs)
        if (shouldThrowError) throw Exception(errorMessage)
        _settings.value = _settings.value.copy(isVoiceInputEnabled = isEnabled)
        updateDerivedFlows()
    }
    
    /**
     * Update voice input enabled (alias)
     */
    override suspend fun updateVoiceInputEnabled(isEnabled: Boolean) {
        setVoiceInputEnabled(isEnabled)
    }
    
    /**
     * Save all settings at once
     */
    override suspend fun saveSettings(settings: AppSettings) {
        saveSettingsCalled++
        lastSavedSettings = settings
        if (delayMs > 0) delay(delayMs)
        if (shouldThrowError) throw Exception(errorMessage)
        _settings.value = settings
        updateDerivedFlows()
    }
    
    // Test helper methods
    
    /**
     * Set initial settings for testing
     */
    fun setSettings(settings: AppSettings) {
        _settings.value = settings
    }
    
    /**
     * Get current settings (for test assertions)
     */
    fun getCurrentSettings(): AppSettings {
        return _settings.value
    }
    
    /**
     * Configure error simulation
     */
    fun setShouldThrowError(shouldThrow: Boolean, message: String = "Test error") {
        shouldThrowError = shouldThrow
        errorMessage = message
    }
    
    /**
     * Configure delay simulation
     */
    fun setDelay(delayMillis: Long) {
        delayMs = delayMillis
    }
    
    /**
     * Reset to default state
     */
    fun reset() {
        _settings.value = AppSettings()
        updateDerivedFlows()
        shouldThrowError = false
        errorMessage = "Test error"
        delayMs = 0L
        getSettingsCalled = 0
        getSettingsSyncCalled = 0
        updateBaseCurrencyCalled = 0
        updateApiKeyCalled = 0
        saveSettingsCalled = 0
        lastSavedSettings = null
    }
}

