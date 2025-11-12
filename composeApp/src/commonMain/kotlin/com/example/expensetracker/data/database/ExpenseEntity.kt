package com.example.expensetracker.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.ExpenseCategory
import kotlinx.datetime.LocalDateTime

/**
 * Room Entity representing an expense in the database
 * This is the database representation of an Expense
 * 
 * @property id Unique identifier (primary key)
 * @property category Expense category (Food, Travel, Utilities, Other)
 * @property description Description of the expense
 * @property amount Expense amount
 * @property currency Currency code
 * @property date Date and time when expense was made
 */
@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey
    val id: String,
    val category: ExpenseCategory,
    val description: String,
    val amount: Double,
    val currency: Currency,
    val date: LocalDateTime
)

/**
 * Extension function to convert ExpenseEntity to domain model Expense
 */
fun ExpenseEntity.toExpense(): Expense {
    return Expense(
        id = id,
        category = category,
        description = description,
        amount = amount,
        currency = currency,
        date = date
    )
}

/**
 * Extension function to convert domain model Expense to ExpenseEntity
 */
fun Expense.toEntity(): ExpenseEntity {
    return ExpenseEntity(
        id = id,
        category = category,
        description = description,
        amount = amount,
        currency = currency,
        date = date
    )
}

