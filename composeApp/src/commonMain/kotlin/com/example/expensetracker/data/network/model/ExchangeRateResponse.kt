package com.example.expensetracker.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response model for exchange rate API (exchangerate-api.com)
 * 
 * This represents the response from the latest rates endpoint:
 * GET {baseUrl}/{apiKey}/latest/{baseCurrency}
 * 
 * Example response:
 * ```json
 * {
 *   "result": "success",
 *   "base_code": "USD",
 *   "time_last_update_utc": "2024-11-01T12:00:00+00:00",
 *   "conversion_rates": {
 *     "USD": 1.0,
 *     "EUR": 0.85,
 *     "GBP": 0.73,
 *     "JPY": 110.0
 *   }
 * }
 * ```
 */
@Serializable
data class ExchangeRateResponse(
    /**
     * Result status: "success" for successful requests, error code for failures
     */
    val result: String,
    
    /**
     * Base currency code (e.g., "USD", "EUR")
     */
    @SerialName("base_code")
    val baseCode: String,
    
    /**
     * Last update timestamp in UTC
     * Example: "Mon, 10 Nov 2025 00:00:01 +0000"
     */
    @SerialName("time_last_update_utc")
    val timeLastUpdateUtc: String,
    
    /**
     * Map of currency codes to exchange rates
     * All rates are relative to the base currency
     * Example: If base is USD, EUR: 0.85 means 1 USD = 0.85 EUR
     */
    @SerialName("conversion_rates")
    val conversionRates: Map<String, Double>,
    
    /**
     * Optional fields that may be present in the API response
     * These are ignored if not present due to default values
     */
    @SerialName("documentation")
    val documentation: String? = null,
    
    @SerialName("terms_of_use")
    val termsOfUse: String? = null,
    
    @SerialName("time_last_update_unix")
    val timeLastUpdateUnix: Long? = null,
    
    @SerialName("time_next_update_unix")
    val timeNextUpdateUnix: Long? = null,
    
    @SerialName("time_next_update_utc")
    val timeNextUpdateUtc: String? = null
) {
    /**
     * Checks if the response indicates success
     */
    fun isSuccess(): Boolean {
        return result == "success"
    }
}

/**
 * Error response model for exchange rate API
 * 
 * Used when the API returns an error response
 * Example:
 * ```json
 * {
 *   "result": "error",
 *   "error-type": "invalid-key"
 * }
 * ```
 */
@Serializable
data class ErrorResponse(
    /**
     * Error code (e.g., "error", "invalid-key", "quota-reached")
     */
    val result: String,
    
    /**
     * Error type description
     */
    @SerialName("error-type")
    val errorType: String? = null
)

