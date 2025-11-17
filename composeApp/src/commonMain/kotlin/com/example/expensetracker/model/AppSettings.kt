package com.example.expensetracker.model

import kotlinx.datetime.LocalDateTime

/**
 * Data class representing app settings
 * 
 * This is the domain model for application settings, including currency exchange configuration.
 * 
 * @param baseCurrency Base currency for expense conversion (default: USD)
 * @param lastExchangeRateUpdate Timestamp of last exchange rate update (nullable)
 * @param exchangeRateApiKey API key for exchange rate service (default: empty, user must configure)
 * @param exchangeRateApiBaseUrl Base URL for exchange rate API (default: exchangerate-api.com)
 * @param themeOption Theme preference (LIGHT, DARK, or SYSTEM) (default: SYSTEM)
 * @param isVoiceInputEnabled Whether voice input is enabled (default: false)
 * 
 * Note: Refresh interval is fixed at 24 hours (once per day) - not configurable
 */
data class AppSettings(
    val baseCurrency: Currency = Currency.USD,
    val lastExchangeRateUpdate: LocalDateTime? = null,
    val exchangeRateApiKey: String = "",
    val exchangeRateApiBaseUrl: String = "https://v6.exchangerate-api.com/v6",
    val themeOption: ThemeOption = ThemeOption.SYSTEM,
    val isVoiceInputEnabled: Boolean = false
) {
    /**
     * Checks if the API is configured (API key is not empty)
     */
    fun isApiConfigured(): Boolean {
        return exchangeRateApiKey.isNotBlank()
    }
}

