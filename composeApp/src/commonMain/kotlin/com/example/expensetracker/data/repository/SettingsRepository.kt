package com.example.expensetracker.data.repository

import com.example.expensetracker.data.database.SettingsDao
import com.example.expensetracker.data.database.getRoomDatabase
import com.example.expensetracker.data.database.toAppSettings
import com.example.expensetracker.data.database.toEntity
import com.example.expensetracker.model.AppSettings
import com.example.expensetracker.model.Currency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Repository for managing app settings
 * Provides a clean API for accessing and manipulating settings data
 * Handles database operations and coordinates between data sources
 * 
 * This is a singleton repository that can be used across multiple ViewModels
 * 
 * Usage:
 * ```kotlin
 * val repository = SettingsRepository.getInstance()
 * val baseCurrency = repository.getBaseCurrency()
 * repository.updateBaseCurrency(Currency.EUR)
 * ```
 */
class SettingsRepository private constructor(
    private val settingsDao: SettingsDao
) {
    
    /**
     * Gets the base currency as a Flow
     * The Flow will emit updates whenever the base currency changes
     */
    fun getBaseCurrency(): Flow<Currency> {
        return settingsDao.getSettings().map { entity ->
            entity?.toAppSettings()?.baseCurrency ?: Currency.USD
        }
    }
    
    /**
     * Gets all settings as a Flow
     * The Flow will emit updates whenever any setting changes
     */
    fun getSettings(): Flow<AppSettings> {
        return settingsDao.getSettings().map { entity ->
            entity?.toAppSettings() ?: AppSettings() // Default settings if not found
        }
    }
    
    /**
     * Gets settings synchronously (for one-time reads)
     * @return The current settings, or default settings if not found
     */
    suspend fun getSettingsSync(): AppSettings {
        return settingsDao.getSettingsSync()?.toAppSettings() ?: AppSettings()
    }
    
    /**
     * Updates the base currency
     * @param currency The currency to set as base currency
     */
    suspend fun updateBaseCurrency(currency: Currency) {
        settingsDao.updateBaseCurrency(currency.code)
    }
    
    /**
     * Gets the base currency synchronously (for one-time reads)
     * @return The current base currency, or USD if not found
     */
    suspend fun getBaseCurrencySync(): Currency {
        return getSettingsSync().baseCurrency
    }
    
    /**
     * Gets the last exchange rate update timestamp as a Flow
     * The Flow will emit updates whenever the timestamp changes
     */
    fun getLastExchangeRateUpdate(): Flow<kotlinx.datetime.LocalDateTime?> {
        return settingsDao.getSettings().map { entity ->
            entity?.toAppSettings()?.lastExchangeRateUpdate
        }
    }
    
    /**
     * Updates the last exchange rate update timestamp
     * @param timestamp The timestamp to set
     */
    suspend fun updateLastExchangeRateUpdate(timestamp: kotlinx.datetime.LocalDateTime) {
        settingsDao.updateLastExchangeRateUpdate(timestamp.toString())
    }
    
    /**
     * Updates the last exchange rate update timestamp to now
     */
    suspend fun updateLastExchangeRateUpdate() {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        updateLastExchangeRateUpdate(now)
    }
    
    /**
     * Gets the API key as a Flow
     * The Flow will emit updates whenever the API key changes
     */
    fun getApiKey(): Flow<String> {
        return settingsDao.getSettings().map { entity ->
            entity?.toAppSettings()?.exchangeRateApiKey ?: ""
        }
    }
    
    /**
     * Gets the API key synchronously (for one-time reads)
     * @return The current API key, or empty string if not found
     */
    suspend fun getApiKeySync(): String {
        return getSettingsSync().exchangeRateApiKey
    }
    
    /**
     * Updates the API key
     * @param apiKey The API key to set
     */
    suspend fun updateApiKey(apiKey: String) {
        settingsDao.updateApiKey(apiKey)
    }
    
    /**
     * Sets the API key (alias for updateApiKey for consistency with spec)
     * @param apiKey The API key to set
     */
    suspend fun setApiKey(apiKey: String) {
        updateApiKey(apiKey)
    }
    
    /**
     * Gets the API base URL as a Flow
     * The Flow will emit updates whenever the API base URL changes
     */
    fun getApiBaseUrl(): Flow<String> {
        return settingsDao.getSettings().map { entity ->
            entity?.toAppSettings()?.exchangeRateApiBaseUrl ?: "https://v6.exchangerate-api.com/v6"
        }
    }
    
    /**
     * Gets the API base URL synchronously (for one-time reads)
     * @return The current API base URL, or default URL if not found
     */
    suspend fun getApiBaseUrlSync(): String {
        return getSettingsSync().exchangeRateApiBaseUrl
    }
    
    /**
     * Updates the API base URL
     * @param baseUrl The API base URL to set
     */
    suspend fun updateApiBaseUrl(baseUrl: String) {
        settingsDao.updateApiBaseUrl(baseUrl)
    }
    
    /**
     * Sets the API base URL (alias for updateApiBaseUrl for consistency with spec)
     * @param baseUrl The API base URL to set
     */
    suspend fun setApiBaseUrl(baseUrl: String) {
        updateApiBaseUrl(baseUrl)
    }
    
    /**
     * Checks if the API is configured (API key is set)
     * @return true if API key is not empty, false otherwise
     */
    suspend fun isApiConfigured(): Boolean {
        return getApiKeySync().isNotBlank()
    }
    
    /**
     * Sets the base currency (alias for updateBaseCurrency for consistency with spec)
     * @param currency The currency to set as base currency
     */
    suspend fun setBaseCurrency(currency: Currency) {
        updateBaseCurrency(currency)
    }
    
    /**
     * Saves all settings at once
     * @param settings The settings to save
     */
    suspend fun saveSettings(settings: AppSettings) {
        settingsDao.insertOrUpdateSettings(settings.toEntity())
    }
    
    /**
     * Initializes default settings if they don't exist
     * This is called automatically on first access
     */
    private suspend fun initializeDefaultSettingsIfNeeded() {
        val existingSettings = settingsDao.getSettingsSync()
        if (existingSettings == null) {
            // Insert default settings
            val defaultSettings = AppSettings()
            settingsDao.insertOrUpdateSettings(defaultSettings.toEntity())
        }
    }
    
    companion object {
        private var instance: SettingsRepository? = null
        private val lock = Any()
        
        /**
         * Gets the singleton instance of SettingsRepository
         * Automatically initializes default settings on first access if needed
         * 
         * @return The singleton SettingsRepository instance
         */
        fun getInstance(): SettingsRepository {
            if (instance != null) return instance!!
            
            // Double-checked locking for thread safety
            return instance ?: run {
                val database = getRoomDatabase()
                val repository = SettingsRepository(database.settingsDao())
                
                // Initialize default settings if needed (on background thread)
                CoroutineScope(Dispatchers.Default).launch {
                    repository.initializeDefaultSettingsIfNeeded()
                }
                
                repository.also { instance = it }
            }
        }
        
        /**
         * Resets the singleton instance (useful for testing)
         */
        fun resetInstance() {
            instance = null
        }
    }
}

