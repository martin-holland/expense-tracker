# Unit Testing Implementation Guide

Quick start guide for implementing unit tests for ExpenseTracker ViewModels and Repositories.

## 📋 Quick Reference

### Test Count Summary
- **Total Tests:** 448
- **ViewModels:** 274 tests (6 classes)
- **Repositories:** 174 tests (3 classes)
- **Estimated Effort:** 70 hours (~2 weeks)

### Complexity Ratings
| Component | Tests | Complexity | Priority |
|-----------|-------|------------|----------|
| AddExpenseViewModel | 44 | Medium-High | 🔴 Critical |
| ExpenseRepository | 52 | Medium | 🔴 Critical |
| ExpenseHistoryViewModel | 59 | High | 🟡 High |
| DashBoardViewModel | 31 | Medium | 🟡 High |
| SettingsRepository | 53 | Medium | 🟡 High |
| ExchangeRateRepository | 69 | Very High | 🟢 Medium |
| SettingsViewModel | 55 | High | 🟢 Medium |
| CurrencyExchangeViewModel | 50 | High | 🟢 Medium |
| VoiceInputViewModel | 35 | High | ⚪ Low |

---

## 🚀 Getting Started

### Step 1: Add Dependencies

#### Update `gradle/libs.versions.toml`

```toml
[versions]
# ... existing versions ...
coroutines-test = "1.9.0"
turbine = "1.1.0"

[libraries]
# ... existing libraries ...
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines-test" }
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }
```

#### Update `composeApp/build.gradle.kts`

```kotlin
kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
        }
    }
}
```

### Step 2: Sync Project

```bash
# In terminal
./gradlew clean build

# Or in Android Studio: File → Sync Project with Gradle Files
```

---

## 📁 Project Structure

Create the following directory structure:

```
composeApp/src/commonTest/kotlin/com/example/expensetracker/
├── viewmodel/
│   ├── AddExpenseViewModelTest.kt
│   ├── DashBoardViewModelTest.kt
│   ├── ExpenseHistoryViewModelTest.kt
│   ├── SettingsViewModelTest.kt
│   ├── CurrencyExchangeViewModelTest.kt
│   └── VoiceInputViewModelTest.kt
├── repository/
│   ├── ExpenseRepositoryTest.kt
│   ├── SettingsRepositoryTest.kt
│   └── ExchangeRateRepositoryTest.kt
├── fakes/
│   ├── FakeExpenseRepository.kt
│   ├── FakeSettingsRepository.kt
│   ├── FakeExchangeRateRepository.kt
│   ├── FakeCurrencyConverter.kt
│   ├── FakeExpenseDao.kt
│   ├── FakeSettingsDao.kt
│   ├── FakeExchangeRateDao.kt
│   ├── FakeMicrophoneService.kt
│   └── FakeExchangeRateApiService.kt
└── helpers/
    ├── TestData.kt
    ├── TestExtensions.kt
    └── FlowTestExtensions.kt
```

---

## 🛠️ Step 3: Create Test Infrastructure

### Create Test Helpers

**File:** `helpers/TestData.kt`

```kotlin
package com.example.expensetracker.helpers

import com.example.expensetracker.model.*
import kotlinx.datetime.LocalDateTime
import kotlin.random.Random

object TestData {
    
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
    
    fun createExpenses(count: Int): List<Expense> {
        return List(count) { index ->
            createExpense(
                id = "test-$index",
                amount = (index + 1) * 10.0
            )
        }
    }
    
    fun createExpenseWithDate(
        year: Int,
        month: Int,
        day: Int,
        amount: Double = 50.0
    ): Expense = createExpense(
        date = LocalDateTime(year, month, day, 12, 0),
        amount = amount
    )
    
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
}
```

**File:** `helpers/FlowTestExtensions.kt`

