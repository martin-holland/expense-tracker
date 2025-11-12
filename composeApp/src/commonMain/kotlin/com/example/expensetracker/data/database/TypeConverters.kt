package com.example.expensetracker.data.database

import androidx.room.TypeConverter
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.ExpenseCategory
import kotlinx.datetime.LocalDateTime

/**
 * Room TypeConverters for converting complex types to/from database-storable types
 * These converters are used by Room to handle non-primitive types in the database
 */
class Converters {
    
    /**
     * Converts LocalDateTime to ISO-8601 string format for database storage
     * Format: YYYY-MM-DDTHH:MM:SS
     */
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.toString()
    }
    
    /**
     * Converts ISO-8601 string from database to LocalDateTime
     */
    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }
    
    /**
     * Converts ExpenseCategory enum to its string name for database storage
     */
    @TypeConverter
    fun fromExpenseCategory(value: ExpenseCategory?): String? {
        return value?.name
    }
    
    /**
     * Converts string from database to ExpenseCategory enum
     */
    @TypeConverter
    fun toExpenseCategory(value: String?): ExpenseCategory? {
        return value?.let { ExpenseCategory.valueOf(it) }
    }
    
    /**
     * Converts Currency enum to its code string for database storage
     */
    @TypeConverter
    fun fromCurrency(value: Currency?): String? {
        return value?.code
    }
    
    /**
     * Converts currency code string from database to Currency enum
     */
    @TypeConverter
    fun toCurrency(value: String?): Currency? {
        return value?.let { Currency.fromCode(it) }
    }
}

