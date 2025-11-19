package com.example.expensetracker.helpers

import com.example.expensetracker.model.*
import kotlinx.datetime.LocalDateTime
import kotlin.random.Random

/**
 * Test data factory for creating test instances
 */
object TestData {
    
    /**
     * Create a test Expense with customizable properties
     */
    fun createExpense(
        id: String = "test-${Random.nextInt()}",
        category: ExpenseCategory = ExpenseCategory.FOOD,
        description: String = "Test expense",
        amount: Double = 50.0,
        currency: Currency = Currency.USD,
        date: LocalDateTime = LocalDateTime(2024, 11, 15, 12, 0)
    ): Expense = Expense(
        id = id,
        category = category,
        description = description,
        amount = amount,
        currency = currency,
        date = date
    )
    
    /**
     * Create multiple test expenses
     */
    fun createExpenses(count: Int): List<Expense> {
        return List(count) { index ->
            createExpense(
                id = "test-$index",
                amount = (index + 1) * 10.0
            )
        }
    }
    
    /**
     * Create expense with specific date
     */
    fun createExpenseWithDate(
        year: Int,
        month: Int,
        day: Int,
        amount: Double = 50.0,
        category: ExpenseCategory = ExpenseCategory.FOOD
    ): Expense = createExpense(
        date = LocalDateTime(year, month, day, 12, 0),
        amount = amount,
        category = category
    )
    
    /**
     * Create test AppSettings
     */
    fun createSettings(
        baseCurrency: Currency = Currency.USD,
        apiKey: String = "",
        apiBaseUrl: String = "https://v6.exchangerate-api.com/v6",
        themeOption: ThemeOption = ThemeOption.SYSTEM,
        isVoiceInputEnabled: Boolean = false
    ): AppSettings = AppSettings(
        baseCurrency = baseCurrency,
        exchangeRateApiKey = apiKey,
        exchangeRateApiBaseUrl = apiBaseUrl,
        themeOption = themeOption,
        isVoiceInputEnabled = isVoiceInputEnabled
    )
    
    /**
     * Create expenses for current month
     */
    fun createCurrentMonthExpenses(count: Int = 5): List<Expense> {
        return List(count) { index ->
            createExpense(
                id = "current-$index",
                date = LocalDateTime(2024, 11, index + 1, 12, 0),
                amount = (index + 1) * 20.0
            )
        }
    }
    
    /**
     * Create expenses for previous month
     */
    fun createPreviousMonthExpenses(count: Int = 3): List<Expense> {
        return List(count) { index ->
            createExpense(
                id = "prev-$index",
                date = LocalDateTime(2024, 10, index + 1, 12, 0),
                amount = (index + 1) * 15.0
            )
        }
    }
    
    /**
     * Create test AppSettings with customizable fields
     */
    fun createAppSettings(
        baseCurrency: Currency = Currency.USD,
        apiKey: String = "test-api-key-123",
        apiBaseUrl: String = "https://test-api.example.com",
        lastUpdate: kotlinx.datetime.LocalDateTime? = null,
        themeOption: ThemeOption = ThemeOption.SYSTEM,
        voiceInputEnabled: Boolean = false
    ): AppSettings {
        return AppSettings(
            baseCurrency = baseCurrency,
            exchangeRateApiKey = apiKey,
            exchangeRateApiBaseUrl = apiBaseUrl,
            lastExchangeRateUpdate = lastUpdate,
            themeOption = themeOption,
            isVoiceInputEnabled = voiceInputEnabled
        )
    }
    
    /**
     * Create test exchange rate map
     */
    fun createExchangeRates(baseCurrency: Currency = Currency.USD): Map<Currency, Double> {
        return when (baseCurrency) {
            Currency.USD -> mapOf(
                Currency.EUR to 0.85,
                Currency.GBP to 0.73,
                Currency.JPY to 110.0,
                Currency.CAD to 1.25,
                Currency.AUD to 1.35
            )
            Currency.EUR -> mapOf(
                Currency.USD to 1.18,
                Currency.GBP to 0.86,
                Currency.JPY to 129.0
            )
            else -> mapOf(Currency.USD to 1.0)
        }
    }
}