```kotlin
package com.example.expensetracker.helpers

import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

/**
 * Test that a flow emits expected values in order
 */
suspend fun <T> Flow<T>.testEmissions(
    vararg expectedValues: T,
    timeout: kotlin.time.Duration = 5.seconds
) {
    test(timeout = timeout) {
        expectedValues.forEach { expected ->
            val item = awaitItem()
            assertEquals(expected, item)
        }
        cancelAndIgnoreRemainingEvents()
    }
}

/**
 * Test that a flow emits a single value
 */
suspend fun <T> Flow<T>.testSingleEmission(
    expectedValue: T,
    timeout: kotlin.time.Duration = 5.seconds
) {
    test(timeout = timeout) {
        assertEquals(expectedValue, awaitItem())
        cancelAndIgnoreRemainingEvents()
    }
}
```

---

## 🧪 Step 4: Create Fake Implementations

### FakeExpenseRepository

**File:** `fakes/FakeExpenseRepository.kt`

```kotlin
package com.example.expensetracker.fakes

import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.ExpenseCategory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDateTime

class FakeExpenseRepository : ExpenseRepository(FakeExpenseDao()) {
    
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
    val savedExpenses: List<Expense> get() = expenses.toList()
    
    override fun getAllExpenses(): Flow<List<Expense>> {
        getAllExpensesCalled = true
        return if (shouldThrowError) {
            flow { throw Exception(errorMessage) }
        } else {
            _expensesFlow
        }
    }
    
    override suspend fun insertExpense(expense: Expense) {
        insertExpenseCalled = true
        if (delayMs > 0) delay(delayMs)
        if (shouldThrowError) throw Exception(errorMessage)
        
        expenses.add(expense)
        _expensesFlow.value = expenses.toList()
    }
    
    override suspend fun deleteExpense(expense: Expense) {
        deleteExpenseCalled = true
        if (shouldThrowError) throw Exception(errorMessage)
        
        expenses.removeAll { it.id == expense.id }
        _expensesFlow.value = expenses.toList()
    }
    
    override suspend fun getExpenseById(id: String): Expense? {
        if (shouldThrowError) throw Exception(errorMessage)
        return expenses.find { it.id == id }
    }
    
    override fun getExpensesByCategory(category: ExpenseCategory): Flow<List<Expense>> {
        return flow {
            emit(expenses.filter { it.category == category })
        }
    }
    
    override fun getExpensesByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<Expense>> {
        return flow {
            emit(expenses.filter { it.date >= startDate && it.date <= endDate })
        }
    }
    
    override fun getExpensesByAmountRange(
        minAmount: Double,
        maxAmount: Double
    ): Flow<List<Expense>> {
        return flow {
            emit(expenses.filter { it.amount >= minAmount && it.amount <= maxAmount })
        }
    }
    
    override suspend fun getExpenseCount(): Int {
        return expenses.size
    }
    
    // Test helper methods
    fun setExpenses(newExpenses: List<Expense>) {
        expenses.clear()
        expenses.addAll(newExpenses)
        _expensesFlow.value = expenses.toList()
    }
    
    fun addExpense(expense: Expense) {
        expenses.add(expense)
        _expensesFlow.value = expenses.toList()
    }
    
    fun reset() {
        expenses.clear()
        _expensesFlow.value = emptyList()
        shouldThrowError = false
        errorMessage = "Test error"
        delayMs = 0L
        getAllExpensesCalled = false
        insertExpenseCalled = false
        deleteExpenseCalled = false
    }
}
```

### FakeSettingsRepository

**File:** `fakes/FakeSettingsRepository.kt`

