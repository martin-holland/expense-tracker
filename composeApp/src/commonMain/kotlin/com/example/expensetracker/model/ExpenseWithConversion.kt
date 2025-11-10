package com.example.expensetracker.model

/**
 * Data class representing an expense with its converted amount
 * 
 * Used for displaying expenses with currency conversion information
 * 
 * @param expense The original expense
 * @param convertedAmount The converted amount in base currency (nullable if conversion fails)
 * @param baseCurrency The base currency used for conversion
 */
data class ExpenseWithConversion(
    val expense: Expense,
    val convertedAmount: Double?,
    val baseCurrency: Currency
)

