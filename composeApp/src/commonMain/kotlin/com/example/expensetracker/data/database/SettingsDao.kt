package com.example.expensetracker.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for Settings operations
 * Defines all database operations for the settings table
 * 
 * Note: Settings table uses a single-row pattern (id is always "settings")
 */
@Dao
interface SettingsDao {
    
    /**
     * Gets settings from the database
     * Returns a Flow that emits the settings whenever data changes
     * @return Flow of settings (nullable since settings might not exist initially)
     */
    @Query("SELECT * FROM settings WHERE id = 'settings'")
    fun getSettings(): Flow<SettingsEntity?>
    
    /**
     * Gets settings synchronously (for one-time reads)
     * @return The settings if found, null otherwise
     */
    @Query("SELECT * FROM settings WHERE id = 'settings'")
    suspend fun getSettingsSync(): SettingsEntity?
    
    /**
     * Inserts or updates settings in the database
     * Uses REPLACE strategy to handle the single-row pattern
     * @param settings The settings to insert/update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: SettingsEntity)
    
    /**
     * Updates only the base currency
     * @param currency The currency code to set as base currency
     */
    @Query("UPDATE settings SET baseCurrency = :currency WHERE id = 'settings'")
    suspend fun updateBaseCurrency(currency: String)
    
    /**
     * Updates only the last exchange rate update timestamp
     * @param timestamp The timestamp as ISO-8601 string
     */
    @Query("UPDATE settings SET lastExchangeRateUpdate = :timestamp WHERE id = 'settings'")
    suspend fun updateLastExchangeRateUpdate(timestamp: String?)
    
    /**
     * Updates only the API key
     * @param apiKey The API key to set
     */
    @Query("UPDATE settings SET exchangeRateApiKey = :apiKey WHERE id = 'settings'")
    suspend fun updateApiKey(apiKey: String)
    
    /**
     * Updates only the API base URL
     * @param baseUrl The API base URL to set
     */
    @Query("UPDATE settings SET exchangeRateApiBaseUrl = :baseUrl WHERE id = 'settings'")
    suspend fun updateApiBaseUrl(baseUrl: String)
    
    /**
     * Updates only the theme option
     * @param themeOption The theme option to set (LIGHT, DARK, or SYSTEM)
     */
    @Query("UPDATE settings SET themeOption = :themeOption WHERE id = 'settings'")
    suspend fun updateThemeOption(themeOption: String)
    
    /**
     * Updates only the voice input enabled flag
     * @param isEnabled Whether voice input is enabled
     */
    @Query("UPDATE settings SET isVoiceInputEnabled = :isEnabled WHERE id = 'settings'")
    suspend fun updateVoiceInputEnabled(isEnabled: Boolean)
}

