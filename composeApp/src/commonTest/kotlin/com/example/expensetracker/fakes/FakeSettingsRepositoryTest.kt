package com.example.expensetracker.fakes

import com.example.expensetracker.helpers.TestData
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.ThemeOption
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlin.test.*

/**
 * Unit tests for FakeSettingsRepository
 * 
 * Validates that the fake implementation correctly implements
 * the ISettingsRepository interface contract.
 */
class FakeSettingsRepositoryTest {
    
    private lateinit var repository: FakeSettingsRepository
    
    @BeforeTest
    fun setup() {
        repository = FakeSettingsRepository()
    }
    
    @AfterTest
    fun tearDown() {
        repository.reset()
    }
    
    // ============================================================
    // INITIALIZATION TESTS
    // ============================================================
    
    @Test
    fun `initial base currency is USD`() = runTest {
        val currency = repository.getBaseCurrency().first()
        assertEquals(Currency.USD, currency)
    }
    
    @Test
    fun `initial theme option is SYSTEM`() = runTest {
        val theme = repository.getThemeOption().first()
        assertEquals(ThemeOption.SYSTEM, theme)
    }
    
    @Test
    fun `initial voice input is disabled`() = runTest {
        val enabled = repository.getVoiceInputEnabled().first()
        assertFalse(enabled)
    }
    
    @Test
    fun `initial API key is empty`() = runTest {
        val apiKey = repository.getApiKey().first()
        assertEquals("", apiKey)
    }
    
    // ============================================================
    // BASE CURRENCY TESTS
    // ============================================================
    
    @Test
    fun `setBaseCurrency updates currency`() = runTest {
        repository.setBaseCurrency(Currency.EUR)
        
        val currency = repository.getBaseCurrency().first()
        assertEquals(Currency.EUR, currency)
    }
    
    @Test
    fun `updateBaseCurrency updates currency`() = runTest {
        repository.updateBaseCurrency(Currency.GBP)
        
        val currency = repository.getBaseCurrencySync()
        assertEquals(Currency.GBP, currency)
    }
    
    @Test
    fun `getBaseCurrencySync returns current currency`() = runTest {
        repository.setBaseCurrency(Currency.JPY)
        
        val currency = repository.getBaseCurrencySync()
        assertEquals(Currency.JPY, currency)
    }
    
    // ============================================================
    // API KEY TESTS
    // ============================================================
    
    @Test
    fun `setApiKey updates API key`() = runTest {
        repository.setApiKey("new-api-key")
        
        val apiKey = repository.getApiKey().first()
        assertEquals("new-api-key", apiKey)
    }
    
    @Test
    fun `updateApiKey updates API key`() = runTest {
        repository.updateApiKey("updated-key")
        
        val apiKey = repository.getApiKeySync()
        assertEquals("updated-key", apiKey)
    }
    
    @Test
    fun `isApiConfigured returns false for empty key`() = runTest {
        val configured = repository.isApiConfigured()
        assertFalse(configured)
    }
    
    @Test
    fun `isApiConfigured returns true for non-empty key`() = runTest {
        repository.setApiKey("valid-key")
        
        val configured = repository.isApiConfigured()
        assertTrue(configured)
    }
    
    // ============================================================
    // API BASE URL TESTS
    // ============================================================
    
    @Test
    fun `setApiBaseUrl updates base URL`() = runTest {
        repository.setApiBaseUrl("https://new-api.example.com")
        
        val url = repository.getApiBaseUrl().first()
        assertEquals("https://new-api.example.com", url)
    }
    
    @Test
    fun `updateApiBaseUrl updates base URL`() = runTest {
        repository.updateApiBaseUrl("https://updated-api.com")
        
        val url = repository.getApiBaseUrlSync()
        assertEquals("https://updated-api.com", url)
    }
    
    // ============================================================
    // THEME OPTION TESTS
    // ============================================================
    
    @Test
    fun `setThemeOption updates theme`() = runTest {
        repository.setThemeOption(ThemeOption.LIGHT)
        
        val theme = repository.getThemeOption().first()
        assertEquals(ThemeOption.LIGHT, theme)
    }
    
    @Test
    fun `updateThemeOption updates theme`() = runTest {
        repository.updateThemeOption(ThemeOption.DARK)
        
        val theme = repository.getThemeOptionSync()
        assertEquals(ThemeOption.DARK, theme)
    }
    
