package com.example.expensetracker.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters

/**
 * Room Database class for the Expense Tracker application
 * 
 * This is the main database configuration:
 * - Entities: ExpenseEntity, SettingsEntity, ExchangeRateEntity
 * - Version: 3
 * - Type Converters: Converters (for LocalDateTime, Currency, ExpenseCategory)
 * 
 * To use this database:
 * 1. Get an instance using the platform-specific builder (getRoomDatabase function)
 * 2. Access the DAO: database.expenseDao(), database.settingsDao(), or database.exchangeRateDao()
 * 3. Use the DAO methods to perform database operations
 * 
 * Note: This database uses platform-specific builders (expect/actual pattern)
 * with manual instantiation for better cross-platform compatibility.
 */
@Database(
    entities = [ExpenseEntity::class, SettingsEntity::class, ExchangeRateEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
@ConstructedBy(ExpenseDatabaseConstructor::class)
abstract class ExpenseDatabase : RoomDatabase() {
    
    /**
     * Provides access to the ExpenseDao for database operations
     */
    abstract fun expenseDao(): ExpenseDao
    
    /**
     * Provides access to the SettingsDao for database operations
     */
    abstract fun settingsDao(): SettingsDao
    
    /**
     * Provides access to the ExchangeRateDao for database operations
     */
    abstract fun exchangeRateDao(): ExchangeRateDao
    
    companion object {
        const val DATABASE_NAME = "expense_tracker.db"
    }
}

/**
 * Constructor for ExpenseDatabase
 * Platform-specific implementations are generated automatically by Room KSP
 * The @Suppress annotation prevents IDE warnings about missing actual implementations
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object ExpenseDatabaseConstructor : RoomDatabaseConstructor<ExpenseDatabase>

