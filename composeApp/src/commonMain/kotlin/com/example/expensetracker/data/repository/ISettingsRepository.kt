package com.example.expensetracker.data.repository

import com.example.expensetracker.model.AppSettings
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.ThemeOption
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

/**
 * Interface for Settings Repository
 * Enables dependency injection and testing with fake implementations
 */
interface ISettingsRepository {
    
    /**
     * Get all settings as a Flow
     */
    fun getSettings(): Flow<AppSettings>
    
    /**
     * Get settings synchronously (one-time read)
     */
    suspend fun getSettingsSync(): AppSettings
    
    /**
     * Get base currency as a Flow
     */
    fun getBaseCurrency(): Flow<Currency>
    
    /**
     * Get base currency synchronously
     */
    suspend fun getBaseCurrencySync(): Currency
    
    /**
     * Set/update base currency
     */
    suspend fun setBaseCurrency(currency: Currency)
    
    /**
     * Update base currency (alias for setBaseCurrency)
     */
    suspend fun updateBaseCurrency(currency: Currency)
    
    /**
     * Get API key as a Flow
     */
    fun getApiKey(): Flow<String>
    
    /**
     * Get API key synchronously
     */
    suspend fun getApiKeySync(): String
    
    /**
     * Set/update API key
     */
    suspend fun setApiKey(apiKey: String)
    
    /**
     * Update API key (alias for setApiKey)
     */
    suspend fun updateApiKey(apiKey: String)
    
    /**
     * Get API base URL as a Flow
     */
    fun getApiBaseUrl(): Flow<String>
    
    /**
     * Get API base URL synchronously
     */
    suspend fun getApiBaseUrlSync(): String
    
    /**
     * Set/update API base URL
     */
    suspend fun setApiBaseUrl(baseUrl: String)
    
    /**
     * Update API base URL (alias for setApiBaseUrl)
     */
    suspend fun updateApiBaseUrl(baseUrl: String)
    
    /**
     * Check if API is configured
     */
    suspend fun isApiConfigured(): Boolean
    
    /**
     * Get last exchange rate update timestamp as a Flow
     */
    fun getLastExchangeRateUpdate(): Flow<LocalDateTime?>
    
    /**
     * Update last exchange rate update timestamp
     */
    suspend fun updateLastExchangeRateUpdate(timestamp: LocalDateTime)
    
    /**
     * Update last exchange rate update timestamp to now
     */
    suspend fun updateLastExchangeRateUpdate()
    
    /**
     * Get theme option as a Flow
     */
    fun getThemeOption(): Flow<ThemeOption>
    
    /**
     * Get theme option synchronously
     */
    suspend fun getThemeOptionSync(): ThemeOption
    
    /**
     * Set/update theme option
     */
    suspend fun setThemeOption(themeOption: ThemeOption)
    
    /**
     * Update theme option (alias for setThemeOption)
     */
    suspend fun updateThemeOption(themeOption: ThemeOption)
    
    /**
     * Get voice input enabled flag as a Flow
     */
    fun getVoiceInputEnabled(): Flow<Boolean>
    
    /**
     * Get voice input enabled flag synchronously
     */
    suspend fun getVoiceInputEnabledSync(): Boolean
    
    /**
     * Set/update voice input enabled flag
     */
    suspend fun setVoiceInputEnabled(isEnabled: Boolean)
    
    /**
     * Update voice input enabled flag (alias for setVoiceInputEnabled)
     */
    suspend fun updateVoiceInputEnabled(isEnabled: Boolean)
    
    /**
     * Save all settings at once
     */
    suspend fun saveSettings(settings: AppSettings)
}

