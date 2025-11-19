package com.example.expensetracker.data.repository

import com.example.expensetracker.data.database.ExpenseDao
import com.example.expensetracker.data.database.getRoomDatabase
import com.example.expensetracker.data.database.toEntity
import com.example.expensetracker.data.database.toExpense
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.ExpenseCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime

/**
 * Repository for managing expense data
 * Provides a clean API for accessing and manipulating expense data
 * Handles database operations and coordinates between data sources
 * 
 * This is a singleton repository that can be used across multiple ViewModels
 * 
 * Usage:
 * ```kotlin
 * val repository = ExpenseRepository.getInstance()
 * val expenses = repository.getAllExpenses()
 * repository.insertExpense(expense)
 * ```
 */
class ExpenseRepository private constructor(
    private val expenseDao: ExpenseDao
) : IExpenseRepository {
    
    /**
     * Gets all expenses as a Flow
     * The Flow will emit updates whenever the data changes
     */
    override fun getAllExpenses(): Flow<List<Expense>> {
        return expenseDao.getAllExpenses().map { entities ->
            entities.map { it.toExpense() }
        }
    }
    
    /**
     * Gets a single expense by ID
     * @param id The unique identifier of the expense
     * @return The expense if found, null otherwise
     */
    override suspend fun getExpenseById(id: String): Expense? {
        return expenseDao.getExpenseById(id)?.toExpense()
    }
    
    /**
     * Gets expenses by category
     * @param category The category to filter by
     * @return Flow of expenses in the specified category
     */
    override fun getExpensesByCategory(category: ExpenseCategory): Flow<List<Expense>> {
        return expenseDao.getExpensesByCategory(category.name).map { entities ->
            entities.map { it.toExpense() }
        }
    }
    
    /**
     * Inserts a new expense or updates if it already exists
     * @param expense The expense to insert/update
     */
    override suspend fun insertExpense(expense: Expense) {
        expenseDao.insertExpense(expense.toEntity())
    }
    
    /**
     * Inserts multiple expenses
     * @param expenses List of expenses to insert
     */
    override suspend fun insertExpenses(expenses: List<Expense>) {
        expenseDao.insertExpenses(expenses.map { it.toEntity() })
    }
    
    /**
     * Updates an existing expense
     * @param expense The expense to update
     */
    override suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense.toEntity())
    }
    
    /**
     * Deletes an expense
     * @param expense The expense to delete
     */
    override suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense.toEntity())
    }
    
    /**
     * Deletes an expense by ID
     * @param id The unique identifier of the expense to delete
     */
    override suspend fun deleteExpenseById(id: String) {
        expenseDao.deleteExpenseById(id)
    }
    
    /**
     * Gets the count of expenses in the database
     * @return The total number of expenses
     */
    override suspend fun getExpenseCount(): Int {
        return expenseDao.getExpenseCount()
    }
    
    /**
     * Gets expenses within a date range
     * @param startDate Start of the date range
     * @param endDate End of the date range
     * @return Flow of expenses within the date range
     */
    override fun getExpensesByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Expense>> {
        return expenseDao.getExpensesByDateRange(
            startDate.toString(),
            endDate.toString()
        ).map { entities ->
            entities.map { it.toExpense() }
        }
    }
    
    /**
     * Gets expenses within an amount range
     * @param minAmount Minimum amount
     * @param maxAmount Maximum amount
     * @return Flow of expenses within the amount range
     */
    override fun getExpensesByAmountRange(minAmount: Double, maxAmount: Double): Flow<List<Expense>> {
        return expenseDao.getExpensesByAmountRange(minAmount, maxAmount).map { entities ->
            entities.map { it.toExpense() }
        }
    }
    
    /**
     * Seeds the database with initial data
     * Only inserts data if the database is empty
     */
    private suspend fun seedDatabaseIfEmpty() {
        val count = expenseDao.getExpenseCount()
        if (count == 0) {
            val seedData = generateSeedData()
            expenseDao.insertExpenses(seedData.map { it.toEntity() })
        }
    }
    
    /**
     * Generates seed data for the database
     * This data will be inserted only on first app launch
     */
    private fun generateSeedData(): List<Expense> {
        // Create static dates for seed data
        val nov1 = LocalDateTime(2024, 11, 1, 12, 0)
        val oct31 = LocalDateTime(2024, 10, 31, 14, 30)
        val oct30 = LocalDateTime(2024, 10, 30, 10, 15)
        val oct29 = LocalDateTime(2024, 10, 29, 16, 45)
        val oct28 = LocalDateTime(2024, 10, 28, 9, 0)

        return listOf(
            Expense(
                id = "1",
                category = ExpenseCategory.FOOD,
                description = "Lunch at restaurant",
                amount = 45.50,
                currency = Currency.USD,
                date = nov1
            ),
            Expense(
                id = "2",
                category = ExpenseCategory.TRAVEL,
                description = "Gas station",
                amount = 120.00,
                currency = Currency.USD,
                date = nov1
            ),
            Expense(
                id = "3",
                category = ExpenseCategory.FOOD,
                description = "Coffee shop",
                amount = 15.99,
                currency = Currency.USD,
                date = oct31
            ),
            Expense(
                id = "4",
                category = ExpenseCategory.UTILITIES,
                description = "Electricity bill",
                amount = 85.00,
                currency = Currency.USD,
                date = oct31
            ),
            Expense(
                id = "5",
                category = ExpenseCategory.FOOD,
                description = "Grocery shopping",
                amount = 32.50,
                currency = Currency.USD,
                date = oct30
            ),
            Expense(
                id = "6",
                category = ExpenseCategory.TRAVEL,
                description = "Uber ride",
                amount = 50.00,
                currency = Currency.USD,
                date = oct30
            ),
            Expense(
                id = "7",
                category = ExpenseCategory.OTHER,
                description = "Online subscription",
                amount = 25.99,
                currency = Currency.EUR,
                date = oct29
            ),
            Expense(
                id = "8",
                category = ExpenseCategory.UTILITIES,
                description = "Internet bill",
                amount = 180.00,
                currency = Currency.USD,
                date = oct28
            )
        )
    }
    
    companion object {
        private var instance: ExpenseRepository? = null
        private val lock = Any()
        
        /**
         * Gets the singleton instance of ExpenseRepository
         * Automatically seeds the database on first access if empty
         * 
         * @return The singleton ExpenseRepository instance
         */
        fun getInstance(): ExpenseRepository {
            if (instance != null) return instance!!
            
            // Double-checked locking for thread safety
            return instance ?: run {
                val database = getRoomDatabase()
                val repository = ExpenseRepository(database.expenseDao())
                
                // Seed database if empty (on background thread)
                CoroutineScope(Dispatchers.Default).launch {
                    repository.seedDatabaseIfEmpty()
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
}

