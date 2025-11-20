package com.example.expensetracker.viewmodel

import com.example.expensetracker.fakes.FakeExpenseRepository
import com.example.expensetracker.helpers.TestData
import com.example.expensetracker.model.ExpenseCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

/**
 * Unit tests for DashBoardViewModel
 * 
 * Tests the dashboard's ability to:
 * - Load and display expenses
 * - Aggregate data by month, week, and day
 * - Calculate category totals
 * - Calculate month-over-month changes
 * - Handle empty states
 * - Handle errors gracefully
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DashBoardViewModelTest {
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: DashBoardViewModel
    private lateinit var fakeRepository: FakeExpenseRepository
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeExpenseRepository()
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
        fakeRepository.reset()
    }
    
    // Helper to create ViewModel and advance scheduler
    private fun TestScope.createViewModel(): DashBoardViewModel {
        val vm = DashBoardViewModel(fakeRepository)
        testScheduler.advanceUntilIdle() // Let Flow collection complete
        return vm
    }
    
    // ============================================================
    // INITIALIZATION TESTS
    // ============================================================
    
    @Test
    fun `initial state is loading`() = runTest {
        // Check state immediately after creation, before advancing scheduler
        viewModel = DashBoardViewModel(fakeRepository)
        
        // Initial state should be loading (before coroutine runs)
        assertTrue(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
    }
    
    @Test
    fun `loading completes after expenses are loaded`() = runTest {
        fakeRepository.setExpenses(listOf(TestData.createExpense()))
        viewModel = createViewModel()
        
        assertFalse(viewModel.uiState.value.isLoading)
    }
    
    @Test
    fun `empty repository shows empty state`() = runTest {
        fakeRepository.setExpenses(emptyList())
        viewModel = createViewModel()
        
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.expenses.isEmpty())
    }
    
    // ============================================================
    // EXPENSE LOADING TESTS
    // ============================================================
    
    @Test
    fun `loads expenses from repository`() = runTest {
        val testExpenses = listOf(
            TestData.createExpense(id = "1", amount = 50.0),
            TestData.createExpense(id = "2", amount = 100.0)
        )
        fakeRepository.setExpenses(testExpenses)
        viewModel = createViewModel()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.expenses.size)
    }
    
    @Test
    fun `updates when expenses are added`() = runTest {
        fakeRepository.setExpenses(emptyList())
        viewModel = createViewModel()
        
        assertEquals(0, viewModel.uiState.value.expenses.size)
        
        // Add expense
        fakeRepository.addExpense(TestData.createExpense(id = "1"))
        testScheduler.advanceUntilIdle() // Let Flow emit and collect
        
        assertEquals(1, viewModel.uiState.value.expenses.size)
    }
    
    @Test
    fun `updates when expenses are removed`() = runTest {
        val expense = TestData.createExpense(id = "1")
        fakeRepository.setExpenses(listOf(expense))
        viewModel = createViewModel()
        
        assertEquals(1, viewModel.uiState.value.expenses.size)
        
        // Remove expense
        fakeRepository.clearExpenses()
        testScheduler.advanceUntilIdle() // Let Flow emit and collect
        
        assertEquals(0, viewModel.uiState.value.expenses.size)
    }
    
    // ============================================================
    // AGGREGATION TESTS
    // ============================================================
    
    @Test
    fun `aggregates expenses by category`() = runTest {
        val expenses = listOf(
            TestData.createExpense(id = "1", category = ExpenseCategory.FOOD, amount = 50.0),
            TestData.createExpense(id = "2", category = ExpenseCategory.FOOD, amount = 30.0),
            TestData.createExpense(id = "3", category = ExpenseCategory.TRAVEL, amount = 20.0)
        )
        fakeRepository.setExpenses(expenses)
        viewModel = createViewModel()
        
        val categoryTotals = viewModel.uiState.value.categoryTotals
        assertTrue(categoryTotals.isNotEmpty())
        
        // Find FOOD category total (category is String in CategoryTotal)
        val foodTotal = categoryTotals.find { it.category == ExpenseCategory.FOOD.name }
        assertNotNull(foodTotal, "FOOD category should exist in totals")
        assertEquals(80.0, foodTotal.total, 0.01)
    }
    
    @Test
    fun `calculates monthly aggregate`() = runTest {
        val currentMonthExpenses = TestData.createCurrentMonthExpenses(5)
        fakeRepository.setExpenses(currentMonthExpenses)
        viewModel = createViewModel()
        
        val monthlyAgg = viewModel.uiState.value.monthlyAggregate
        assertNotNull(monthlyAgg)
        assertTrue(monthlyAgg.totalExpenses > 0)
        assertEquals(5, monthlyAgg.transactionCount)
    }
    
    @Test
    fun `calculates weekly aggregates`() = runTest {
        val expenses = TestData.createCurrentMonthExpenses(10)
        fakeRepository.setExpenses(expenses)
        viewModel = createViewModel()
        
        val weeklyAggs = viewModel.uiState.value.weeklyAggregates
        assertTrue(weeklyAggs.isNotEmpty(), "Should have weekly aggregates")
    }
    
    @Test
    fun `calculates daily aggregates`() = runTest {
        val expenses = TestData.createCurrentMonthExpenses(10)
        fakeRepository.setExpenses(expenses)
        viewModel = createViewModel()
        
        val dailyAggs = viewModel.uiState.value.dailyAggregates
        assertTrue(dailyAggs.isNotEmpty(), "Should have daily aggregates")
    }
    
    // ============================================================
    // MONTH-OVER-MONTH TESTS
    // ============================================================
    
    @Test
    fun `calculates month over month change`() = runTest {
        val currentMonth = TestData.createCurrentMonthExpenses(5)
        val previousMonth = TestData.createPreviousMonthExpenses(3)
        
        fakeRepository.setExpenses(currentMonth + previousMonth)
        viewModel = createViewModel()
        
        val momChange = viewModel.uiState.value.monthOverMonthChange
        assertNotNull(momChange, "Month-over-month change should be calculated")
        // Note: Actual change value depends on test data amounts
    }
    
    @Test
    fun `handles no previous month data correctly`() = runTest {
        val currentMonth = TestData.createCurrentMonthExpenses(5)
        fakeRepository.setExpenses(currentMonth)
        viewModel = createViewModel()
        
        val monthlyAgg = viewModel.uiState.value.monthlyAggregate
        assertNotNull(monthlyAgg, "Should have current month aggregate")
        
        // Previous month should exist but be empty
        val prevAgg = viewModel.uiState.value.previousMonthAggregate
        assertNotNull(prevAgg, "Should have previous month aggregate")
    }
    
    // ============================================================
    // ERROR HANDLING TESTS
    // ============================================================
    
    @Test
    fun `handles repository errors gracefully`() = runTest {
        fakeRepository.shouldThrowError = true
        fakeRepository.errorMessage = "Database error"
        viewModel = createViewModel()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("error"), "Error message should contain 'error'")
    }
    
    @Test
    fun `recovers from errors when data becomes available`() = runTest {
        // Start with error
        fakeRepository.shouldThrowError = true
        fakeRepository.errorMessage = "Temporary error"
        viewModel = createViewModel()
        
        assertNotNull(viewModel.uiState.value.error, "Should have error initially")
        
        // Fix the error and provide data
        fakeRepository.shouldThrowError = false
        fakeRepository.setExpenses(listOf(TestData.createExpense()))
        testScheduler.advanceUntilIdle() // Let Flow emit and collect
        
        // Should recover
        assertNull(viewModel.uiState.value.error, "Error should be cleared")
        assertFalse(viewModel.uiState.value.isLoading, "Should not be loading")
    }
    
    // ============================================================
    // EDGE CASE TESTS
    // ============================================================
    
    @Test
    fun `handles large number of expenses`() = runTest {
        val largeExpenseList = List(100) { index ->
            TestData.createExpense(
                id = "expense-$index",
                amount = (index + 1) * 10.0
            )
        }
        fakeRepository.setExpenses(largeExpenseList)
        viewModel = createViewModel()
        
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(100, state.expenses.size)
        assertNotNull(state.monthlyAggregate)
    }
    
    @Test
    fun `handles expenses with zero amount`() = runTest {
        val expenses = listOf(
            TestData.createExpense(id = "1", amount = 0.0),
            TestData.createExpense(id = "2", amount = 50.0)
        )
        fakeRepository.setExpenses(expenses)
        viewModel = createViewModel()
        
        val state = viewModel.uiState.value
        assertEquals(2, state.expenses.size)
        assertNotNull(state.monthlyAggregate)
    }
    
    @Test
    fun `handles multiple categories correctly`() = runTest {
        val expenses = ExpenseCategory.entries.mapIndexed { index, category ->
            TestData.createExpense(
                id = "expense-$index",
                category = category,
                amount = (index + 1) * 10.0
            )
        }
        fakeRepository.setExpenses(expenses)
        viewModel = createViewModel()
        
        val categoryTotals = viewModel.uiState.value.categoryTotals
        assertTrue(categoryTotals.size <= ExpenseCategory.entries.size)
        
        // Each category should have correct total
        categoryTotals.forEach { total ->
            assertTrue(total.total > 0, "Category ${total.category} should have positive total")
        }
    }
    
    @Test
    fun `handles single expense correctly`() = runTest {
        val singleExpense = TestData.createExpense(id = "1", amount = 100.0)
        fakeRepository.setExpenses(listOf(singleExpense))
        viewModel = createViewModel()
        
        val state = viewModel.uiState.value
        assertEquals(1, state.expenses.size)
        assertNotNull(state.monthlyAggregate)
        assertEquals(1, state.monthlyAggregate?.transactionCount)
    }
    
    // ============================================================
    // CURRENT MONTH TESTS
    // ============================================================
    
    @Test
    fun `current month is set correctly`() = runTest {
        fakeRepository.setExpenses(listOf(TestData.createExpense()))
        viewModel = createViewModel()
        
        val currentMonth = viewModel.uiState.value.currentMonth
        assertNotNull(currentMonth)
        // Year should be reasonable
        assertTrue(currentMonth.year >= 2024, "Year should be 2024 or later")
    }
    
    @Test
    fun `empty state has no aggregates`() = runTest {
        fakeRepository.setExpenses(emptyList())
        viewModel = createViewModel()
        
        val state = viewModel.uiState.value
        assertTrue(state.expenses.isEmpty())
        assertTrue(state.categoryTotals.isEmpty())
        assertTrue(state.dailyAggregates.isEmpty())
        assertTrue(state.weeklyAggregates.isEmpty())
    }
}
