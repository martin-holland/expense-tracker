package com.example.expensetracker.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.expensetracker.model.AppSettings
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.ThemeOption
import kotlinx.datetime.LocalDateTime

/**
 * Room Entity representing app settings in the database
 * This is the database representation of AppSettings
 * 
 * Uses a single-row table pattern where id is always "settings"
 * 
 * @property id Primary key, always "settings" (single row table)
 * @property baseCurrency Base currency for expense conversion
 * @property lastExchangeRateUpdate Timestamp of last exchange rate update (nullable)
 * @property exchangeRateApiKey API key for exchange rate service
 * @property exchangeRateApiBaseUrl Base URL for exchange rate API
 * @property themeOption Theme preference (LIGHT, DARK, or SYSTEM)
 * @property isVoiceInputEnabled Whether voice input is enabled
 * 
 * Note: Refresh interval is fixed at 24 hours - not stored in database
 */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: String = "settings",
    val baseCurrency: Currency,
    val lastExchangeRateUpdate: LocalDateTime?,
    val exchangeRateApiKey: String,
    val exchangeRateApiBaseUrl: String,
    val themeOption: ThemeOption = ThemeOption.SYSTEM,
    val isVoiceInputEnabled: Boolean = false
)

/**
 * Extension function to convert SettingsEntity to domain model AppSettings
 */
fun SettingsEntity.toAppSettings(): AppSettings {
    return AppSettings(
        baseCurrency = baseCurrency,
        lastExchangeRateUpdate = lastExchangeRateUpdate,
        exchangeRateApiKey = exchangeRateApiKey,
        exchangeRateApiBaseUrl = exchangeRateApiBaseUrl,
        themeOption = themeOption,
        isVoiceInputEnabled = isVoiceInputEnabled
    )
}

/**
 * Extension function to convert domain model AppSettings to SettingsEntity
 */
fun AppSettings.toEntity(): SettingsEntity {
    return SettingsEntity(
        id = "settings",
        baseCurrency = baseCurrency,
        lastExchangeRateUpdate = lastExchangeRateUpdate,
        exchangeRateApiKey = exchangeRateApiKey,
        exchangeRateApiBaseUrl = exchangeRateApiBaseUrl,
        themeOption = themeOption,
        isVoiceInputEnabled = isVoiceInputEnabled
    )
}

