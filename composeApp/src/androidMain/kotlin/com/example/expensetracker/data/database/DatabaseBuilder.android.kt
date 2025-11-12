package com.example.expensetracker.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Android-specific application context holder
 * Must be initialized in MainActivity before using the database
 */
object AndroidDatabaseContext {
    lateinit var context: Context
        private set
    
    fun init(context: Context) {
        this.context = context.applicationContext
    }
}

/**
 * Android implementation of the database builder
 * Creates a Room database instance for Android
 */
actual fun getDatabaseBuilder(): RoomDatabase.Builder<ExpenseDatabase> {
    val appContext = AndroidDatabaseContext.context
    val dbFile = appContext.getDatabasePath(ExpenseDatabase.DATABASE_NAME)
    return Room.databaseBuilder<ExpenseDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}

/**
 * Android implementation to get the database instance
 * Returns a singleton database instance
 */
private var databaseInstance: ExpenseDatabase? = null

actual fun getRoomDatabase(): ExpenseDatabase {
    return databaseInstance ?: synchronized(AndroidDatabaseContext) {
        databaseInstance ?: createDatabase(
            getDatabaseBuilder()
                .fallbackToDestructiveMigrationOnDowngrade(true)
        ).also { databaseInstance = it }
    }
}