```kotlin
package com.example.expensetracker.fakes

import com.example.expensetracker.data.repository.SettingsRepository
import com.example.expensetracker.model.AppSettings
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.ThemeOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDateTime

class FakeSettingsRepository : SettingsRepository(FakeSettingsDao()) {
    
    private var settings = AppSettings()
    private val _settingsFlow = MutableStateFlow(settings)
    
    var shouldThrowError = false
    var errorMessage = "Test error"
    
    override fun getSettings(): Flow<AppSettings> = _settingsFlow
    
    override suspend fun getSettingsSync(): AppSettings {
        if (shouldThrowError) throw Exception(errorMessage)
        return settings
    }
    
    override fun getBaseCurrency(): Flow<Currency> {
        return MutableStateFlow(settings.baseCurrency)
    }
    
    override suspend fun getBaseCurrencySync(): Currency {
        return settings.baseCurrency
    }
    
    override suspend fun setBaseCurrency(currency: Currency) {
        if (shouldThrowError) throw Exception(errorMessage)
        settings = settings.copy(baseCurrency = currency)
        _settingsFlow.value = settings
    }
    
    override fun getApiKey(): Flow<String> {
        return MutableStateFlow(settings.exchangeRateApiKey)
    }
    
    override suspend fun getApiKeySync(): String {
        return settings.exchangeRateApiKey
    }
    
    override suspend fun setApiKey(apiKey: String) {
        settings = settings.copy(exchangeRateApiKey = apiKey)
        _settingsFlow.value = settings
    }
    
    override fun getThemeOption(): Flow<ThemeOption> {
        return MutableStateFlow(settings.themeOption)
    }
    
    override suspend fun setThemeOption(themeOption: ThemeOption) {
        settings = settings.copy(themeOption = themeOption)
        _settingsFlow.value = settings
    }
    
    override fun getVoiceInputEnabled(): Flow<Boolean> {
        return MutableStateFlow(settings.isVoiceInputEnabled)
    }
    
    override suspend fun setVoiceInputEnabled(isEnabled: Boolean) {
        settings = settings.copy(isVoiceInputEnabled = isEnabled)
        _settingsFlow.value = settings
    }
    
    // ... implement remaining methods
    
    fun reset() {
        settings = AppSettings()
        _settingsFlow.value = settings
        shouldThrowError = false
    }
}
```

---

## 📝 Step 5: Write Your First Test

### Example: AddExpenseViewModel Test

**File:** `viewmodel/AddExpenseViewModelTest.kt`

