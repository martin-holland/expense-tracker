package com.example.expensetracker.data.repository

import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.ExpenseCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

/**
 * Interface for Expense Repository Enables dependency injection and testing with fake
 * implementations
 */
interface IExpenseRepository {

    /** Get all expenses as a Flow */
    fun getAllExpenses(): Flow<List<Expense>>

    /** Get expense by ID */
    suspend fun getExpenseById(id: String): Expense?

    /** Get expenses by category */
    fun getExpensesByCategory(category: ExpenseCategory): Flow<List<Expense>>

    /** Insert a new expense */
    suspend fun insertExpense(expense: Expense)

    /** Insert multiple expenses */
    suspend fun insertExpenses(expenses: List<Expense>)

    /** Update an expense */
    suspend fun updateExpense(expense: Expense)

    /** Delete an expense */
    suspend fun deleteExpense(expense: Expense)

    /** Delete expense by ID */
    suspend fun deleteExpenseById(id: String)

    /** Get expense count */
    suspend fun getExpenseCount(): Int

    /** Get expenses by date range */
    fun getExpensesByDateRange(
            startDate: LocalDateTime,
            endDate: LocalDateTime
    ): Flow<List<Expense>>

    /** Get expenses by amount range */
    fun getExpensesByAmountRange(minAmount: Double, maxAmount: Double): Flow<List<Expense>>
}
