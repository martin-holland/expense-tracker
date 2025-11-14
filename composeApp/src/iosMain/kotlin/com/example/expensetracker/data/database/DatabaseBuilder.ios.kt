package com.example.expensetracker.data.database

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * iOS implementation of the database builder
 * Creates a Room database instance for iOS
 */
@OptIn(ExperimentalForeignApi::class)
actual fun getDatabaseBuilder(): RoomDatabase.Builder<ExpenseDatabase> {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )
    val dbFilePath = documentDirectory!!.path + "/${ExpenseDatabase.DATABASE_NAME}"
    return Room.databaseBuilder<ExpenseDatabase>(
        name = dbFilePath
    )
}

/**
 * iOS implementation to get the database instance
 * Returns a singleton database instance
 */
private var databaseInstance: ExpenseDatabase? = null

actual fun getRoomDatabase(): ExpenseDatabase {
    return databaseInstance ?: createDatabase(
        getDatabaseBuilder()
            // Proper migrations are defined in DatabaseMigrations.kt
            // This ensures data is preserved during schema updates
            .fallbackToDestructiveMigrationOnDowngrade(true)
    ).also { databaseInstance = it }
}

