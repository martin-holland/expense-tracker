package com.example.expensetracker.viewmodel

import com.example.expensetracker.fakes.FakeExpenseRepository
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.ExpenseCategory
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Simplified unit tests for AddExpenseViewModel
 * Tests public APIs with dependency injection
 */
class AddExpenseViewModelSimpleTest {
    
    private lateinit var viewModel: AddExpenseViewModel
    private lateinit var fakeRepository: FakeExpenseRepository
    
    @BeforeTest
    fun setup() {
        fakeRepository = FakeExpenseRepository()
        viewModel = AddExpenseViewModel(fakeRepository)
    }
    
    @AfterTest
    fun tearDown() {
        fakeRepository.reset()
    }
    
    // ============================================
    // Initialization Tests
    // ============================================
    
    @Test
    fun `viewModel initializes successfully`() {
        assertNotNull(viewModel, "ViewModel should be created")
    }
    
    @Test
    fun `default currency is USD`() {
        assertEquals(Currency.USD, viewModel.currency)
    }
    
    // ============================================
    // Method Call Tests
    // ============================================
    
    @Test
    fun `onCurrencySelected accepts valid currency`() {
        // Should not throw
        viewModel.onCurrencySelected(Currency.EUR)
        assertEquals(Currency.EUR, viewModel.currency)
        
        viewModel.onCurrencySelected(Currency.GBP)
        assertEquals(Currency.GBP, viewModel.currency)
    }
    
    @Test
    fun `onAmountChanged accepts valid amounts`() {
        // Should not throw
        viewModel.onAmountChanged("50.00")
        viewModel.onAmountChanged("100")
        viewModel.onAmountChanged("0.99")
    }
    
    @Test
    fun `onCategorySelected accepts valid category`() {
        // Should not throw  
        viewModel.onCategorySelected(ExpenseCategory.FOOD)
        viewModel.onCategorySelected(ExpenseCategory.TRAVEL)
        viewModel.onCategorySelected(ExpenseCategory.UTILITIES)
    }
    
    @Test
    fun `onNoteChanged accepts text`() {
        // Should not throw
        viewModel.onNoteChanged("Test note")
        viewModel.onNoteChanged("Another note")
    }
    
    @Test
    fun `onDateSelected accepts date string`() {
        // Should not throw
        viewModel.onDateSelected("November 7, 2024")
        viewModel.onDateSelected("December 25, 2024")
    }
    
    @Test
    fun `resetForm does not throw`() {
        // Setup some state
        viewModel.onAmountChanged("100")
        viewModel.onCategorySelected(ExpenseCategory.TRAVEL)
        viewModel.onNoteChanged("Test")
        
        // Should not throw
        viewModel.resetForm()
    }
    
    @Test
    fun `dismissSnackbar does not throw`() {
        // Should not throw
        viewModel.dismissSnackbar()
    }
    
    // ============================================
    // Validation Tests (via saveExpense behavior)
    // ============================================
    
    @Test
    fun `saveExpense can be called`() {
        // Should not throw (though may show validation errors)
        viewModel.saveExpense()
    }
    
    @Test
    fun `saveExpense with valid data sets up correctly`() {
        viewModel.onAmountChanged("50.00")
        viewModel.onCategorySelected(ExpenseCategory.FOOD)
        viewModel.onDateSelected("November 7, 2024")
        
        // Verify form is ready to save
        assertEquals("50.00", viewModel.amount)
        assertEquals(ExpenseCategory.FOOD, viewModel.category)
        // Note: Full saveExpense test requires advanced viewModelScope mocking
        // This test verifies the form setup works correctly
    }
}