```kotlin
package com.example.expensetracker.viewmodel

import com.example.expensetracker.fakes.FakeExpenseRepository
import com.example.expensetracker.helpers.TestData
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.ExpenseCategory
import com.example.expensetracker.view.components.SnackbarType
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class AddExpenseViewModelTest {
    
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
    fun `initial state has empty form fields`() {
        assertEquals("", viewModel.amount)
        assertEquals("", viewModel.note)
        assertNull(viewModel.category)
        assertNull(viewModel.errorMessage)
        assertFalse(viewModel.isSaving)
        assertNull(viewModel.snackbarMessage)
    }
    
    @Test
    fun `default currency is USD`() {
        assertEquals(Currency.USD, viewModel.currency)
    }
    
    @Test
    fun `date is set to today on init`() {
        assertTrue(viewModel.date.isNotBlank())
        // Check format: "Month Day, Year"
        assertTrue(viewModel.date.matches(Regex("\\w+ \\d+, \\d{4}")))
    }
    
    // ============================================
    // Form Field Update Tests
    // ============================================
    
    @Test
    fun `onCurrencySelected updates currency and clears error`() {
        viewModel.errorMessage = "Some error"
        
        viewModel.onCurrencySelected(Currency.EUR)
        
        assertEquals(Currency.EUR, viewModel.currency)
        assertNull(viewModel.errorMessage)
    }
    
    @Test
    fun `onAmountChanged accepts valid decimal values`() {
        val validAmounts = listOf("45.50", "100", "0.99", "")
        
        validAmounts.forEach { amount ->
            viewModel.onAmountChanged(amount)
            assertEquals(amount, viewModel.amount)
        }
    }
    
    @Test
    fun `onAmountChanged rejects invalid input`() {
        viewModel.onAmountChanged("50.00")
        assertEquals("50.00", viewModel.amount)
        
        // Try invalid inputs - amount should remain unchanged
        val invalidInputs = listOf("abc", "12.34.56", "12a", "-10")
        invalidInputs.forEach { invalid ->
            viewModel.onAmountChanged(invalid)
            assertEquals("50.00", viewModel.amount)
        }
    }
    
    @Test
    fun `onCategorySelected updates category and clears error`() {
        viewModel.errorMessage = "Some error"
        
        viewModel.onCategorySelected(ExpenseCategory.FOOD)
        
        assertEquals(ExpenseCategory.FOOD, viewModel.category)
        assertNull(viewModel.errorMessage)
    }
    
    @Test
    fun `onNoteChanged updates note`() {
        viewModel.onNoteChanged("Test note")
        assertEquals("Test note", viewModel.note)
    }
    
    // ============================================
    // Validation Tests
    // ============================================
    
    @Test
    fun `empty amount shows validation error`() {
        viewModel.amount = ""
        viewModel.category = ExpenseCategory.FOOD
        viewModel.date = "November 7, 2024"
        
        viewModel.saveExpense()
        
        assertNotNull(viewModel.snackbarMessage)
        assertEquals(SnackbarType.ERROR, viewModel.snackbarMessage?.type)
        assertTrue(
            viewModel.snackbarMessage?.message?.contains("amount", ignoreCase = true) == true
        )
    }
    
    @Test
    fun `invalid amount shows validation error`() {
        viewModel.amount = "." // Just a dot is invalid
        viewModel.category = ExpenseCategory.FOOD
        
        viewModel.saveExpense()
        
        assertNotNull(viewModel.snackbarMessage)
        assertEquals(SnackbarType.ERROR, viewModel.snackbarMessage?.type)
    }
    
    @Test
    fun `null category shows validation error`() {
        viewModel.amount = "50.00"
        viewModel.category = null
        
        viewModel.saveExpense()
        
        assertNotNull(viewModel.snackbarMessage)
        assertTrue(
            viewModel.snackbarMessage?.message?.contains("category", ignoreCase = true) == true
        )
    }
    
    // ============================================
    // Save Operation Tests
    // ============================================
    
    @Test
    fun `saveExpense validates before saving`() = runTest {
        viewModel.amount = "" // Invalid
        
        viewModel.saveExpense()
        advanceUntilIdle()
        
        // Nothing should be saved
        assertEquals(0, fakeRepository.savedExpenses.size)
        assertNotNull(viewModel.snackbarMessage)
        assertEquals(SnackbarType.ERROR, viewModel.snackbarMessage?.type)
    }
    
    @Test
    fun `valid expense saves to repository`() = runTest {
        viewModel.amount = "75.50"
        viewModel.category = ExpenseCategory.TRAVEL
        viewModel.note = "Gas station"
        viewModel.currency = Currency.USD
        viewModel.date = "November 7, 2024"
        
        viewModel.saveExpense()
        advanceUntilIdle()
        
        // Verify saved
        assertEquals(1, fakeRepository.savedExpenses.size)
        val saved = fakeRepository.savedExpenses.first()
        assertEquals(75.5, saved.amount)
        assertEquals(ExpenseCategory.TRAVEL, saved.category)
        assertEquals("Gas station", saved.description)
        assertEquals(Currency.USD, saved.currency)
    }
    
    @Test
    fun `successful save shows success snackbar`() = runTest {
        viewModel.amount = "50.00"
        viewModel.category = ExpenseCategory.FOOD
        
        viewModel.saveExpense()
        advanceUntilIdle()
        
        assertNotNull(viewModel.snackbarMessage)
        assertEquals(SnackbarType.SUCCESS, viewModel.snackbarMessage?.type)
        assertTrue(
            viewModel.snackbarMessage?.message?.contains("success", ignoreCase = true) == true
        )
    }
    
    @Test
    fun `successful save clears form`() = runTest {
        viewModel.amount = "50.00"
        viewModel.category = ExpenseCategory.FOOD
        viewModel.note = "Test note"
        
        viewModel.saveExpense()
        advanceUntilIdle()
        
        // Form should be cleared
        assertEquals("", viewModel.amount)
        assertNull(viewModel.category)
        assertEquals("", viewModel.note)
    }
    
    @Test
    fun `save failure shows error snackbar`() = runTest {
        fakeRepository.shouldThrowError = true
        fakeRepository.errorMessage = "Database error"
        
        viewModel.amount = "50.00"
        viewModel.category = ExpenseCategory.FOOD
        
        viewModel.saveExpense()
        advanceUntilIdle()
        
        assertNotNull(viewModel.snackbarMessage)
        assertEquals(SnackbarType.ERROR, viewModel.snackbarMessage?.type)
        assertTrue(
            viewModel.snackbarMessage?.message?.contains("Failed", ignoreCase = true) == true
        )
    }
    
    @Test
    fun `isSaving flag updates correctly during save`() = runTest {
        fakeRepository.delayMs = 100 // Simulate slow save
        
        viewModel.amount = "50.00"
        viewModel.category = ExpenseCategory.FOOD
        
        assertFalse(viewModel.isSaving)
        
        viewModel.saveExpense()
        // Flag should be set immediately
        assertTrue(viewModel.isSaving)
        
        advanceUntilIdle()
        // Flag should be cleared after save
        assertFalse(viewModel.isSaving)
    }
    
    @Test
    fun `generated expense ID is unique`() = runTest {
        viewModel.amount = "10.00"
        viewModel.category = ExpenseCategory.FOOD
        viewModel.saveExpense()
        advanceUntilIdle()
        val id1 = fakeRepository.savedExpenses.first().id
        
        viewModel.amount = "20.00"
        viewModel.saveExpense()
        advanceUntilIdle()
        val id2 = fakeRepository.savedExpenses.last().id
        
        assertNotEquals(id1, id2)
        assertTrue(id1.startsWith("expense_"))
        assertTrue(id2.startsWith("expense_"))
    }
    
    // ============================================
    // Form Management Tests
    // ============================================
    
    @Test
    fun `resetForm clears all fields`() {
        viewModel.amount = "50.00"
        viewModel.category = ExpenseCategory.FOOD
        viewModel.note = "Test"
        
        viewModel.resetForm()
        
        assertEquals("", viewModel.amount)
        assertEquals("", viewModel.note)
        // Category reset to default FOOD
        assertEquals(ExpenseCategory.FOOD, viewModel.category)
        // Currency kept
        assertEquals(Currency.USD, viewModel.currency)
    }
    
    @Test
    fun `dismissSnackbar clears snackbar message`() = runTest {
        viewModel.amount = "50.00"
        viewModel.category = ExpenseCategory.FOOD
        viewModel.saveExpense()
        advanceUntilIdle()
        
        assertNotNull(viewModel.snackbarMessage)
        
        viewModel.dismissSnackbar()
        
        assertNull(viewModel.snackbarMessage)
    }
}
```

