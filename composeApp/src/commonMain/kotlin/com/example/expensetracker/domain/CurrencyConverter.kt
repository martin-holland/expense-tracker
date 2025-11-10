package com.example.expensetracker.domain

import com.example.expensetracker.data.repository.ExchangeRateRepository
import com.example.expensetracker.data.repository.SettingsRepository
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateTime

/**
 * Service for converting amounts and expenses between currencies
 * 
 * This service provides business logic for currency conversion with cross-rate optimization.
 * It leverages the ExchangeRateRepository which fetches ALL rates for a base currency in a
 * single API call, then calculates any currency pair conversion locally.
 * 
 * Key Optimization:
 * - Single API call fetches ALL rates for base currency
 * - All conversions calculated from those rates (no additional API calls)
 * - Dramatically reduces API usage
 * 
 * Usage:
 * ```kotlin
 * val converter = CurrencyConverter.getInstance()
 * val convertedAmount = converter.convertAmountSync(100.0, Currency.EUR, Currency.GBP)
 * val convertedExpense = converter.convertToBaseCurrencySync(expense)
 * ```
 */
class CurrencyConverter private constructor(
    private val exchangeRateRepository: ExchangeRateRepository,
    private val settingsRepository: SettingsRepository
) {
    
    companion object {
        private var instance: CurrencyConverter? = null
        
        /**
         * Gets the singleton instance of CurrencyConverter
         * 
         * @return The singleton CurrencyConverter instance
         */
        fun getInstance(): CurrencyConverter {
            if (instance != null) return instance!!
            
            return instance ?: run {
                val exchangeRateRepository = ExchangeRateRepository.getInstance()
                val settingsRepository = SettingsRepository.getInstance()
                CurrencyConverter(exchangeRateRepository, settingsRepository)
            }.also { instance = it }
        }
        
        /**
         * Resets the singleton instance (useful for testing)
         */
        fun resetInstance() {
            instance = null
        }
    }
    
    /**
     * Converts an expense to the base currency
     * Returns a Flow that emits the converted expense
     * 
     * @param expense The expense to convert
     * @return Flow emitting the expense with amount converted to base currency
     */
    fun convertToBaseCurrency(expense: Expense): Flow<Expense> {
        val baseCurrency = settingsRepository.getBaseCurrency()
        
        return baseCurrency.map { base ->
            val convertedAmount = convertAmountSync(
                expense.amount,
                expense.currency,
                base,
                expense.date
            )
            
            // If conversion failed, return original expense
            // In production, you might want to handle this differently
            expense.copy(
                amount = convertedAmount ?: expense.amount,
                currency = base
            )
        }
    }
    
    /**
     * Converts an expense to the base currency synchronously
     * 
     * @param expense The expense to convert
     * @return The expense with amount converted to base currency
     */
    suspend fun convertToBaseCurrencySync(expense: Expense): Expense {
        val baseCurrency = settingsRepository.getBaseCurrencySync()
        val convertedAmount = convertAmountSync(
            expense.amount,
            expense.currency,
            baseCurrency,
            expense.date
        )
        
        // If conversion failed, return original expense
        // In production, you might want to handle this differently
        return expense.copy(
            amount = convertedAmount ?: expense.amount,
            currency = baseCurrency
        )
    }
    
    /**
     * Converts an amount from one currency to another
     * Returns a Flow that emits the converted amount
     * 
     * @param amount The amount to convert
     * @param fromCurrency Source currency
     * @param toCurrency Target currency
     * @param date Optional date for historical rates. If null, uses latest rate
     * @return Flow emitting the converted amount, or null if conversion failed
     */
    fun convertAmount(
        amount: Double,
        fromCurrency: Currency,
        toCurrency: Currency,
        date: LocalDateTime? = null
    ): Flow<Double?> {
        // If same currency, return amount as-is
        if (fromCurrency == toCurrency) {
            return kotlinx.coroutines.flow.flowOf(amount)
        }
        
        // Get exchange rate
        return exchangeRateRepository.getExchangeRate(fromCurrency, toCurrency, date)
            .map { rate ->
                rate?.let { amount * it }
            }
    }
    
    /**
     * Converts an amount from one currency to another synchronously
     * 
     * This method implements optimized conversion logic:
     * 1. If same currency, return amount as-is
     * 2. Try direct rate lookup first
     * 3. Use cross-rate calculation via base currency (optimized)
     * 4. Fallback to reverse rate if available
     * 
     * @param amount The amount to convert
     * @param fromCurrency Source currency
     * @param toCurrency Target currency
     * @param date Optional date for historical rates. If null, uses latest rate
     * @return The converted amount, or null if conversion failed
     */
    suspend fun convertAmountSync(
        amount: Double,
        fromCurrency: Currency,
        toCurrency: Currency,
        date: LocalDateTime? = null
    ): Double? {
        // If same currency, return amount as-is
        if (fromCurrency == toCurrency) {
            return amount
        }
        
        // Get exchange rate (this already handles cross-rate calculation)
        val rate = exchangeRateRepository.getExchangeRateSync(fromCurrency, toCurrency, date)
        
        return rate?.let { 
            val converted = amount * it
            
            // Handle division by zero edge case (shouldn't happen, but safety check)
            if (it.isNaN() || it.isInfinite()) {
                null
            } else {
                converted
            }
        }
    }
    
    /**
     * Converts a list of expenses to the base currency
     * 
     * @param expenses List of expenses to convert
     * @return List of expenses with amounts converted to base currency
     */
    suspend fun convertExpensesToBaseCurrency(expenses: List<Expense>): List<Expense> {
        val baseCurrency = settingsRepository.getBaseCurrencySync()
        
        return expenses.map { expense ->
            if (expense.currency == baseCurrency) {
                expense // Already in base currency
            } else {
                val convertedAmount = convertAmountSync(
                    expense.amount,
                    expense.currency,
                    baseCurrency,
                    expense.date
                )
                
                expense.copy(
                    amount = convertedAmount ?: expense.amount,
                    currency = baseCurrency
                )
            }
        }
    }
}