    // ============================================================
    // VOICE INPUT TESTS
    // ============================================================
    
    @Test
    fun `setVoiceInputEnabled enables voice input`() = runTest {
        repository.setVoiceInputEnabled(true)
        
        val enabled = repository.getVoiceInputEnabled().first()
        assertTrue(enabled)
    }
    
    @Test
    fun `updateVoiceInputEnabled updates state`() = runTest {
        repository.updateVoiceInputEnabled(true)
        
        val enabled = repository.getVoiceInputEnabledSync()
        assertTrue(enabled)
    }
    
    // ============================================================
    // EXCHANGE RATE UPDATE TIMESTAMP TESTS
    // ============================================================
    
    @Test
    fun `initial last update is null`() = runTest {
        val timestamp = repository.getLastExchangeRateUpdate().first()
        assertNull(timestamp)
    }
    
    @Test
    fun `updateLastExchangeRateUpdate sets timestamp`() = runTest {
        val now = LocalDateTime(2024, 11, 20, 12, 30)
        
        repository.updateLastExchangeRateUpdate(now)
        
        val timestamp = repository.getLastExchangeRateUpdate().first()
        assertEquals(now, timestamp)
    }
    
    // ============================================================
    // FULL SETTINGS TESTS
    // ============================================================
    
    @Test
    fun `getSettings returns complete settings`() = runTest {
        repository.setBaseCurrency(Currency.EUR)
        repository.setApiKey("test-key")
        repository.setThemeOption(ThemeOption.DARK)
        
        val settings = repository.getSettings().first()
        
        assertEquals(Currency.EUR, settings.baseCurrency)
        assertEquals("test-key", settings.exchangeRateApiKey)
        assertEquals(ThemeOption.DARK, settings.themeOption)
    }
    
    @Test
    fun `saveSettings updates all settings`() = runTest {
        val settings = TestData.createAppSettings(
            baseCurrency = Currency.GBP,
            apiKey = "saved-key",
            themeOption = ThemeOption.LIGHT,
            voiceInputEnabled = true
        )
        
        repository.saveSettings(settings)
        
        val retrieved = repository.getSettingsSync()
        assertEquals(Currency.GBP, retrieved.baseCurrency)
        assertEquals("saved-key", retrieved.exchangeRateApiKey)
        assertEquals(ThemeOption.LIGHT, retrieved.themeOption)
        assertTrue(retrieved.isVoiceInputEnabled)
    }
    
    // ============================================================
    // FLOW REACTIVITY TESTS
    // ============================================================
    
    @Test
    fun `base currency flow emits updates`() = runTest {
        val initial = repository.getBaseCurrency().first()
        assertEquals(Currency.USD, initial)
        
        repository.setBaseCurrency(Currency.EUR)
        
        val updated = repository.getBaseCurrency().first()
        assertEquals(Currency.EUR, updated)
    }
    
    @Test
    fun `theme option flow emits updates`() = runTest {
        repository.setThemeOption(ThemeOption.DARK)
        
        val theme = repository.getThemeOption().first()
        assertEquals(ThemeOption.DARK, theme)
    }
    
    // ============================================================
    // ERROR SIMULATION TESTS
    // ============================================================
    
    @Test
    fun `shouldThrowError flag can be set`() = runTest {
        assertFalse(repository.shouldThrowError)
        
        repository.shouldThrowError = true
        
        assertTrue(repository.shouldThrowError)
    }
    
    // ============================================================
    // RESET TESTS
    // ============================================================
    
    @Test
    fun `reset restores default settings`() = runTest {
        repository.setBaseCurrency(Currency.EUR)
        repository.setApiKey("test-key")
        repository.setThemeOption(ThemeOption.DARK)
        repository.setVoiceInputEnabled(true)
        
        repository.reset()
        
        val settings = repository.getSettings().first()
        assertEquals(Currency.USD, settings.baseCurrency)
        assertEquals("", settings.exchangeRateApiKey)
        assertEquals(ThemeOption.SYSTEM, settings.themeOption)
        assertFalse(settings.isVoiceInputEnabled)
    }
    
    @Test
    fun `reset clears error flags`() = runTest {
        repository.shouldThrowError = true
        
        repository.reset()
        
        assertFalse(repository.shouldThrowError)
    }
}