---

## ▶️ Step 6: Run Tests

### Via Command Line

```bash
# Run all common tests
./gradlew :composeApp:cleanTestDebugUnitTest :composeApp:testDebugUnitTest

# Run specific test class
./gradlew :composeApp:testDebugUnitTest --tests "com.example.expensetracker.viewmodel.AddExpenseViewModelTest"

# Run with coverage
./gradlew :composeApp:testDebugUnitTest jacocoTestReport
```

### Via Android Studio

1. Right-click on test class or method
2. Select "Run 'TestName'"
3. View results in Run panel
4. Green ✅ = Pass, Red ❌ = Fail

---

## 📊 Step 7: Measure Coverage

### Generate Coverage Report

```bash
./gradlew :composeApp:testDebugUnitTest jacocoTestReport
```

### View Report

Open: `composeApp/build/reports/jacoco/test/html/index.html`

### Target Coverage
- **ViewModels:** 80-90%
- **Repositories:** 85-95%
- **Overall:** 80%+

---

## ✅ Implementation Checklist

### Week 1: Foundation
- [ ] Add test dependencies
- [ ] Create test directory structure
- [ ] Implement FakeExpenseRepository
- [ ] Implement FakeSettingsRepository
- [ ] Create TestData helpers
- [ ] Write AddExpenseViewModel tests (44 tests)
- [ ] Write ExpenseRepository tests (52 tests)
- [ ] **Target:** 96 tests passing

