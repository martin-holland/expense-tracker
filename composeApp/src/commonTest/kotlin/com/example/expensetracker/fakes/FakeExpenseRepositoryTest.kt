package com.example.expensetracker.fakes

import com.example.expensetracker.helpers.TestData
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.ExpenseCategory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlin.test.*

/**
 * Unit tests for FakeExpenseRepository
 * 
 * These tests validate that the fake implementation correctly implements
 * the IExpenseRepository interface contract, ensuring reliable test infrastructure.
 */
class FakeExpenseRepositoryTest {
    
    private lateinit var repository: FakeExpenseRepository
    
    @BeforeTest
    fun setup() {
        repository = FakeExpenseRepository()
    }
    
    @AfterTest
    fun tearDown() {
        repository.reset()
    }
    
    // ============================================================
    // INITIALIZATION TESTS
    // ============================================================
    
    @Test
    fun `initial state is empty`() = runTest {
        val expenses = repository.getAllExpenses().first()
        assertTrue(expenses.isEmpty())
    }
    
    @Test
    fun `initial expense count is zero`() = runTest {
        val count = repository.getExpenseCount()
        assertEquals(0, count)
    }
    
    // ============================================================
    // INSERT TESTS
    // ============================================================
    
    @Test
    fun `insertExpense adds expense`() = runTest {
        val expense = TestData.createExpense(amount = 100.0, category = ExpenseCategory.FOOD)
        
        repository.insertExpense(expense)
        
        val expenses = repository.getAllExpenses().first()
        assertEquals(1, expenses.size)
        assertEquals(expense, expenses.first())
    }
    
    @Test
    fun `insertExpense with same ID replaces expense`() = runTest {
        val expense1 = TestData.createExpense(id = "test-id", amount = 100.0)
        val expense2 = TestData.createExpense(id = "test-id", amount = 200.0)
        
        repository.insertExpense(expense1)
        repository.insertExpense(expense2)
        
        val expenses = repository.getAllExpenses().first()
        assertEquals(1, expenses.size)
        assertEquals(200.0, expenses.first().amount)
    }
    
    @Test
    fun `insertExpenses adds multiple expenses`() = runTest {
        val expenses = listOf(
            TestData.createExpense(amount = 100.0),
            TestData.createExpense(amount = 200.0),
            TestData.createExpense(amount = 300.0)
        )
        
        repository.insertExpenses(expenses)
        
        val result = repository.getAllExpenses().first()
        assertEquals(3, result.size)
    }
    
    // ============================================================
    // RETRIEVAL TESTS
    // ============================================================
    
    @Test
    fun `getExpenseById returns correct expense`() = runTest {
        val expense = TestData.createExpense(id = "test-id", amount = 100.0)
        repository.insertExpense(expense)
        
        val result = repository.getExpenseById("test-id")
        
        assertNotNull(result)
        assertEquals("test-id", result.id)
        assertEquals(100.0, result.amount)
    }
    
    @Test
    fun `getExpenseById returns null for non-existent ID`() = runTest {
        val result = repository.getExpenseById("non-existent")
        
        assertNull(result)
    }
    
    @Test
    fun `getAllExpenses returns all expenses`() = runTest {
        val expenses = listOf(
            TestData.createExpense(amount = 100.0),
            TestData.createExpense(amount = 200.0),
            TestData.createExpense(amount = 300.0)
        )
        repository.insertExpenses(expenses)
        
        val result = repository.getAllExpenses().first()
        
        assertEquals(3, result.size)
    }
    
    @Test
    fun `getExpensesByCategory returns correct expenses`() = runTest {
        repository.insertExpense(TestData.createExpense(category = ExpenseCategory.FOOD))
        repository.insertExpense(TestData.createExpense(category = ExpenseCategory.TRAVEL))
        repository.insertExpense(TestData.createExpense(category = ExpenseCategory.FOOD))
        
        val foodExpenses = repository.getExpensesByCategory(ExpenseCategory.FOOD).first()
        
        assertEquals(2, foodExpenses.size)
        assertTrue(foodExpenses.all { it.category == ExpenseCategory.FOOD })
    }
    
