package com.example.expensetracker.data.database

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/**
 * Expected function to get the Room database instance
 * Platform-specific implementations will provide the actual database builder
 * 
 * Usage:
 * ```kotlin
 * val database = getRoomDatabase()
 * val dao = database.expenseDao()
 * ```
 */
expect fun getRoomDatabase(): ExpenseDatabase

/**
 * Expected function to get the database builder
 * This is used internally by platform implementations
 * You should use getRoomDatabase() instead
 */
expect fun getDatabaseBuilder(): RoomDatabase.Builder<ExpenseDatabase>

/**
 * Internal function to instantiate the database implementation
 * This is used by Room to create the database instance
 */
internal fun createDatabase(builder: RoomDatabase.Builder<ExpenseDatabase>): ExpenseDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
        .build()
}

