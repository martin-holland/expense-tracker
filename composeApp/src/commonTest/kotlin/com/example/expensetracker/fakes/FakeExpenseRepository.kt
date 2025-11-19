package com.example.expensetracker.fakes

import com.example.expensetracker.data.repository.IExpenseRepository
import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.ExpenseCategory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDateTime

/**
 * Fake implementation of IExpenseRepository for testing
 * 
 * This fake maintains an in-memory list of expenses and provides
 * full control over behavior for comprehensive testing.
 */
class FakeExpenseRepository : IExpenseRepository {
    
    private val expenses = mutableListOf<Expense>()
    private val _expensesFlow = MutableStateFlow<List<Expense>>(emptyList())
    
    // Test controls
    var shouldThrowError = false
    var errorMessage = "Test error"
    var delayMs = 0L
    
    // Test observability
    var getAllExpensesCalled = false
    var insertExpenseCalled = false
    var deleteExpenseCalled = false
    var updateExpenseCalled = false
    val savedExpenses: List<Expense> get() = expenses.toList()
    
    /**
     * Get all expenses as a Flow
     */
    override fun getAllExpenses(): Flow<List<Expense>> {
        getAllExpensesCalled = true
        return if (shouldThrowError) {
            flow { throw Exception(errorMessage) }
        } else {
            _expensesFlow
        }
    }
    
    /**
     * Insert a new expense
     */
    override suspend fun insertExpense(expense: Expense) {
        insertExpenseCalled = true
        if (delayMs > 0) delay(delayMs)
        if (shouldThrowError) throw Exception(errorMessage)
        
        // Remove existing expense with same ID if present (upsert behavior)
        expenses.removeAll { it.id == expense.id }
        expenses.add(expense)
        _expensesFlow.value = expenses.toList()
    }
    
    /**
     * Insert multiple expenses
     */
    override suspend fun insertExpenses(expenses: List<Expense>) {
        if (shouldThrowError) throw Exception(errorMessage)
        this.expenses.addAll(expenses)
        _expensesFlow.value = expenses.toList()
    }
    
    /**
     * Update an expense
     */
    override suspend fun updateExpense(expense: Expense) {
        updateExpenseCalled = true
        if (shouldThrowError) throw Exception(errorMessage)
        
        val index = expenses.indexOfFirst { it.id == expense.id }
        if (index >= 0) {
            expenses[index] = expense
            _expensesFlow.value = expenses.toList()
        }
    }
    
    /**
     * Delete an expense
     */
    override suspend fun deleteExpense(expense: Expense) {
        deleteExpenseCalled = true
        if (shouldThrowError) throw Exception(errorMessage)
        
        expenses.removeAll { it.id == expense.id }
        _expensesFlow.value = expenses.toList()
    }
    
    /**
     * Delete expense by ID
     */
    override suspend fun deleteExpenseById(id: String) {
        if (shouldThrowError) throw Exception(errorMessage)
        expenses.removeAll { it.id == id }
        _expensesFlow.value = expenses.toList()
    }
    
    /**
     * Get expense by ID
     */
    override suspend fun getExpenseById(id: String): Expense? {
        if (shouldThrowError) throw Exception(errorMessage)
        return expenses.find { it.id == id }
    }
    
    /**
     * Get expenses by category
     */
    override fun getExpensesByCategory(category: ExpenseCategory): Flow<List<Expense>> {
        return flow {
            if (shouldThrowError) throw Exception(errorMessage)
            emit(expenses.filter { it.category == category })
        }
    }
    
    /**
     * Get expenses by date range
     */
    override fun getExpensesByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<Expense>> {
        return flow {
            if (shouldThrowError) throw Exception(errorMessage)
            emit(expenses.filter { it.date >= startDate && it.date <= endDate })
        }
    }
    
    /**
     * Get expenses by amount range
     */
    override fun getExpensesByAmountRange(
        minAmount: Double,
        maxAmount: Double
    ): Flow<List<Expense>> {
        return flow {
            if (shouldThrowError) throw Exception(errorMessage)
            emit(expenses.filter { it.amount >= minAmount && it.amount <= maxAmount })
        }
    }
    
    /**
     * Get expense count
     */
    override suspend fun getExpenseCount(): Int {
        if (shouldThrowError) throw Exception(errorMessage)
        return expenses.size
    }
    
    // ============================================
    // Test Helper Methods
    // ============================================
    
    /**
     * Set expenses (replaces all existing)
     */
    fun setExpenses(newExpenses: List<Expense>) {
        expenses.clear()
        expenses.addAll(newExpenses)
        _expensesFlow.value = expenses.toList()
    }
    
    /**
     * Add single expense
     */
    fun addExpense(expense: Expense) {
        expenses.add(expense)
        _expensesFlow.value = expenses.toList()
    }
    
    /**
     * Clear all expenses
     */
    fun clearExpenses() {
        expenses.clear()
        _expensesFlow.value = emptyList()
    }
    
    /**
     * Reset all state and flags
     */
    fun reset() {
        expenses.clear()
        _expensesFlow.value = emptyList()
        shouldThrowError = false
        errorMessage = "Test error"
        delayMs = 0L
        getAllExpensesCalled = false
        insertExpenseCalled = false
        deleteExpenseCalled = false
        updateExpenseCalled = false
    }
}