    @Test
    fun `getExpensesByCategory returns empty list for unused category`() = runTest {
        repository.insertExpense(TestData.createExpense(category = ExpenseCategory.FOOD))
        
        val result = repository.getExpensesByCategory(ExpenseCategory.UTILITIES).first()
        
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun `getExpensesByDateRange returns expenses in range`() = runTest {
        val date1 = LocalDateTime(2024, 11, 1, 12, 0)
        val date2 = LocalDateTime(2024, 11, 15, 12, 0)
        val date3 = LocalDateTime(2024, 11, 30, 12, 0)
        
        repository.insertExpense(TestData.createExpense(date = date1, amount = 100.0))
        repository.insertExpense(TestData.createExpense(date = date2, amount = 200.0))
        repository.insertExpense(TestData.createExpense(date = date3, amount = 300.0))
        
        val result = repository.getExpensesByDateRange(date1, date2).first()
        
        assertEquals(2, result.size)
        assertTrue(result.any { it.amount == 100.0 })
        assertTrue(result.any { it.amount == 200.0 })
    }
    
    @Test
    fun `getExpensesByAmountRange returns expenses in range`() = runTest {
        repository.insertExpense(TestData.createExpense(amount = 50.0))
        repository.insertExpense(TestData.createExpense(amount = 150.0))
        repository.insertExpense(TestData.createExpense(amount = 250.0))
        
        val result = repository.getExpensesByAmountRange(100.0, 200.0).first()
        
        assertEquals(1, result.size)
        assertEquals(150.0, result.first().amount)
    }
    
    // ============================================================
    // UPDATE TESTS
    // ============================================================
    
    @Test
    fun `updateExpense modifies existing expense`() = runTest {
        val expense = TestData.createExpense(id = "test-id", amount = 100.0, description = "Original")
        repository.insertExpense(expense)
        
        val updated = expense.copy(amount = 200.0, description = "Updated")
        repository.updateExpense(updated)
        
        val result = repository.getExpenseById("test-id")
        assertNotNull(result)
        assertEquals(200.0, result.amount)
        assertEquals("Updated", result.description)
    }
    
    @Test
    fun `updateExpense on non-existent expense does nothing`() = runTest {
        val expense = TestData.createExpense(id = "non-existent")
        
        repository.updateExpense(expense) // Should not crash
        
        val result = repository.getExpenseById("non-existent")
        assertNull(result)
    }
    
    // ============================================================
    // DELETE TESTS
    // ============================================================
    
    @Test
    fun `deleteExpense removes expense`() = runTest {
        val expense = TestData.createExpense(id = "test-id")
        repository.insertExpense(expense)
        
        repository.deleteExpense(expense)
        
        val result = repository.getExpenseById("test-id")
        assertNull(result)
        assertEquals(0, repository.getExpenseCount())
    }
    
    @Test
    fun `deleteExpenseById removes expense`() = runTest {
        val expense = TestData.createExpense(id = "test-id")
        repository.insertExpense(expense)
        
        repository.deleteExpenseById("test-id")
        
        val result = repository.getExpenseById("test-id")
        assertNull(result)
    }
    
    @Test
    fun `delete on non-existent expense does nothing`() = runTest {
        val expense = TestData.createExpense(id = "non-existent")
        
        repository.deleteExpense(expense) // Should not crash
        
        assertEquals(0, repository.getExpenseCount())
    }
    
    // ============================================================
    // COUNT TESTS
    // ============================================================
    
    @Test
    fun `getExpenseCount returns correct count`() = runTest {
        assertEquals(0, repository.getExpenseCount())
        
        repository.insertExpense(TestData.createExpense())
        assertEquals(1, repository.getExpenseCount())
        
        repository.insertExpense(TestData.createExpense())
        assertEquals(2, repository.getExpenseCount())
    }
    
    // ============================================================
    // FLOW REACTIVITY TESTS
    // ============================================================
    
    @Test
    fun `flow emits updated data after insert`() = runTest {
        var emissionCount = 0
        val expense = TestData.createExpense(amount = 100.0)
        
        // Start collecting
        val job = launch {
            repository.getAllExpenses().collect {
                emissionCount++
            }
        }
        
        // Initial emission
        delay(50)
        assertEquals(1, emissionCount)
        
        // Insert should trigger new emission
        repository.addExpense(expense)
        delay(50)
        assertEquals(2, emissionCount)
        
        job.cancel()
    }
    
    // ============================================================
    // ERROR SIMULATION TESTS
    // ============================================================
    
    @Test
    fun `shouldThrowError causes getAllExpenses to throw`() = runTest {
        repository.shouldThrowError = true
        
        assertFailsWith<Exception> {
            repository.getAllExpenses().first()
        }
    }
    
    @Test
    fun `shouldThrowError causes insertExpense to throw`() = runTest {
        repository.shouldThrowError = true
        
        assertFailsWith<Exception> {
            repository.insertExpense(TestData.createExpense())
        }
    }
    
    @Test
    fun `custom error message is used`() = runTest {
        repository.shouldThrowError = true
        repository.errorMessage = "Custom test error"
        
        val exception = assertFailsWith<Exception> {
            repository.getAllExpenses().first()
        }
        assertEquals("Custom test error", exception.message)
    }
    
    // ============================================================
    // RESET TESTS
    // ============================================================
    
    @Test
    fun `reset clears all data`() = runTest {
        repository.insertExpense(TestData.createExpense())
        repository.insertExpense(TestData.createExpense())
        assertEquals(2, repository.getExpenseCount())
        
        repository.reset()
        
        assertEquals(0, repository.getExpenseCount())
        val expenses = repository.getAllExpenses().first()
        assertTrue(expenses.isEmpty())
    }
    
    @Test
    fun `reset clears error flags`() = runTest {
        repository.shouldThrowError = true
        repository.errorMessage = "Test error"
        repository.delayMs = 100L
        
        repository.reset()
        
        assertFalse(repository.shouldThrowError)
        assertEquals("Test error", repository.errorMessage) // errorMessage not reset
        assertEquals(0L, repository.delayMs)
    }
    
    @Test
    fun `reset clears call counters`() = runTest {
        repository.insertExpense(TestData.createExpense())
        assertTrue(repository.insertExpenseCalled)
        
        repository.reset()
        
        assertFalse(repository.insertExpenseCalled)
        assertFalse(repository.getAllExpensesCalled)
    }
}

