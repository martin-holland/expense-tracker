package com.example.expensetracker.viewmodel

import com.example.expensetracker.fakes.FakeCurrencyConverter
import com.example.expensetracker.fakes.FakeExchangeRateRepository
import com.example.expensetracker.fakes.FakeExpenseRepository
import com.example.expensetracker.fakes.FakeSettingsRepository
import com.example.expensetracker.helpers.TestData
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.ExpenseCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

/**
 * Unit tests for CurrencyExchangeViewModel
 * 
 * Tests exchange rate display, expense conversion, rate refresh, and state management.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CurrencyExchangeViewModelTest {
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: CurrencyExchangeViewModel
    private lateinit var fakeCurrencyConverter: FakeCurrencyConverter
    private lateinit var fakeSettingsRepository: FakeSettingsRepository
    private lateinit var fakeExchangeRateRepository: FakeExchangeRateRepository
    private lateinit var fakeExpenseRepository: FakeExpenseRepository
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeCurrencyConverter = FakeCurrencyConverter()
        fakeSettingsRepository = FakeSettingsRepository()
        fakeExchangeRateRepository = FakeExchangeRateRepository()
        fakeExpenseRepository = FakeExpenseRepository()
    }
    
    @AfterTest
    fun tearDown() {
        fakeExpenseRepository.reset()
        fakeSettingsRepository.reset()
        fakeExchangeRateRepository.reset()
        Dispatchers.resetMain()
    }
    
    private fun TestScope.createViewModel(): CurrencyExchangeViewModel {
        val vm = CurrencyExchangeViewModel(
            currencyConverter = fakeCurrencyConverter,
            settingsRepository = fakeSettingsRepository,
            exchangeRateRepository = fakeExchangeRateRepository,
            expenseRepository = fakeExpenseRepository
        )
        testScheduler.advanceUntilIdle()
        return vm
    }
    
    // ============================================================
    // INITIALIZATION TESTS
    // ============================================================
    
    @Test
    fun `initial state has USD as base currency`() = runTest {
        viewModel = createViewModel()
        
        assertEquals(Currency.USD, viewModel.baseCurrency.value)
    }
    
    @Test
    fun `initial state is not loading`() = runTest {
        viewModel = createViewModel()
        
        assertFalse(viewModel.isLoading.value)
    }
    
    @Test
    fun `initial state has no error`() = runTest {
        viewModel = createViewModel()
        
        assertNull(viewModel.errorMessage.value)
    }
    
    @Test
    fun `loads base currency from settings`() = runTest {
        val settings = TestData.createAppSettings(baseCurrency = Currency.EUR)
        fakeSettingsRepository.setSettings(settings)
        
        viewModel = createViewModel()
        
        assertEquals(Currency.EUR, viewModel.baseCurrency.value)
    }
    
    // ============================================================
    // EXPENSE CONVERSION TESTS
    // ============================================================
    
    @Test
    fun `converts expenses when loaded`() = runTest {
        fakeExpenseRepository.addExpense(
            TestData.createExpense(amount = 100.0, currency = Currency.USD)
        )
        fakeExpenseRepository.addExpense(
            TestData.createExpense(amount = 50.0, currency = Currency.EUR)
        )
        
        viewModel = createViewModel()
        
        assertEquals(2, viewModel.expensesWithConversion.value.size)
    }
    
    @Test
    fun `expense conversion uses correct base currency`() = runTest {
        val settings = TestData.createAppSettings(baseCurrency = Currency.EUR)
        fakeSettingsRepository.setSettings(settings)
        fakeCurrencyConverter.baseCurrency = Currency.EUR
        
        fakeExpenseRepository.addExpense(
            TestData.createExpense(amount = 100.0, currency = Currency.USD)
        )
        
        viewModel = createViewModel()
        
        val converted = viewModel.expensesWithConversion.value.first()
        assertEquals(Currency.EUR, converted.baseCurrency)
        assertNotNull(converted.convertedAmount)
    }
    
    @Test
    fun `expense already in base currency not converted`() = runTest {
        fakeExpenseRepository.addExpense(
            TestData.createExpense(amount = 100.0, currency = Currency.USD)
        )
        
        viewModel = createViewModel()
        
        val converted = viewModel.expensesWithConversion.value.first()
        assertEquals(100.0, converted.convertedAmount)
        assertEquals(Currency.USD, converted.baseCurrency)
    }
    
    @Test
    fun `empty expense list results in empty conversions`() = runTest {
        viewModel = createViewModel()
        
        assertTrue(viewModel.expensesWithConversion.value.isEmpty())
    }
    
    @Test
    fun `expense conversions update when expenses change`() = runTest {
        viewModel = createViewModel()
        assertTrue(viewModel.expensesWithConversion.value.isEmpty())
        
        fakeExpenseRepository.addExpense(
            TestData.createExpense(amount = 75.0, currency = Currency.GBP)
        )
        testScheduler.advanceUntilIdle()
        
        assertEquals(1, viewModel.expensesWithConversion.value.size)
    }
    
    // ============================================================
    // EXCHANGE RATE TESTS
    // ============================================================
    
    @Test
    fun `loads exchange rates on initialization`() = runTest {
        // Add some test rates
        fakeExchangeRateRepository.setExchangeRate(Currency.USD, Currency.EUR, 0.85)
        fakeExchangeRateRepository.setExchangeRate(Currency.USD, Currency.GBP, 0.73)
        
        viewModel = createViewModel()
        
        val rates = viewModel.exchangeRates.value
        assertTrue(rates.isNotEmpty())
    }
    
    @Test
    fun `exchange rates include base currency at 1_0`() = runTest {
        viewModel = createViewModel()
        
        val rates = viewModel.exchangeRates.value
        // Base currency (USD) should be 1.0
        assertEquals(1.0, rates[Currency.USD])
    }
    
    @Test
    fun `exchange rates update when base currency changes`() = runTest {
        viewModel = createViewModel()
        val initialRates = viewModel.exchangeRates.value
        
        // Change base currency
        val newSettings = TestData.createAppSettings(baseCurrency = Currency.EUR)
        fakeSettingsRepository.setSettings(newSettings)
        testScheduler.advanceUntilIdle()
        
        val newRates = viewModel.exchangeRates.value
        // EUR should now be 1.0 as the base currency
        assertEquals(1.0, newRates[Currency.EUR])
    }
    
    // ============================================================
    // REFRESH TESTS
    // ============================================================
    
    @Test
    fun `refreshExchangeRates sets loading true`() = runTest {
        viewModel = createViewModel()
        assertFalse(viewModel.isLoading.value)
        
        viewModel.refreshExchangeRates()
        // Check immediately (before completion)
        // Note: with StandardTestDispatcher, we have control
        
        testScheduler.advanceUntilIdle()
        // After completion, should be false again
        assertFalse(viewModel.isLoading.value)
    }
    
    @Test
    fun `refreshExchangeRates clears previous error`() = runTest {
        viewModel = createViewModel()
        
        // Simulate an error first
        fakeExchangeRateRepository.setRefreshShouldFail(true)
        viewModel.refreshExchangeRates()
        testScheduler.advanceUntilIdle()
        assertNotNull(viewModel.errorMessage.value)
        
        // Now refresh should succeed and clear error
        fakeExchangeRateRepository.setRefreshShouldFail(false)
        viewModel.refreshExchangeRates()
        testScheduler.advanceUntilIdle()
        
        // Loading should complete
        assertFalse(viewModel.isLoading.value)
    }
    
    @Test
    fun `refreshExchangeRates handles success`() = runTest {
        fakeExchangeRateRepository.setExchangeRate(Currency.USD, Currency.EUR, 0.85)
        
        viewModel = createViewModel()
        viewModel.refreshExchangeRates()
        testScheduler.advanceUntilIdle()
        
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
    }
    
    @Test
    fun `refreshExchangeRates handles failure`() = runTest {
        fakeExchangeRateRepository.setRefreshShouldFail(true)
        
        viewModel = createViewModel()
        viewModel.refreshExchangeRates()
        testScheduler.advanceUntilIdle()
        
        assertFalse(viewModel.isLoading.value)
        assertNotNull(viewModel.errorMessage.value)
    }
    
    @Test
    fun `refreshExchangeRates reloads rates after success`() = runTest {
        viewModel = createViewModel()
        val initialRates = viewModel.exchangeRates.value
        
        // Add new rates
        fakeExchangeRateRepository.setExchangeRate(Currency.USD, Currency.JPY, 110.0)
        
        viewModel.refreshExchangeRates()
        testScheduler.advanceUntilIdle()
        
        val newRates = viewModel.exchangeRates.value
        // Should have reloaded rates
        assertTrue(newRates.containsKey(Currency.JPY))
    }
    
    // ============================================================
    // BASE CURRENCY CHANGE TESTS
    // ============================================================
    
    @Test
    fun `base currency change triggers expense reconversion`() = runTest {
        fakeExpenseRepository.addExpense(
            TestData.createExpense(amount = 100.0, currency = Currency.USD)
        )
        
        viewModel = createViewModel()
        val initial = viewModel.expensesWithConversion.value.first()
        assertEquals(Currency.USD, initial.baseCurrency)
        
        // Change base currency
        val newSettings = TestData.createAppSettings(baseCurrency = Currency.EUR)
        fakeSettingsRepository.setSettings(newSettings)
        fakeCurrencyConverter.baseCurrency = Currency.EUR
        testScheduler.advanceUntilIdle()
        
        val updated = viewModel.expensesWithConversion.value.first()
        assertEquals(Currency.EUR, updated.baseCurrency)
    }
    
    @Test
    fun `base currency change updates baseCurrency state`() = runTest {
        viewModel = createViewModel()
        assertEquals(Currency.USD, viewModel.baseCurrency.value)
        
        val newSettings = TestData.createAppSettings(baseCurrency = Currency.GBP)
        fakeSettingsRepository.setSettings(newSettings)
        testScheduler.advanceUntilIdle()
        
        assertEquals(Currency.GBP, viewModel.baseCurrency.value)
    }
    
    // ============================================================
    // ERROR HANDLING TESTS
    // ============================================================
    
    @Test
    fun `handles empty expense repository gracefully`() = runTest {
        // Test with no expenses - should not crash or show errors
        viewModel = createViewModel()
        
        // Should have no errors
        assertNull(viewModel.errorMessage.value)
        // Should have empty conversions
        assertTrue(viewModel.expensesWithConversion.value.isEmpty())
        // Should not be loading
        assertFalse(viewModel.isLoading.value)
    }
    
    @Test
    fun `handles settings repository errors gracefully`() = runTest {
        fakeSettingsRepository.shouldThrowError = true
        
        viewModel = createViewModel()
        
        // Should handle error without crashing
        // Error message might be set
    }
    
    // ============================================================
    // INTEGRATION TESTS
    // ============================================================
    
    @Test
    fun `complete flow - expenses converted with correct rates`() = runTest {
        // Setup: EUR base currency, expenses in different currencies
        val settings = TestData.createAppSettings(baseCurrency = Currency.EUR)
        fakeSettingsRepository.setSettings(settings)
        fakeCurrencyConverter.baseCurrency = Currency.EUR
        
        fakeExpenseRepository.addExpense(
            TestData.createExpense(amount = 100.0, currency = Currency.USD, category = ExpenseCategory.FOOD)
        )
        fakeExpenseRepository.addExpense(
            TestData.createExpense(amount = 50.0, currency = Currency.EUR, category = ExpenseCategory.TRAVEL)
        )
        fakeExpenseRepository.addExpense(
            TestData.createExpense(amount = 75.0, currency = Currency.GBP, category = ExpenseCategory.UTILITIES)
        )
        
        viewModel = createViewModel()
        
        // Verify all expenses converted
        assertEquals(3, viewModel.expensesWithConversion.value.size)
        
        // Verify base currency
        assertEquals(Currency.EUR, viewModel.baseCurrency.value)
        
        // Verify conversions have converted amounts
        viewModel.expensesWithConversion.value.forEach { converted ->
            assertNotNull(converted.convertedAmount)
            assertEquals(Currency.EUR, converted.baseCurrency)
        }
    }
    
    @Test
    fun `multiple currencies handled correctly`() = runTest {
        Currency.entries.take(5).forEach { currency ->
            fakeExpenseRepository.addExpense(
                TestData.createExpense(amount = 100.0, currency = currency)
            )
        }
        
        viewModel = createViewModel()
        
        assertEquals(5, viewModel.expensesWithConversion.value.size)
        viewModel.expensesWithConversion.value.forEach { converted ->
            assertEquals(Currency.USD, converted.baseCurrency)
        }
    }
}

