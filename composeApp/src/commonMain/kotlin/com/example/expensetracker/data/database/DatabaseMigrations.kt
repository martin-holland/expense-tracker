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

/**
 * Migration from version 2 to version 3
 * Adds the exchange_rates table with indexes
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SQLiteConnection) {
        // Create exchange_rates table
        val createTableSQL = """
            CREATE TABLE IF NOT EXISTS exchange_rates (
                id TEXT NOT NULL PRIMARY KEY,
                baseCurrency TEXT NOT NULL,
                targetCurrency TEXT NOT NULL,
                rate REAL NOT NULL,
                date TEXT NOT NULL,
                lastUpdated TEXT NOT NULL
            )
        """.trimIndent()
        
        val createStatement = database.prepare(createTableSQL)
        try {
            createStatement.step()
        } finally {
            createStatement.close()
        }
        
        // Create index on (baseCurrency, date)
        val createIndex1SQL = """
            CREATE INDEX IF NOT EXISTS idx_exchange_rates_base_date 
            ON exchange_rates(baseCurrency, date)
        """.trimIndent()
        
        val index1Statement = database.prepare(createIndex1SQL)
        try {
            index1Statement.step()
        } finally {
            index1Statement.close()
        }
        
        // Create index on (baseCurrency, targetCurrency, date)
        val createIndex2SQL = """
            CREATE INDEX IF NOT EXISTS idx_exchange_rates_base_target_date 
            ON exchange_rates(baseCurrency, targetCurrency, date)
        """.trimIndent()
        
        val index2Statement = database.prepare(createIndex2SQL)
        try {
            index2Statement.step()
        } finally {
            index2Statement.close()
        }
    }
}

/**
 * Migration from version 3 to version 4
 * Adds theme and voice input settings columns to the settings table
 * 
 * Changes:
 * - Added 'themeOption' column (default: "SYSTEM")
 * - Added 'isVoiceInputEnabled' column (default: 0/false)
 * 
 * This migration preserves all existing settings data.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SQLiteConnection) {
        // Add themeOption column with default value "SYSTEM"
        val addThemeColumnSQL = """
            ALTER TABLE settings 
            ADD COLUMN themeOption TEXT NOT NULL DEFAULT 'SYSTEM'
        """.trimIndent()
        
        val addThemeStatement = database.prepare(addThemeColumnSQL)
        try {
            addThemeStatement.step()
        } finally {
            addThemeStatement.close()
        }
        
        // Add isVoiceInputEnabled column with default value 0 (false)
        val addVoiceColumnSQL = """
            ALTER TABLE settings 
            ADD COLUMN isVoiceInputEnabled INTEGER NOT NULL DEFAULT 0
        """.trimIndent()
        
        val addVoiceStatement = database.prepare(addVoiceColumnSQL)
        try {
            addVoiceStatement.step()
        } finally {
            addVoiceStatement.close()
        }
        
        println("✅ Database migration 3→4 completed successfully")
        println("   - Added themeOption column (default: SYSTEM)")
        println("   - Added isVoiceInputEnabled column (default: false)")
    }
}

