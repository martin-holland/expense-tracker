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
 * - Entities: ExpenseEntity
 * - Version: 1
 * - Type Converters: Converters (for LocalDateTime, Currency, ExpenseCategory)
 *
 * To use this database:
 * 1. Get an instance using the platform-specific builder (getRoomDatabase function)
 * 2. Access the DAO: database.expenseDao()
 * 3. Use the DAO methods to perform database operations
 *
 * Note: This database uses platform-specific builders (expect/actual pattern) with manual
 * instantiation for better cross-platform compatibility.
 */
@Database(entities = [ExpenseEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
@ConstructedBy(ExpenseDatabaseConstructor::class)
abstract class ExpenseDatabase : RoomDatabase() {

    /** Provides access to the ExpenseDao for database operations */
    abstract fun expenseDao(): ExpenseDao

    companion object {
        const val DATABASE_NAME = "expense_tracker.db"
    }
}

/**
 * Constructor for ExpenseDatabase Platform-specific implementations are generated automatically by
 * Room KSP The @Suppress annotation prevents IDE warnings about missing actual implementations
 * Note: Room generates the actual in metadata/commonMain, which causes a same-module warning This
 * is a known limitation and can be safely ignored
 */
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object ExpenseDatabaseConstructor : RoomDatabaseConstructor<ExpenseDatabase>
