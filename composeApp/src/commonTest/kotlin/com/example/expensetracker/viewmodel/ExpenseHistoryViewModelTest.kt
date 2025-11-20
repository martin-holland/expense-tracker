package com.example.expensetracker.viewmodel

import com.example.expensetracker.fakes.FakeCurrencyConverter
import com.example.expensetracker.fakes.FakeExpenseRepository
import com.example.expensetracker.fakes.FakeSettingsRepository
import com.example.expensetracker.helpers.TestData
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.ExpenseCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.datetime.LocalDateTime
import kotlin.test.*

/**
 * Unit tests for ExpenseHistoryViewModel
 * 
 * Now fully testable with ICurrencyConverter dependency injection!
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseHistoryViewModelTest {
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ExpenseHistoryViewModel
    private lateinit var fakeExpenseRepository: FakeExpenseRepository
    private lateinit var fakeSettingsRepository: FakeSettingsRepository
    private lateinit var fakeCurrencyConverter: FakeCurrencyConverter
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeExpenseRepository = FakeExpenseRepository()
        fakeSettingsRepository = FakeSettingsRepository()
        fakeCurrencyConverter = FakeCurrencyConverter()
    }
    
    @AfterTest
    fun tearDown() {
        fakeExpenseRepository.reset()
        fakeSettingsRepository.reset()
        Dispatchers.resetMain()
    }
    
    private fun TestScope.createViewModel(): ExpenseHistoryViewModel {
        val vm = ExpenseHistoryViewModel(
            repository = fakeExpenseRepository,
            currencyConverter = fakeCurrencyConverter,
            settingsRepository = fakeSettingsRepository
        )
        testScheduler.advanceUntilIdle()
        return vm
    }
    
    // ============================================================
    // INITIALIZATION TESTS
    // ============================================================
    
    @Test
    fun `initial state is not loading after init`() = runTest {
        viewModel = createViewModel()
        
        assertFalse(viewModel.uiState.isLoading)
        assertTrue(viewModel.uiState.expenses.isEmpty())
    }
    
    @Test
    fun `loads expenses from repository`() = runTest {
        fakeExpenseRepository.addExpense(TestData.createExpense(amount = 50.0, category = ExpenseCategory.FOOD))
        fakeExpenseRepository.addExpense(TestData.createExpense(amount = 100.0, category = ExpenseCategory.TRAVEL))
        
        viewModel = createViewModel()
        
        assertEquals(2, viewModel.uiState.expenses.size)
        assertFalse(viewModel.uiState.isLoading)
        assertNull(viewModel.uiState.error)
    }
    
    @Test
    fun `empty repository shows empty state`() = runTest {
        viewModel = createViewModel()
        
        assertTrue(viewModel.uiState.expenses.isEmpty())
        assertFalse(viewModel.uiState.isLoading)
    }
    
    @Test
    fun `showConvertedAmounts defaults to true`() = runTest {
        viewModel = createViewModel()
        
        assertTrue(viewModel.showConvertedAmounts.value)
    }
    
    @Test
    fun `toggleShowConvertedAmounts changes state`() = runTest {
        viewModel = createViewModel()
        val initial = viewModel.showConvertedAmounts.value
        
        viewModel.toggleShowConvertedAmounts()
        
        assertEquals(!initial, viewModel.showConvertedAmounts.value)
    }
    
    // ============================================================
    // FILTERING TESTS  
    // ============================================================
    
    @Test
    fun `applyFilters updates filter state`() = runTest {
        viewModel = createViewModel()
        
        val categories = setOf(ExpenseCategory.FOOD, ExpenseCategory.TRAVEL)
        viewModel.applyFilters(categories = categories)
        
        assertEquals(categories, viewModel.uiState.selectedCategories)
    }
    
    @Test
    fun `clearFilters resets all filters`() = runTest {
        viewModel = createViewModel()
        
        viewModel.applyFilters(
            categories = setOf(ExpenseCategory.FOOD),
            amountRange = Pair(10.0, 100.0)
        )
        
        viewModel.clearFilters()
        
        assertNull(viewModel.uiState.selectedCategories)
        assertNull(viewModel.uiState.dateRange)
        assertNull(viewModel.uiState.amountRange)
    }
    
    @Test
    fun `getFilteredExpenses returns all when no filters`() = runTest {
        fakeExpenseRepository.addExpense(TestData.createExpense(amount = 50.0, category = ExpenseCategory.FOOD))
        fakeExpenseRepository.addExpense(TestData.createExpense(amount = 100.0, category = ExpenseCategory.TRAVEL))
        fakeExpenseRepository.addExpense(TestData.createExpense(amount = 25.0, category = ExpenseCategory.UTILITIES))
        
        viewModel = createViewModel()
        
        val filtered = viewModel.getFilteredExpenses()
        assertEquals(3, filtered.size)
    }
    
    @Test
    fun `getFilteredExpenses filters by category`() = runTest {
        fakeExpenseRepository.addExpense(TestData.createExpense(amount = 50.0, category = ExpenseCategory.FOOD))
        fakeExpenseRepository.addExpense(TestData.createExpense(amount = 100.0, category = ExpenseCategory.TRAVEL))
        fakeExpenseRepository.addExpense(TestData.createExpense(amount = 25.0, category = ExpenseCategory.FOOD))
        
        viewModel = createViewModel()
        viewModel.applyFilters(categories = setOf(ExpenseCategory.FOOD))
        
        val filtered = viewModel.getFilteredExpenses()
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.category == ExpenseCategory.FOOD })
    }
    
    @Test
    fun `getFilteredExpenses filters by amount range`() = runTest {
        fakeExpenseRepository.addExpense(TestData.createExpense(amount = 10.0))
        fakeExpenseRepository.addExpense(TestData.createExpense(amount = 50.0))
        fakeExpenseRepository.addExpense(TestData.createExpense(amount = 100.0))
        fakeExpenseRepository.addExpense(TestData.createExpense(amount = 150.0))
        
        viewModel = createViewModel()
        viewModel.applyFilters(amountRange = Pair(25.0, 125.0))
        
        val filtered = viewModel.getFilteredExpenses()
        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.amount in 25.0..125.0 })
    }
    
    // ============================================================
    // DELETE TESTS
    // ============================================================
    
    @Test
    fun `requestDeleteExpense shows delete dialog`() = runTest {
        val expense = TestData.createExpense()
        fakeExpenseRepository.addExpense(expense)
        
        viewModel = createViewModel()
        viewModel.requestDeleteExpense(expense)
        
        assertTrue(viewModel.uiState.showDeleteDialog)
        assertEquals(expense, viewModel.uiState.expenseToDelete)
    }
    
    @Test
    fun `confirmDeleteExpense deletes expense`() = runTest {
        val expense = TestData.createExpense()
        fakeExpenseRepository.addExpense(expense)
        
        viewModel = createViewModel()
        assertEquals(1, viewModel.uiState.expenses.size)
        
        viewModel.requestDeleteExpense(expense)
        viewModel.confirmDeleteExpense()
        testScheduler.advanceUntilIdle()
        
        assertTrue(viewModel.uiState.expenses.isEmpty())
        assertFalse(viewModel.uiState.showDeleteDialog)
    }
    
    @Test
    fun `cancelDeleteExpense closes dialog`() = runTest {
        val expense = TestData.createExpense()
        viewModel = createViewModel()
        
        viewModel.requestDeleteExpense(expense)
        assertTrue(viewModel.uiState.showDeleteDialog)
        
        viewModel.cancelDeleteExpense()
        
        assertFalse(viewModel.uiState.showDeleteDialog)
        assertNull(viewModel.uiState.expenseToDelete)
    }
    
    // ============================================================
    // EDIT TESTS
    // ============================================================
    
    @Test
    fun `openEditDialog shows edit dialog`() = runTest {
        val expense = TestData.createExpense()
        viewModel = createViewModel()
        
        viewModel.openEditDialog(expense)
        
        assertTrue(viewModel.uiState.showEditDialog)
        assertEquals(expense, viewModel.uiState.expenseToEdit)
    }
    
    @Test
    fun `closeEditDialog hides dialog`() = runTest {
        val expense = TestData.createExpense()
        viewModel = createViewModel()
        
        viewModel.openEditDialog(expense)
        viewModel.closeEditDialog()
        
        assertFalse(viewModel.uiState.showEditDialog)
        assertNull(viewModel.uiState.expenseToEdit)
    }
    
    @Test
    fun `saveExpense inserts new expense`() = runTest {
        viewModel = createViewModel()
        assertTrue(viewModel.uiState.expenses.isEmpty())
        
        val newExpense = TestData.createExpense(amount = 123.45)
        viewModel.saveExpense(newExpense)
        testScheduler.advanceUntilIdle()
        
        assertEquals(1, viewModel.uiState.expenses.size)
        assertFalse(viewModel.uiState.showEditDialog)
    }
    
    // ============================================================
    // FILTER DIALOG TESTS
    // ============================================================
    
    @Test
    fun `showFilterDialog sets dialog visible`() = runTest {
        viewModel = createViewModel()
        
        viewModel.showFilterDialog()
        
        assertTrue(viewModel.uiState.showFilterDialog)
    }
    
    @Test
    fun `hideFilterDialog sets dialog hidden`() = runTest {
        viewModel = createViewModel()
        viewModel.showFilterDialog()
        
        viewModel.hideFilterDialog()
        
        assertFalse(viewModel.uiState.showFilterDialog)
    }
    
    // ============================================================
    // CONVERSION TESTS
    // ============================================================
    
    @Test
    fun `convertedExpenses updates when expenses load`() = runTest {
        fakeExpenseRepository.addExpense(TestData.createExpense(amount = 50.0, currency = Currency.USD))
        fakeExpenseRepository.addExpense(TestData.createExpense(amount = 100.0, currency = Currency.EUR))
        
        viewModel = createViewModel()
        
        assertEquals(2, viewModel.convertedExpenses.value.size)
    }
    
    @Test
    fun `getFilteredExpensesWithConversion matches filtered expenses`() = runTest {
        fakeExpenseRepository.addExpense(TestData.createExpense(amount = 50.0, category = ExpenseCategory.FOOD))
        fakeExpenseRepository.addExpense(TestData.createExpense(amount = 100.0, category = ExpenseCategory.TRAVEL))
        fakeExpenseRepository.addExpense(TestData.createExpense(amount = 25.0, category = ExpenseCategory.FOOD))
        
        viewModel = createViewModel()
        viewModel.applyFilters(categories = setOf(ExpenseCategory.FOOD))
        
        val filteredWithConversion = viewModel.getFilteredExpensesWithConversion()
        assertEquals(2, filteredWithConversion.size)
        assertTrue(filteredWithConversion.all { it.expense.category == ExpenseCategory.FOOD })
    }
}

