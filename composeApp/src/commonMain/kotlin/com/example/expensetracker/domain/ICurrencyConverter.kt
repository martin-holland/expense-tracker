package com.example.expensetracker.domain

import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

/**
 * Interface for currency conversion services
 * 
 * Enables dependency injection and testing by allowing fake implementations
 * to be injected into ViewModels.
 */
interface ICurrencyConverter {
    
    /**
     * Converts an expense to the base currency
     * Returns a Flow that emits the converted expense
     * 
     * @param expense The expense to convert
     * @return Flow emitting the expense with amount converted to base currency
     */
    fun convertToBaseCurrency(expense: Expense): Flow<Expense>
    
    /**
     * Converts an expense to the base currency synchronously
     * 
     * @param expense The expense to convert
     * @return The expense with amount converted to base currency
     */
    suspend fun convertToBaseCurrencySync(expense: Expense): Expense
    
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
    ): Flow<Double?>
    
    /**
     * Converts an amount from one currency to another synchronously
     * 
     * @param amount The amount to convert
     * @param fromCurrency Source currency
     * @param toCurrency Target currency
     * @param date Optional date for historical rates. If null, uses latest rate
     * @return The converted amount, or null if no cached rate exists
     */
    suspend fun convertAmountSync(
        amount: Double,
        fromCurrency: Currency,
        toCurrency: Currency,
        date: LocalDateTime? = null
    ): Double?
    
    /**
     * Converts a list of expenses to the base currency
     * 
     * @param expenses List of expenses to convert
     * @return List of expenses with amounts converted to base currency
     */
    suspend fun convertExpensesToBaseCurrency(expenses: List<Expense>): List<Expense>
}

