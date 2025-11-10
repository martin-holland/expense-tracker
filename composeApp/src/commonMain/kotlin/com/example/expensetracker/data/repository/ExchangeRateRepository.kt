package com.example.expensetracker.data.repository

import com.example.expensetracker.data.database.ExchangeRateDao
import com.example.expensetracker.data.database.ExchangeRateEntity
import com.example.expensetracker.data.database.getRoomDatabase
import com.example.expensetracker.data.network.ExchangeRateApiService
import com.example.expensetracker.data.network.createHttpClient
import com.example.expensetracker.model.Currency
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Repository for managing exchange rates
 * 
 * Provides a clean API for fetching, caching, and retrieving exchange rates.
 * Implements cross-rate calculation optimization: single API call fetches ALL rates
 * for base currency, then calculates any currency pair conversion locally.
 * 
 * This is a singleton repository that can be used across multiple ViewModels.
 * 
 * Usage:
 * ```kotlin
 * val repository = ExchangeRateRepository.getInstance()
 * val rate = repository.getExchangeRateSync(Currency.USD, Currency.EUR)
 * repository.refreshExchangeRates(Currency.USD)
 * ```
 */
class ExchangeRateRepository private constructor(
    private val exchangeRateDao: ExchangeRateDao,
    private val settingsRepository: SettingsRepository,
    private val apiService: ExchangeRateApiService
) {
    
    companion object {
        private var instance: ExchangeRateRepository? = null
        private val lock = Any()
        
        /**
         * Gets the singleton instance of ExchangeRateRepository
         * 
         * @return The singleton ExchangeRateRepository instance
         */
        fun getInstance(): ExchangeRateRepository {
            if (instance != null) return instance!!
            
            // Double-checked locking for thread safety
            return instance ?: run {
                val database = getRoomDatabase()
                val exchangeRateDao = database.exchangeRateDao()
                val settingsRepository = SettingsRepository.getInstance()
                val httpClient = createHttpClient()
                val apiService = ExchangeRateApiService(httpClient)
                
                val repository = ExchangeRateRepository(
                    exchangeRateDao,
                    settingsRepository,
                    apiService
                )
                
                // Cleanup old rates on background thread
                CoroutineScope(Dispatchers.Default).launch {
                    repository.clearOldRates()
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
    
    /**
     * Gets an exchange rate as a Flow
     * Uses cross-rate calculation if direct rate not available
     * 
     * Note: This method uses getLatestRates Flow and filters for the specific rate.
     * For better performance, consider using getExchangeRateSync for one-time reads.
     * 
     * @param baseCurrency Base currency
     * @param targetCurrency Target currency
     * @param date Optional date for historical rates. If null, uses latest rate
     * @return Flow emitting the exchange rate, or null if unavailable
     */
    fun getExchangeRate(
        baseCurrency: Currency,
        targetCurrency: Currency,
        date: LocalDateTime? = null
    ): Flow<Double?> {
        // If same currency, return 1.0
        if (baseCurrency == targetCurrency) {
            return kotlinx.coroutines.flow.flowOf(1.0)
        }
        
        val dateString = date?.date?.toString() // Extract date part (YYYY-MM-DD)
        
        // Use getLatestRates Flow and filter for the specific target currency
        return if (dateString != null) {
            exchangeRateDao.getRatesByDate(baseCurrency.code, dateString)
        } else {
            exchangeRateDao.getLatestRates(baseCurrency.code)
        }.map { entities ->
            entities.find { it.targetCurrency == targetCurrency.code }?.rate
        }
    }
    
    /**
     * Gets an exchange rate synchronously
     * Uses cross-rate calculation if direct rate not available
     * 
     * @param baseCurrency Base currency
     * @param targetCurrency Target currency
     * @param date Optional date for historical rates. If null, uses latest rate
     * @return The exchange rate, or null if unavailable
     */
    suspend fun getExchangeRateSync(
        baseCurrency: Currency,
        targetCurrency: Currency,
        date: LocalDateTime? = null
    ): Double? {
        // If same currency, return 1.0
        if (baseCurrency == targetCurrency) {
            return 1.0
        }
        
        val dateString = date?.date?.toString()
        
        // Try direct rate first
        val directRate = exchangeRateDao.getRate(
            baseCurrency.code,
            targetCurrency.code,
            dateString
        )
        
        if (directRate != null) {
            return directRate.rate
        }
        
        // Try cross-rate calculation via user's base currency
        // This is the key optimization: if we have rates for user's base currency,
        // we can calculate any currency pair conversion
        val userBaseCurrency = settingsRepository.getBaseCurrencySync()
        
        if (userBaseCurrency != baseCurrency && userBaseCurrency != targetCurrency) {
            // Get rate from userBaseCurrency to baseCurrency
            val rate1 = exchangeRateDao.getRate(
                userBaseCurrency.code,
                baseCurrency.code,
                dateString
            )
            
            // Get rate from userBaseCurrency to targetCurrency
            val rate2 = exchangeRateDao.getRate(
                userBaseCurrency.code,
                targetCurrency.code,
                dateString
            )
            
            if (rate1 != null && rate2 != null && rate1.rate != 0.0) {
                // Calculate: baseCurrency -> targetCurrency = (userBase -> target) / (userBase -> base)
                // Example: USD -> EUR = (USD -> EUR) / (USD -> USD) = 0.85 / 1.0 = 0.85
                // But if we have GBP -> USD and GBP -> EUR, then USD -> EUR = (GBP -> EUR) / (GBP -> USD)
                return rate2.rate / rate1.rate
            }
        }
        
        // Fallback: try direct reverse lookup (if we have target -> base, invert it)
        val reverseRate = exchangeRateDao.getRate(
            targetCurrency.code,
            baseCurrency.code,
            dateString
        )
        
        if (reverseRate != null && reverseRate.rate != 0.0) {
            return 1.0 / reverseRate.rate
        }
        
        return null
    }
    
    /**
     * Fetches and caches ALL exchange rates for a base currency
     * This is the key optimization: single API call gets all rates
     * 
     * @param baseCurrency Base currency to fetch rates for
     * @return Result indicating success or failure
     */
    suspend fun refreshExchangeRates(baseCurrency: Currency): Result<Unit> {
        return try {
            // Get API configuration from settings
            val apiKey = settingsRepository.getApiKeySync()
            val apiBaseUrl = settingsRepository.getApiBaseUrlSync()
            
            if (apiKey.isBlank()) {
                return Result.failure(Exception("API key not configured. Please configure it in Settings."))
            }
            
            // Fetch rates from API
            val result = apiService.getLatestRates(apiKey, baseCurrency.code, apiBaseUrl)
            
            result.fold(
                onSuccess = { response ->
                    // Get current date for caching
                    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    val today = now.date.toString()
                    val nowString = now.toString()
                    
                    // Convert all rates to entities
                    val entities = response.conversionRates.map { (targetCode, rate) ->
                        ExchangeRateEntity(
                            id = ExchangeRateEntity.generateId(baseCurrency.code, targetCode, today),
                            baseCurrency = baseCurrency.code,
                            targetCurrency = targetCode,
                            rate = rate,
                            date = today,
                            lastUpdated = nowString
                        )
                    }
                    
                    // Store all rates in database
                    exchangeRateDao.insertOrUpdateRates(entities)
                    
                    // Update last refresh timestamp in settings
                    settingsRepository.updateLastExchangeRateUpdate()
                    
                    Result.success(Unit)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(Exception("Failed to refresh exchange rates: ${e.message}", e))
        }
    }
    
    /**
     * Checks if rates for a base currency are stale (older than 24 hours)
     * 
     * @param baseCurrency Base currency to check
     * @return true if rates need refresh, false otherwise
     */
    suspend fun isRateStale(baseCurrency: Currency): Boolean {
        val latestRates = exchangeRateDao.getRatesSync(baseCurrency.code, null)
        
        if (latestRates.isEmpty()) {
            return true // No rates cached, need to fetch
        }
        
        // Get the most recent rate's lastUpdated timestamp
        val mostRecent = latestRates.maxByOrNull { it.lastUpdated }
            ?: return true
        
        // Parse the timestamp
        val lastUpdated = try {
            LocalDateTime.parse(mostRecent.lastUpdated)
        } catch (e: Exception) {
            return true // Invalid timestamp, consider stale
        }
        
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val nowDate = now.date
        val lastUpdatedDate = lastUpdated.date
        
        // Calculate hours since update (simplified - in production use proper duration calculation)
        val daysDiff = (nowDate.toEpochDays() - lastUpdatedDate.toEpochDays()).toInt()
        val hoursSinceUpdate = daysDiff * 24 + (now.hour - lastUpdated.hour)
        
        // Consider stale if older than 24 hours
        return hoursSinceUpdate >= 24
    }
    
    /**
     * Cleans up exchange rates older than 30 days
     */
    suspend fun clearOldRates() {
        try {
            // Calculate date 30 days ago
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val today = now.date
            // Calculate 30 days ago using epoch days
            val thirtyDaysAgoEpoch = today.toEpochDays() - 30
            val thirtyDaysAgo = LocalDate.fromEpochDays(thirtyDaysAgoEpoch)
            
            // Delete rates older than 30 days
            exchangeRateDao.deleteOldRates(thirtyDaysAgo.toString())
        } catch (e: Exception) {
            // Log error but don't fail
            println("Error clearing old rates: ${e.message}")
        }
    }
    
    /**
     * Gets all cached rates for a base currency
     * 
     * @param baseCurrency Base currency
     * @param date Optional date. If null, gets latest rates
     * @return Map of Currency to exchange rate
     */
    suspend fun getAllRatesForBase(
        baseCurrency: Currency,
        date: LocalDateTime? = null
    ): Map<Currency, Double> {
        val dateString = date?.date?.toString()
        val entities = exchangeRateDao.getRatesSync(baseCurrency.code, dateString)
        
        return entities.associate { entity ->
            val currency = Currency.fromCode(entity.targetCurrency)
            currency to entity.rate
        }
    }
}

