package com.example.expensetracker.data.repository

import com.example.expensetracker.model.Currency
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

/**
 * Interface for Exchange Rate Repository
 * Enables dependency injection and testing with fake implementations
 */
interface IExchangeRateRepository {
    
    /**
     * Get exchange rate as a Flow
     */
    fun getExchangeRate(
        baseCurrency: Currency,
        targetCurrency: Currency,
        date: LocalDateTime? = null
    ): Flow<Double?>
    
    /**
     * Get exchange rate synchronously
     */
    suspend fun getExchangeRateSync(
        baseCurrency: Currency,
        targetCurrency: Currency,
        date: LocalDateTime? = null
    ): Double?
    
    /**
     * Refresh exchange rates from API
     */
    suspend fun refreshExchangeRates(baseCurrency: Currency): Result<Unit>
    
    /**
     * Check if rates are stale (need refresh)
     */
    suspend fun isRateStale(baseCurrency: Currency): Boolean
    
    /**
     * Get all cached rates for a base currency
     */
    suspend fun getAllRatesForBase(
        baseCurrency: Currency,
        date: LocalDateTime? = null
    ): Map<Currency, Double>
    
    /**
     * Clear old rates from cache
     */
    suspend fun clearOldRates()
}

