package com.example.expensetracker.fakes

import com.example.expensetracker.model.Currency
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlin.test.*

/**
 * Unit tests for FakeExchangeRateRepository
 * 
 * Validates that the fake implementation correctly implements
 * the IExchangeRateRepository interface contract.
 */
class FakeExchangeRateRepositoryTest {
    
    private lateinit var repository: FakeExchangeRateRepository
    
    @BeforeTest
    fun setup() {
        repository = FakeExchangeRateRepository()
    }
    
    @AfterTest
    fun tearDown() {
        repository.reset()
    }
    
    // ============================================================
    // BASIC RATE OPERATIONS
    // ============================================================
    
    @Test
    fun `setExchangeRate stores rate`() = runTest {
        repository.setExchangeRate(Currency.USD, Currency.EUR, 0.85)
        
        val rate = repository.getExchangeRateSync(Currency.USD, Currency.EUR)
        assertEquals(0.85, rate)
    }
    
    @Test
    fun `getExchangeRate returns null for non-existent rate`() = runTest {
        val rate = repository.getExchangeRateSync(Currency.USD, Currency.EUR)
        assertNull(rate)
    }
    
    @Test
    fun `same currency returns 1_0`() = runTest {
        val rate = repository.getExchangeRateSync(Currency.USD, Currency.USD)
        assertEquals(1.0, rate)
    }
    
    @Test
    fun `getExchangeRate as flow returns rate`() = runTest {
        repository.setExchangeRate(Currency.EUR, Currency.GBP, 0.86)
        
        val rate = repository.getExchangeRate(Currency.EUR, Currency.GBP).first()
        assertEquals(0.86, rate)
    }
    
    // ============================================================
    // MULTIPLE RATES
    // ============================================================
    
    @Test
    fun `setRates stores multiple rates`() = runTest {
        val rates = mapOf(
            (Currency.USD to Currency.EUR) to 0.85,
            (Currency.USD to Currency.GBP) to 0.73,
            (Currency.USD to Currency.JPY) to 110.0
        )
        
        repository.setRates(rates)
        
        assertEquals(0.85, repository.getExchangeRateSync(Currency.USD, Currency.EUR))
        assertEquals(0.73, repository.getExchangeRateSync(Currency.USD, Currency.GBP))
        assertEquals(110.0, repository.getExchangeRateSync(Currency.USD, Currency.JPY))
    }
    
    @Test
    fun `setCommonRates loads standard test rates`() = runTest {
        repository.setCommonRates()
        
        assertNotNull(repository.getExchangeRateSync(Currency.USD, Currency.EUR))
        assertNotNull(repository.getExchangeRateSync(Currency.USD, Currency.GBP))
        assertNotNull(repository.getExchangeRateSync(Currency.USD, Currency.JPY))
    }
    
    // ============================================================
    // DATE-SPECIFIC RATES
    // ============================================================
    
    @Test
    fun `rate with date is stored separately`() = runTest {
        val date = LocalDateTime(2024, 11, 15, 12, 0)
        
        repository.setExchangeRate(Currency.USD, Currency.EUR, 0.85)
        repository.setExchangeRate(Currency.USD, Currency.EUR, 0.90, date)
        
        val rateWithoutDate = repository.getExchangeRateSync(Currency.USD, Currency.EUR)
        val rateWithDate = repository.getExchangeRateSync(Currency.USD, Currency.EUR, date)
        
        assertEquals(0.85, rateWithoutDate)
        assertEquals(0.90, rateWithDate)
    }
    
    // ============================================================
    // REFRESH OPERATIONS
    // ============================================================
    
