package com.example.expensetracker.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for Exchange Rate operations
 * Defines all database operations for the exchange_rates table
 */
@Dao
interface ExchangeRateDao {
    
    /**
     * Gets a specific exchange rate
     * 
     * @param baseCurrency Base currency code
     * @param targetCurrency Target currency code
     * @param date Date in ISO format (YYYY-MM-DD). If null, gets the latest rate
     * @return The exchange rate entity if found, null otherwise
     */
    @Query("""
        SELECT * FROM exchange_rates 
        WHERE baseCurrency = :baseCurrency 
        AND targetCurrency = :targetCurrency 
        AND (:date IS NULL OR date = :date)
        ORDER BY date DESC
        LIMIT 1
    """)
    suspend fun getRate(
        baseCurrency: String,
        targetCurrency: String,
        date: String?
    ): ExchangeRateEntity?
    
    /**
     * Gets all latest rates for a base currency
     * Returns a Flow that emits updates whenever rates change
     * 
     * @param baseCurrency Base currency code
     * @return Flow of all exchange rates for the base currency (latest date for each target currency)
     */
    @Query("""
        SELECT * FROM exchange_rates 
        WHERE baseCurrency = :baseCurrency 
        AND date = (
            SELECT MAX(date) FROM exchange_rates 
            WHERE baseCurrency = :baseCurrency
        )
        ORDER BY targetCurrency
    """)
    fun getLatestRates(baseCurrency: String): Flow<List<ExchangeRateEntity>>
    
    /**
     * Gets all rates for a base currency on a specific date
     * 
     * @param baseCurrency Base currency code
     * @param date Date in ISO format (YYYY-MM-DD)
     * @return Flow of all exchange rates for the base currency on the specified date
     */
    @Query("""
        SELECT * FROM exchange_rates 
        WHERE baseCurrency = :baseCurrency 
        AND date = :date
        ORDER BY targetCurrency
    """)
    fun getRatesByDate(baseCurrency: String, date: String): Flow<List<ExchangeRateEntity>>
    
    /**
     * Inserts or updates a single exchange rate
     * Uses REPLACE strategy to handle updates
     * 
     * @param rate The exchange rate entity to insert/update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRate(rate: ExchangeRateEntity)
    
    /**
     * Inserts or updates multiple exchange rates (bulk operation)
     * Uses REPLACE strategy to handle updates
     * 
     * @param rates List of exchange rate entities to insert/update
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRates(rates: List<ExchangeRateEntity>)
    
    /**
     * Deletes exchange rates older than the specified date
     * Used for cleanup of old cached rates
     * 
     * @param olderThan Date in ISO format (YYYY-MM-DD). Rates with date older than this will be deleted
     */
    @Query("DELETE FROM exchange_rates WHERE date < :olderThan")
    suspend fun deleteOldRates(olderThan: String)
    
    /**
     * Gets the count of cached exchange rates
     * 
     * @return The total number of exchange rates in the database
     */
    @Query("SELECT COUNT(*) FROM exchange_rates")
    suspend fun getRateCount(): Int
    
    /**
     * Deletes all exchange rates for a specific base currency
     * Useful when refreshing rates for a base currency
     * 
     * @param baseCurrency Base currency code
     */
    @Query("DELETE FROM exchange_rates WHERE baseCurrency = :baseCurrency")
    suspend fun deleteRatesForBaseCurrency(baseCurrency: String)
    
    /**
     * Gets all rates for a base currency synchronously (for one-time reads)
     * 
     * @param baseCurrency Base currency code
     * @param date Date in ISO format (YYYY-MM-DD). If null, gets the latest rates
     * @return List of exchange rate entities
     */
    @Query("""
        SELECT * FROM exchange_rates 
        WHERE baseCurrency = :baseCurrency 
        AND (:date IS NULL OR date = :date)
        ORDER BY date DESC, targetCurrency
    """)
    suspend fun getRatesSync(baseCurrency: String, date: String?): List<ExchangeRateEntity>
    
    /**
     * Gets all distinct base currencies that have rates stored
     * Useful for finding available base currencies for cross-rate calculations
     * 
     * @return List of distinct base currency codes
     */
    @Query("SELECT DISTINCT baseCurrency FROM exchange_rates ORDER BY baseCurrency")
    suspend fun getAvailableBaseCurrencies(): List<String>
}

