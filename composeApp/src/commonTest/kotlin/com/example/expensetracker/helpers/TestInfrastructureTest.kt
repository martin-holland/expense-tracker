package com.example.expensetracker.helpers

import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.ExpenseCategory
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Test to verify testing infrastructure is working correctly
 */
class TestInfrastructureTest {
    
    @Test
    fun `kotlin test framework works`() {
        assertTrue(true, "Basic assertion works")
        assertEquals(1, 1)
        assertNotNull("test")
    }
    
    @Test
    fun `coroutines test works`() = runTest {
        // Test that runTest works
        val result = suspendFunction()
        assertEquals("success", result)
    }
    
    private suspend fun suspendFunction(): String {
        return "success"
    }
    
    @Test
    fun `TestData helper creates expense`() {
        val expense = TestData.createExpense()
        
        assertNotNull(expense)
        assertEquals(50.0, expense.amount)
        assertEquals(Currency.USD, expense.currency)
        assertEquals(ExpenseCategory.FOOD, expense.category)
    }
    
    @Test
    fun `TestData helper creates multiple expenses`() {
        val expenses = TestData.createExpenses(5)
        
        assertEquals(5, expenses.size)
        assertEquals(10.0, expenses[0].amount)
        assertEquals(50.0, expenses[4].amount)
    }
    
    @Test
    fun `TestData helper creates settings`() {
        val settings = TestData.createSettings(
            baseCurrency = Currency.EUR,
            apiKey = "test-key"
        )
        
        assertNotNull(settings)
        assertEquals(Currency.EUR, settings.baseCurrency)
        assertEquals("test-key", settings.exchangeRateApiKey)
    }
    
    @Test
    fun `model classes work`() {
        val expense = TestData.createExpense(
            amount = 100.0,
            category = ExpenseCategory.TRAVEL,
            currency = Currency.GBP
        )
        
        // Verify model properties
        assertEquals(100.0, expense.amount)
        assertEquals(ExpenseCategory.TRAVEL, expense.category)
        assertEquals(Currency.GBP, expense.currency)
        assertEquals("Test expense", expense.description)
    }
}