    @Test
    fun `refreshExchangeRates succeeds by default`() = runTest {
        val result = repository.refreshExchangeRates(Currency.USD)
        
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `refreshExchangeRates increments call counter`() = runTest {
        assertEquals(0, repository.refreshExchangeRatesCalled)
        
        repository.refreshExchangeRates(Currency.USD)
        
        assertEquals(1, repository.refreshExchangeRatesCalled)
    }
    
    @Test
    fun `refreshExchangeRates stores last currency`() = runTest {
        repository.refreshExchangeRates(Currency.EUR)
        
        assertEquals(Currency.EUR, repository.lastRefreshCurrency)
    }
    
    @Test
    fun `refreshExchangeRates fails when configured`() = runTest {
        repository.setRefreshShouldFail(true)
        
        val result = repository.refreshExchangeRates(Currency.USD)
        
        assertTrue(result.isFailure)
    }
    
    // ============================================================
    // STALE RATE CHECKING
    // ============================================================
    
    @Test
    fun `isRateStale returns false by default`() = runTest {
        val isStale = repository.isRateStale(Currency.USD)
        assertFalse(isStale)
    }
    
    @Test
    fun `isRateStale returns configured value`() = runTest {
        repository.setRatesAreStale(true)
        
        val isStale = repository.isRateStale(Currency.USD)
        assertTrue(isStale)
    }
    
    // ============================================================
    // GET ALL RATES FOR BASE
    // ============================================================
    
    @Test
    fun `getAllRatesForBase returns empty map when no rates`() = runTest {
        val rates = repository.getAllRatesForBase(Currency.USD)
        assertTrue(rates.isEmpty())
    }
    
    @Test
    fun `getAllRatesForBase returns rates for specific base`() = runTest {
        repository.setExchangeRate(Currency.USD, Currency.EUR, 0.85)
        repository.setExchangeRate(Currency.USD, Currency.GBP, 0.73)
        repository.setExchangeRate(Currency.EUR, Currency.GBP, 0.86)
        
        val usdRates = repository.getAllRatesForBase(Currency.USD)
        
        assertEquals(2, usdRates.size)
        assertTrue(usdRates.containsKey(Currency.EUR))
        assertTrue(usdRates.containsKey(Currency.GBP))
    }
    
    // ============================================================
    // CLEAR OPERATIONS
    // ============================================================
    
    @Test
    fun `clearRates removes all rates`() = runTest {
        repository.setExchangeRate(Currency.USD, Currency.EUR, 0.85)
        repository.setExchangeRate(Currency.USD, Currency.GBP, 0.73)
        
        repository.clearRates()
        
        assertNull(repository.getExchangeRateSync(Currency.USD, Currency.EUR))
        assertNull(repository.getExchangeRateSync(Currency.USD, Currency.GBP))
    }
    
    @Test
    fun `clearOldRates increments call counter`() = runTest {
        assertEquals(0, repository.clearOldRatesCalled)
        
        repository.clearOldRates()
        
        assertEquals(1, repository.clearOldRatesCalled)
    }
    
    // ============================================================
    // ERROR SIMULATION
    // ============================================================
    
    @Test
    fun `shouldThrowError causes getExchangeRateSync to throw`() = runTest {
        repository.shouldThrowError = true
        
        assertFailsWith<Exception> {
            repository.getExchangeRateSync(Currency.USD, Currency.EUR)
        }
    }
    
    @Test
    fun `custom error message is used`() = runTest {
        repository.shouldThrowError = true
        repository.errorMessage = "Custom error"
        
        val exception = assertFailsWith<Exception> {
            repository.getExchangeRateSync(Currency.USD, Currency.EUR)
        }
        assertEquals("Custom error", exception.message)
    }
    
    // ============================================================
    // FLOW REACTIVITY
    // ============================================================
    
    @Test
    fun `flow emits updated rate`() = runTest {
        repository.setExchangeRate(Currency.USD, Currency.EUR, 0.85)
        val rate1 = repository.getExchangeRate(Currency.USD, Currency.EUR).first()
        assertEquals(0.85, rate1)
        
        repository.setExchangeRate(Currency.USD, Currency.EUR, 0.90)
        val rate2 = repository.getExchangeRate(Currency.USD, Currency.EUR).first()
        assertEquals(0.90, rate2)
    }
    
    // ============================================================
    // RESET
    // ============================================================
    
    @Test
    fun `reset clears all data`() = runTest {
        repository.setExchangeRate(Currency.USD, Currency.EUR, 0.85)
        repository.refreshExchangeRates(Currency.USD)
        
        repository.reset()
        
        assertNull(repository.getExchangeRateSync(Currency.USD, Currency.EUR))
        assertEquals(0, repository.refreshExchangeRatesCalled)
    }
    
    @Test
    fun `reset clears error flags`() = runTest {
        repository.shouldThrowError = true
        repository.setRefreshShouldFail(true)
        
        repository.reset()
        
        assertFalse(repository.shouldThrowError)
        // refreshShouldFail is private, so we test by checking that refresh succeeds
        val result = repository.refreshExchangeRates(Currency.USD)
        assertTrue(result.isSuccess)
    }
}

