package com.example.expensetracker.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for Expense operations
 * Defines all database operations for the expenses table
 */
@Dao
interface ExpenseDao {
    
    /**
     * Gets all expenses from the database
     * Returns a Flow that emits the list whenever data changes
     * @return Flow of all expenses ordered by date (newest first)
     */
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>
    
    /**
     * Gets a single expense by its ID
     * @param id The unique identifier of the expense
     * @return The expense if found, null otherwise
     */
    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: String): ExpenseEntity?
    
    /**
     * Gets expenses by category
     * @param category The category to filter by
     * @return Flow of expenses in the specified category
     */
    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY date DESC")
    fun getExpensesByCategory(category: String): Flow<List<ExpenseEntity>>
    
    /**
     * Inserts a new expense into the database
     * If an expense with the same ID exists, it will be replaced
     * @param expense The expense to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)
    
    /**
     * Inserts multiple expenses into the database
     * Used for seeding initial data
     * @param expenses List of expenses to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)
    
    /**
     * Updates an existing expense
     * @param expense The expense to update
     */
    @Update
    suspend fun updateExpense(expense: ExpenseEntity)
    
    /**
     * Deletes an expense from the database
     * @param expense The expense to delete
     */
    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)
    
    /**
     * Deletes an expense by its ID
     * @param id The unique identifier of the expense to delete
     */
    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: String)
    
    /**
     * Deletes all expenses from the database
     * Use with caution!
     */
    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()
    
    /**
     * Gets the count of expenses in the database
     * @return The total number of expenses
     */
    @Query("SELECT COUNT(*) FROM expenses")
    suspend fun getExpenseCount(): Int
    
    /**
     * Gets expenses within a date range
     * @param startDate Start of the date range (ISO-8601 string)
     * @param endDate End of the date range (ISO-8601 string)
     * @return Flow of expenses within the date range
     */
    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByDateRange(startDate: String, endDate: String): Flow<List<ExpenseEntity>>
    
    /**
     * Gets expenses within an amount range
     * @param minAmount Minimum amount
     * @param maxAmount Maximum amount
     * @return Flow of expenses within the amount range
     */
    @Query("SELECT * FROM expenses WHERE amount BETWEEN :minAmount AND :maxAmount ORDER BY date DESC")
    fun getExpensesByAmountRange(minAmount: Double, maxAmount: Double): Flow<List<ExpenseEntity>>
}

