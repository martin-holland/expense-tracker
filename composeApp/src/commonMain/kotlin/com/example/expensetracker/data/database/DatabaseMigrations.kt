package com.example.expensetracker.data.database

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection

/**
 * Database migrations for ExpenseDatabase
 * 
 * Migrations handle schema changes between database versions
 * 
 * Note: For Room KMP with BundledSQLiteDriver, we must use SQLiteConnection
 * instead of SupportSQLiteDatabase. The prepare() method returns a PreparedStatement
 * that must be executed.
 */

/**
 * Migration from version 1 to version 2
 * Adds the settings table with default values
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SQLiteConnection) {
        // Create settings table
        val createTableSQL = """
            CREATE TABLE IF NOT EXISTS settings (
                id TEXT NOT NULL PRIMARY KEY,
                baseCurrency TEXT NOT NULL,
                lastExchangeRateUpdate TEXT,
                exchangeRateApiKey TEXT NOT NULL,
                exchangeRateApiBaseUrl TEXT NOT NULL
            )
        """.trimIndent()
        
        val createStatement = database.prepare(createTableSQL)
        try {
            createStatement.step()
        } finally {
            createStatement.close()
        }
        
        // Insert default settings row
        val insertSQL = """
            INSERT INTO settings (id, baseCurrency, lastExchangeRateUpdate, exchangeRateApiKey, exchangeRateApiBaseUrl)
            VALUES ('settings', 'USD', NULL, '', 'https://v6.exchangerate-api.com/v6')
        """.trimIndent()
        
        val insertStatement = database.prepare(insertSQL)
        try {
            insertStatement.step()
        } finally {
            insertStatement.close()
        }
    }
}

