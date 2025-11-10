package com.example.expensetracker.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

/**
 * Room Entity representing an exchange rate in the database
 * 
 * This entity caches exchange rates fetched from the API to enable offline support
 * and reduce API usage.
 * 
 * @property id Primary key - Format: "{baseCurrency}_{targetCurrency}_{date}" (e.g., "USD_EUR_2024-11-01")
 * @property baseCurrency Base currency code (e.g., "USD")
 * @property targetCurrency Target currency code (e.g., "EUR")
 * @property rate Exchange rate from baseCurrency to targetCurrency (e.g., 0.85 means 1 USD = 0.85 EUR)
 * @property date Date of the rate in ISO format (YYYY-MM-DD)
 * @property lastUpdated Timestamp when this rate was cached (ISO format string)
 * 
 * Indexes:
 * - idx_exchange_rates_base_date: (baseCurrency, date) for fast lookups by base currency and date
 * - idx_exchange_rates_base_target_date: (baseCurrency, targetCurrency, date) for specific rate queries
 */
@Entity(
    tableName = "exchange_rates",
    indices = [
        Index(value = ["baseCurrency", "date"], name = "idx_exchange_rates_base_date"),
        Index(value = ["baseCurrency", "targetCurrency", "date"], name = "idx_exchange_rates_base_target_date")
    ]
)
data class ExchangeRateEntity(
    @PrimaryKey
    val id: String,
    val baseCurrency: String,
    val targetCurrency: String,
    val rate: Double,
    val date: String, // ISO format: YYYY-MM-DD
    val lastUpdated: String // ISO format: LocalDateTime as String
) {
    companion object {
        /**
         * Generates an ID for an exchange rate entity
         * Format: "{baseCurrency}_{targetCurrency}_{date}"
         * Example: "USD_EUR_2024-11-01"
         */
        fun generateId(baseCurrency: String, targetCurrency: String, date: String): String {
            return "${baseCurrency}_${targetCurrency}_$date"
        }
        
        /**
         * Generates an ID using LocalDate
         */
        fun generateId(baseCurrency: String, targetCurrency: String, date: LocalDate): String {
            return generateId(baseCurrency, targetCurrency, date.toString())
        }
    }
}