### Week 2: Core Features
- [ ] Implement FakeExchangeRateRepository
- [ ] Write ExpenseHistoryViewModel tests (59 tests)
- [ ] Write DashBoardViewModel tests (31 tests)
- [ ] Write SettingsRepository tests (53 tests)
- [ ] **Target:** 239 total tests passing

### Week 3: Advanced Features
- [ ] Implement FakeMicrophoneService
- [ ] Implement FakeExchangeRateApiService
- [ ] Write ExchangeRateRepository tests (69 tests)
- [ ] Write SettingsViewModel tests (55 tests)
- [ ] Write CurrencyExchangeViewModel tests (50 tests)
- [ ] Write VoiceInputViewModel tests (35 tests)
- [ ] **Target:** 448 total tests passing

### Week 4: Quality & Polish
- [ ] Fix all failing tests
- [ ] Achieve 80%+ coverage
- [ ] Document test patterns
- [ ] Create CI/CD pipeline
- [ ] Code review

---

## 🐛 Troubleshooting

### Common Issues

#### Issue: "Cannot find symbol: runTest"
**Solution:** Add coroutines-test dependency

```kotlin
commonTest.dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}
```

#### Issue: "Flow not emitting in tests"
**Solution:** Use `advanceUntilIdle()` after async operations

```kotlin
@Test
fun `test flow emission`() = runTest {
    viewModel.doSomething()
    advanceUntilIdle() // Wait for coroutines to complete
    
    assertEquals(expected, viewModel.state.value)
}
```

#### Issue: "ViewModel dependencies are singletons"
**Solution:** Add dependency injection or reset singletons between tests

```kotlin
@AfterTest
fun tearDown() {
    ExpenseRepository.resetInstance()
    SettingsRepository.resetInstance()
}
```

#### Issue: "Tests are flaky"
**Solution:** Use proper test dispatchers

```kotlin
@Test
fun `test with proper dispatcher`() = runTest {
    val testDispatcher = StandardTestDispatcher(testScheduler)
    Dispatchers.setMain(testDispatcher)
    
    // Your test code
    
    Dispatchers.resetMain()
}
```

---

## 📚 Additional Resources

### Documentation
- [Kotlin Test Documentation](https://kotlinlang.org/api/latest/kotlin.test/)
- [Coroutines Test Guide](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/)
- [Turbine Flow Testing](https://github.com/cashapp/turbine)

### Your Project Docs
- [Full Test Plan](./UNIT_TEST_PLAN.md)
- [ViewModel Specifications](./VIEWMODEL_TEST_SPECIFICATIONS.md)
- [Database Testing](../database/IMPLEMENTATION.md#testing)

---

## 🎯 Success Criteria

### Definition of Done
- ✅ All 448 planned tests implemented
- ✅ All tests passing consistently
- ✅ 80%+ code coverage achieved
- ✅ No flaky tests
- ✅ Tests run in < 5 minutes total
- ✅ CI/CD pipeline integrated
- ✅ Documentation complete

### Quality Gates
- **Build:** All tests must pass before merge
- **Coverage:** Minimum 80% for ViewModels/Repositories
- **Performance:** Test suite < 5 minutes
- **Maintainability:** Fakes are reusable and well-documented

---

## 🚢 Next Steps

1. ✅ Review this guide
2. ✅ Set up test dependencies
3. ✅ Create one complete test file (AddExpenseViewModel)
4. ✅ Run and verify tests pass
5. ✅ Create remaining fakes
6. ✅ Implement tests by priority
7. ✅ Measure and improve coverage
8. ✅ Document learnings

**Ready to start? Begin with [Step 1: Add Dependencies](#step-1-add-dependencies)**

