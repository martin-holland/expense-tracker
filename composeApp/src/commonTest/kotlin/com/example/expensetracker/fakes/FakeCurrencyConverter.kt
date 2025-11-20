package com.example.expensetracker.fakes

import com.example.expensetracker.domain.ICurrencyConverter
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDateTime

/**
 * Fake implementation of ICurrencyConverter for testing
 * 
 * Provides simple, predictable conversion logic for unit tests without requiring
 * actual exchange rate data or database access.
 */
class FakeCurrencyConverter : ICurrencyConverter {
    
    // Simple exchange rates for testing (all relative to USD = 1.0)
    private val exchangeRates = mapOf(
        Currency.USD to 1.0,
        Currency.EUR to 0.85,
        Currency.GBP to 0.73,
        Currency.JPY to 110.0,
        Currency.CAD to 1.25,
        Currency.AUD to 1.35,
        Currency.CHF to 0.92,
        Currency.CNY to 6.45,
        Currency.INR to 74.0,
        Currency.SEK to 9.5,
        Currency.NOK to 9.8,
        Currency.DKK to 6.3
    )
    
    // Base currency for conversions (can be changed in tests)
    var baseCurrency: Currency = Currency.USD
    
    override fun convertToBaseCurrency(expense: Expense): Flow<Expense> {
        val convertedAmount = if (expense.currency == baseCurrency) {
            expense.amount
        } else {
            convertAmount(expense.amount, expense.currency, baseCurrency)
        }
        
        return flowOf(expense.copy(
            amount = convertedAmount,
            currency = baseCurrency
        ))
    }
    
    override suspend fun convertToBaseCurrencySync(expense: Expense): Expense {
        val convertedAmount = if (expense.currency == baseCurrency) {
            expense.amount
        } else {
            convertAmount(expense.amount, expense.currency, baseCurrency)
        }
        
        return expense.copy(
            amount = convertedAmount,
            currency = baseCurrency
        )
    }
    
    override fun convertAmount(
        amount: Double,
        fromCurrency: Currency,
        toCurrency: Currency,
        date: LocalDateTime?
    ): Flow<Double?> {
        return flowOf(convertAmount(amount, fromCurrency, toCurrency))
    }
    
    override suspend fun convertAmountSync(
        amount: Double,
        fromCurrency: Currency,
        toCurrency: Currency,
        date: LocalDateTime?
    ): Double? {
        return convertAmount(amount, fromCurrency, toCurrency)
    }
    
    override suspend fun convertExpensesToBaseCurrency(expenses: List<Expense>): List<Expense> {
        return expenses.map { expense ->
            if (expense.currency == baseCurrency) {
                expense
            } else {
                val convertedAmount = convertAmount(expense.amount, expense.currency, baseCurrency)
                expense.copy(
                    amount = convertedAmount,
                    currency = baseCurrency
                )
            }
        }
    }
    
    /**
     * Internal conversion logic using simple fixed rates
     */
    private fun convertAmount(
        amount: Double,
        fromCurrency: Currency,
        toCurrency: Currency
    ): Double {
        if (fromCurrency == toCurrency) {
            return amount
        }
        
        val fromRate = exchangeRates[fromCurrency] ?: 1.0
        val toRate = exchangeRates[toCurrency] ?: 1.0
        
        // Convert to USD first, then to target currency
        val amountInUsd = amount / fromRate
        return amountInUsd * toRate
    }
}

