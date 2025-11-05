package com.example.expensetracker.model

import kotlinx.datetime.LocalDateTime

/**
 * Data class representing an expense item
 * 
 * @param id Unique identifier for the expense
 * @param category Category of the expense (Food, Travel, Utilities, Other)
 * @param description Brief description of the expense
 * @param amount Expense amount
 * @param currency Currency of the expense
 * @param date Date and time when the expense was made
 */
data class Expense(
    val id: String,
    val category: ExpenseCategory,
    val description: String,
    val amount: Double,
    val currency: Currency,
    val date: LocalDateTime
) {
    /**
     * Returns formatted amount with currency symbol
     */
    fun getFormattedAmount(): String = currency.format(amount)
}

