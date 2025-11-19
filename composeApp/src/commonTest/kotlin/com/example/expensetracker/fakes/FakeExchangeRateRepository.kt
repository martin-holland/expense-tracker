package com.example.expensetracker.fakes

import com.example.expensetracker.data.repository.IExchangeRateRepository
import com.example.expensetracker.model.Currency
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDateTime

/**
 * Fake implementation of IExchangeRateRepository for testing
 * 
 * This fake maintains in-memory exchange rates and provides full control
 * over behavior for comprehensive testing.
 */
class FakeExchangeRateRepository : IExchangeRateRepository {
    
    // In-memory exchange rate storage
    // Key format: "BASE_TARGET_DATE" or "BASE_TARGET" for null date
    private val rates = mutableMapOf<String, Double>()
    private val rateFlows = mutableMapOf<String, MutableStateFlow<Double?>>()
    
    // Test control flags
    var shouldThrowError = false
    var errorMessage = "Test error"
    var delayMs = 0L
    private var refreshShouldFail = false
    private var ratesAreStale = false
    
    // Test observability
    var getExchangeRateCalled = 0
    var getExchangeRateSyncCalled = 0
    var refreshExchangeRatesCalled = 0
    var isRateStaleCalled = 0
    var getAllRatesForBaseCalled = 0
    var clearOldRatesCalled = 0
    var lastRefreshCurrency: Currency? = null
    
    /**
     * Get exchange rate as a Flow
     */
    override fun getExchangeRate(
        baseCurrency: Currency,
        targetCurrency: Currency,
        date: LocalDateTime?
    ): Flow<Double?> {
        getExchangeRateCalled++
        
        // Same currency always returns 1.0
        if (baseCurrency == targetCurrency) {
            return MutableStateFlow(1.0)
        }
        
        val key = getRateKey(baseCurrency, targetCurrency, date)
        
        // Return existing flow or create new one
        return rateFlows.getOrPut(key) {
            MutableStateFlow(rates[key])
        }
    }
    
    /**
     * Get exchange rate synchronously
     */
    override suspend fun getExchangeRateSync(
        baseCurrency: Currency,
        targetCurrency: Currency,
        date: LocalDateTime?
    ): Double? {
        getExchangeRateSyncCalled++
        if (delayMs > 0) delay(delayMs)
        if (shouldThrowError) throw Exception(errorMessage)
        
        // Same currency always returns 1.0
        if (baseCurrency == targetCurrency) {
            return 1.0
        }
        
        val key = getRateKey(baseCurrency, targetCurrency, date)
        return rates[key]
    }
    
    /**
     * Refresh exchange rates from API
     */
    override suspend fun refreshExchangeRates(baseCurrency: Currency): Result<Unit> {
        refreshExchangeRatesCalled++
        lastRefreshCurrency = baseCurrency
        
        if (delayMs > 0) delay(delayMs)
        
        return if (refreshShouldFail || shouldThrowError) {
            Result.failure(Exception(errorMessage))
        } else {
            Result.success(Unit)
        }
    }
    
    /**
     * Check if rates are stale
     */
    override suspend fun isRateStale(baseCurrency: Currency): Boolean {
        isRateStaleCalled++
        if (delayMs > 0) delay(delayMs)
        if (shouldThrowError) throw Exception(errorMessage)
        return ratesAreStale
    }
    
    /**
     * Get all cached rates for a base currency
     */
    override suspend fun getAllRatesForBase(
        baseCurrency: Currency,
        date: LocalDateTime?
    ): Map<Currency, Double> {
        getAllRatesForBaseCalled++
        if (delayMs > 0) delay(delayMs)
        if (shouldThrowError) throw Exception(errorMessage)
        
        val result = mutableMapOf<Currency, Double>()
        val dateStr = date?.toString() ?: "null"
        
        // Find all rates for this base currency
        rates.entries
            .filter { it.key.startsWith("${baseCurrency.code}_") && it.key.endsWith("_$dateStr") }
            .forEach { (key, rate) ->
                // Extract target currency from key
                val parts = key.split("_")
                if (parts.size >= 2) {
                    val targetCode = parts[1]
                    Currency.entries.find { it.code == targetCode }?.let { targetCurrency ->
                        result[targetCurrency] = rate
                    }
                }
            }
        
        return result
    }
    
    /**
     * Clear old rates from cache
     */
    override suspend fun clearOldRates() {
        clearOldRatesCalled++
        if (delayMs > 0) delay(delayMs)
        if (shouldThrowError) throw Exception(errorMessage)
        // In fake, we just note it was called
        // Could clear rates older than X days if needed
    }
    
    // Test helper methods
    
    /**
     * Set a specific exchange rate for testing
     */
    fun setExchangeRate(
        baseCurrency: Currency,
        targetCurrency: Currency,
        rate: Double,
        date: LocalDateTime? = null
    ) {
        val key = getRateKey(baseCurrency, targetCurrency, date)
        rates[key] = rate
        
        // Update flow if it exists
        rateFlows[key]?.value = rate
    }
    
    /**
     * Set multiple rates at once
     */
    fun setRates(rateMap: Map<Pair<Currency, Currency>, Double>) {
        rateMap.forEach { (pair, rate) ->
            setExchangeRate(pair.first, pair.second, rate)
        }
    }
    
    /**
     * Set common rates for testing (USD as base)
     */
    fun setCommonRates() {
        setExchangeRate(Currency.USD, Currency.EUR, 0.85)
        setExchangeRate(Currency.USD, Currency.GBP, 0.73)
        setExchangeRate(Currency.USD, Currency.JPY, 110.0)
        setExchangeRate(Currency.USD, Currency.CAD, 1.25)
        setExchangeRate(Currency.USD, Currency.AUD, 1.35)
        
        // Reverse rates
        setExchangeRate(Currency.EUR, Currency.USD, 1.18)
        setExchangeRate(Currency.GBP, Currency.USD, 1.37)
        setExchangeRate(Currency.JPY, Currency.USD, 0.0091)
    }
    
    /**
     * Clear all rates
     */
    fun clearRates() {
        rates.clear()
        rateFlows.clear()
    }
    
    /**
     * Configure error simulation
     */
    fun setShouldThrowError(shouldThrow: Boolean, message: String = "Test error") {
        shouldThrowError = shouldThrow
        errorMessage = message
    }
    
    /**
     * Configure refresh failure
     */
    fun setRefreshShouldFail(shouldFail: Boolean) {
        refreshShouldFail = shouldFail
    }
    
    /**
     * Configure delay simulation
     */
    fun setDelay(delayMillis: Long) {
        delayMs = delayMillis
    }
    
    /**
     * Set whether rates are stale
     */
    fun setRatesAreStale(isStale: Boolean) {
        ratesAreStale = isStale
    }
    
    /**
     * Reset to default state
     */
    fun reset() {
        rates.clear()
        rateFlows.clear()
        shouldThrowError = false
        errorMessage = "Test error"
        delayMs = 0L
        refreshShouldFail = false
        ratesAreStale = false
        getExchangeRateCalled = 0
        getExchangeRateSyncCalled = 0
        refreshExchangeRatesCalled = 0
        isRateStaleCalled = 0
        getAllRatesForBaseCalled = 0
        clearOldRatesCalled = 0
        lastRefreshCurrency = null
    }
    
    /**
     * Helper to generate consistent keys
     */
    private fun getRateKey(
        baseCurrency: Currency,
        targetCurrency: Currency,
        date: LocalDateTime?
    ): String {
        val dateStr = date?.toString() ?: "null"
        return "${baseCurrency.code}_${targetCurrency.code}_$dateStr"
    }
}

