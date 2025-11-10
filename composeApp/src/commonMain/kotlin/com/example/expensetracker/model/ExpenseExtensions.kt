package com.example.expensetracker.model

import com.example.expensetracker.domain.CurrencyConverter
import kotlinx.coroutines.flow.Flow

/**
 * Extension functions for Expense model
 * These functions provide additional functionality without modifying the core Expense data class
 */

/**
 * Gets the converted amount of an expense in the specified base currency
 * 
 * This is a convenience extension function that uses CurrencyConverter to convert
 * the expense amount to the base currency while preserving the original expense.
 * 
 * @param baseCurrency The target currency to convert to
 * @param converter The CurrencyConverter instance to use for conversion
 * @return Flow emitting the converted amount, or null if conversion failed
 * 
 * Example:
 * ```kotlin
 * val converter = CurrencyConverter.getInstance()
 * expense.getConvertedAmount(Currency.USD, converter)
 *     .collect { convertedAmount ->
 *         // Use converted amount
 *     }
 * ```
 */
fun Expense.getConvertedAmount(
    baseCurrency: Currency,
    converter: CurrencyConverter
): Flow<Double?> {
    return converter.convertAmount(
        amount = this.amount,
        fromCurrency = this.currency,
        toCurrency = baseCurrency,
        date = this.date
    )
}

