# Unit Testing Documentation

Comprehensive unit testing plan for ExpenseTracker ViewModels and Repositories.

---

## 📑 Documentation Index

| Document | Purpose | Audience |
|----------|---------|----------|
| **[README.md](./README.md)** (this file) | Overview and quick start | Everyone |
| **[UNIT_TEST_PLAN.md](./UNIT_TEST_PLAN.md)** | Detailed test specifications | Developers, QA |
| **[VIEWMODEL_TEST_SPECIFICATIONS.md](./VIEWMODEL_TEST_SPECIFICATIONS.md)** | ViewModel test examples | Developers |
| **[IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md)** | Step-by-step setup guide | Developers |

---

## 🎯 Overview

### Project Status
- **Current Coverage:** Minimal (only example test)
- **Target Coverage:** 80%+ for ViewModels and Repositories
- **Planned Tests:** 448 unit tests
- **Estimated Effort:** 70 hours (~2 weeks for 1 developer)

### What's Being Tested

#### ✅ In Scope (Unit Tests)
- **6 ViewModels:** Business logic, state management, validation
- **3 Repositories:** Data operations, caching, error handling
- **Domain Logic:** Aggregations, calculations, transformations
- **Flow Emissions:** StateFlow updates and transformations

#### ❌ Out of Scope (Not Unit Tests)
- UI Composables (use instrumented tests)
- Platform-specific implementations (expect/actual)
- Database migrations
- Network integration
- Permission handling

---

## 📊 Test Breakdown

### ViewModels (274 tests)

| ViewModel | Tests | Complexity | Priority | Status |
|-----------|-------|------------|----------|--------|
| VoiceInputViewModel | 35 | High | Low | 📋 Planned |
| AddExpenseViewModel | 44 | Medium-High | **Critical** | 📋 Planned |
| DashBoardViewModel | 31 | Medium | High | 📋 Planned |
| SettingsViewModel | 55 | High | Medium | 📋 Planned |
| ExpenseHistoryViewModel | 59 | High | High | 📋 Planned |
| CurrencyExchangeViewModel | 50 | High | Medium | 📋 Planned |

### Repositories (174 tests)

| Repository | Tests | Complexity | Priority | Status |
|------------|-------|------------|----------|--------|
| ExpenseRepository | 52 | Medium | **Critical** | 📋 Planned |
| SettingsRepository | 53 | Medium | High | 📋 Planned |
| ExchangeRateRepository | 69 | Very High | Medium | 📋 Planned |

---

## 🚀 Quick Start

### For the Impatient

```bash
# 1. Add dependencies to build.gradle.kts
commonTest.dependencies {
    implementation(libs.kotlin.test)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    implementation("app.cash.turbine:turbine:1.1.0")
}

# 2. Sync project
./gradlew clean build

# 3. Create your first test (see IMPLEMENTATION_GUIDE.md)

# 4. Run tests
./gradlew :composeApp:testDebugUnitTest
```

**👉 Full setup:** See [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md)

---

## 📖 Component Summaries

### 1. VoiceInputViewModel (35 tests)

**Purpose:** Manages voice recording, speech recognition, and expense parsing.

**Key Responsibilities:**
- Audio recording state
- Speech recognition lifecycle
- Transcription parsing
- Manual text entry fallback
- Error handling

**Test Categories:**
- State Management (15)
- Audio Recording (5)
- Parsing (8)
- Manual Entry (7)

**Example Test:**
```kotlin
@Test
fun `onSpeechResult with Success updates state correctly`() {
    val result = SpeechRecognitionResult.Success(
        text = "Lunch 45 euros",
        confidence = 0.95f,
        alternatives = listOf()
    )
    
    viewModel.onSpeechResult(result)
    
    val state = viewModel.speechRecognitionState.value
    assertTrue(state is SpeechRecognitionState.Success)
    assertEquals("Lunch 45 euros", (state as SpeechRecognitionState.Success).transcription)
}
```

---

### 2. AddExpenseViewModel (44 tests) ⭐ CRITICAL

**Purpose:** Manages expense creation form and validation.

**Key Responsibilities:**
- Form field management
- Input validation
- Date handling
- Save operation
- Voice integration

**Test Categories:**
- Initialization (3)
- Form Field Updates (8)
- Date Management (5)
- Validation (8)
- Save Operation (10)
- Form Management (5)
- Voice Integration (5)

**Example Test:**
```kotlin
@Test
fun `valid expense saves to repository`() = runTest {
    viewModel.amount = "75.50"
    viewModel.category = ExpenseCategory.TRAVEL
    viewModel.note = "Gas station"
    
    viewModel.saveExpense()
    advanceUntilIdle()
    
    assertEquals(1, fakeRepository.savedExpenses.size)
    val saved = fakeRepository.savedExpenses.first()
    assertEquals(75.5, saved.amount)
}
```

---

### 3. DashBoardViewModel (31 tests)

**Purpose:** Displays expense analytics and aggregations.

**Key Responsibilities:**
- Load and observe expenses
- Calculate monthly aggregates
- Compare month-over-month
- Generate category totals
- Create weekly/daily breakdowns

**Test Categories:**
- Initialization (3)
- Data Loading (8)
- Aggregation (15)
- UI State (5)

**Example Test:**
```kotlin
@Test
fun `month-over-month change computed correctly`() = runTest {
    val thisMonth = LocalDateTime(2024, 11, 15, 12, 0)
    val lastMonth = LocalDateTime(2024, 10, 15, 12, 0)
    
    val expenses = listOf(
        createExpense(date = thisMonth, amount = 200.0),
        createExpense(date = lastMonth, amount = 100.0)
    )
    
    fakeRepository.setExpenses(expenses)
    advanceUntilIdle()
    
    // 200 vs 100 = 100% increase
    assertEquals(1.0, viewModel.uiState.value.monthOverMonthChange, 0.01)
}
```

---

### 4. SettingsViewModel (55 tests)

**Purpose:** Manages application settings and preferences.

**Key Responsibilities:**
- Currency selection
- API configuration
- Theme preferences
- Voice input settings
- Exchange rate refresh

**Test Categories:**
- Initialization (5)
- Settings Loading (10)
- Currency Management (8)
- API Configuration (12)
- Theme Settings (5)
- Voice Input Settings (7)
- Flow Observations (8)

**Example Test:**
```kotlin
@Test
fun `updateApiKey saves to repository`() = runTest {
    viewModel.updateApiKey("new-api-key-123")
    advanceUntilIdle()
    
    assertEquals("new-api-key-123", viewModel.apiKey.value)
    assertTrue(viewModel.isApiConfigured.value)
}
```

---

### 5. ExpenseHistoryViewModel (59 tests)

**Purpose:** Displays and manages expense list with filtering.

**Key Responsibilities:**
- Load and display expenses
- Currency conversion
- Multi-criteria filtering
- Expense deletion
- Expense editing

**Test Categories:**
- Initialization (4)
- Expense Loading (8)
- Currency Conversion (12)
- Filtering (15)
- Expense Deletion (8)
- Expense Editing (7)
- UI State (5)

**Example Test:**
```kotlin
@Test
fun `applyFilters with category filters expenses correctly`() = runTest {
    val expenses = listOf(
        createExpense(category = ExpenseCategory.FOOD, amount = 50.0),
        createExpense(category = ExpenseCategory.TRAVEL, amount = 100.0)
    )
    
    fakeRepository.setExpenses(expenses)
    advanceUntilIdle()
    
    viewModel.applyFilters(categories = setOf(ExpenseCategory.FOOD))
    
    val filtered = viewModel.getFilteredExpenses()
    assertEquals(1, filtered.size)
    assertEquals(ExpenseCategory.FOOD, filtered.first().category)
}
```

---

### 6. CurrencyExchangeViewModel (50 tests)

**Purpose:** Displays exchange rates and converted expenses.

**Key Responsibilities:**
- Load exchange rates
- Display rate table
- Convert expenses
- Refresh rates
- Handle base currency changes

**Test Categories:**
- Initialization (6)
- Base Currency (5)
- Expense Conversion (10)
- Exchange Rate Display (12)
- Rate Refresh (8)
- Last Update (4)
- Error Handling (5)

**Example Test:**
```kotlin
@Test
fun `refreshExchangeRates calls repository and reloads rates`() = runTest {
    fakeExchangeRateRepo.mockRates = mapOf(
        Currency.EUR to 0.85,
        Currency.GBP to 0.73
    )
    
    viewModel.refreshExchangeRates()
    advanceUntilIdle()
    
    assertTrue(fakeExchangeRateRepo.refreshCalled)
    assertEquals(2, viewModel.exchangeRates.value.size)
}
```

---

### 7. ExpenseRepository (52 tests) ⭐ CRITICAL

**Purpose:** Manages expense data persistence and queries.

**Key Responsibilities:**
- CRUD operations
- Category filtering
- Date range queries
- Amount range queries
- Seed data management

**Test Categories:**
- Singleton Pattern (3)
- CRUD Operations (15)
- Category Queries (5)
- Date Range Queries (8)
- Amount Range Queries (6)
- Count Operations (3)
- Seed Data (8)
- Error Handling (4)

**Example Test:**
```kotlin
@Test
fun `insertExpense saves to DAO and emits via flow`() = runTest {
    val expense = createExpense(amount = 50.0)
    
    repository.getAllExpenses().test {
        // Initial empty state
        assertEquals(emptyList(), awaitItem())
        
        // Insert expense
        repository.insertExpense(expense)
        
        // Flow should emit updated list
        val emitted = awaitItem()
        assertEquals(1, emitted.size)
        assertEquals(50.0, emitted.first().amount)
        
        cancelAndIgnoreRemainingEvents()
    }
}
```

---

### 8. SettingsRepository (53 tests)

**Purpose:** Manages settings persistence.

**Key Responsibilities:**
- Base currency storage
- API credentials
- Theme preferences
- Voice settings
- Last update timestamp

**Test Categories:**
- Singleton Pattern (3)
- Base Currency (8)
- Settings CRUD (10)
- API Configuration (10)
- Exchange Rate Timestamp (6)
- Theme Settings (6)
- Voice Input Settings (6)
- Error Handling (4)

---

### 9. ExchangeRateRepository (69 tests)

**Purpose:** Manages exchange rate fetching and caching.

**Key Responsibilities:**
- API integration
- Rate caching
- Cross-rate calculation
- Staleness detection
- Cache cleanup

**Test Categories:**
- Singleton Pattern (3)
- Direct Rate Lookup (10)
- Reverse Rate Calculation (5)
- Cross-Rate Calculation (12)
- Rate Refresh (15)
- Staleness Checks (8)
- Cache Management (6)
- Bulk Operations (5)
- Error Handling (5)

**Example Test:**
```kotlin
@Test
fun `cross-rate calculation via user base currency`() = runTest {
    // User base: USD
    // Want: EUR -> GBP
    // Have: USD -> EUR (0.85), USD -> GBP (0.73)
    
    fakeSettingsRepo.setBaseCurrency(Currency.USD)
    fakeDao.addRate("USD", "EUR", 0.85)
    fakeDao.addRate("USD", "GBP", 0.73)
    
    val rate = repository.getExchangeRateSync(Currency.EUR, Currency.GBP)
    
    // GBP/EUR = 0.73/0.85 ≈ 0.858
    assertNotNull(rate)
    assertEquals(0.858, rate!!, 0.01)
}
```

---

## 🛠️ Testing Tools & Patterns

### Required Dependencies

```kotlin
// build.gradle.kts
commonTest.dependencies {
    implementation(libs.kotlin.test)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    implementation("app.cash.turbine:turbine:1.1.0")
}
```

### Key Testing Patterns

#### 1. Test Structure (AAA Pattern)

```kotlin
@Test
fun `descriptive test name`() = runTest {
    // Arrange
    val expense = createExpense(amount = 50.0)
    
    // Act
    viewModel.process(expense)
    advanceUntilIdle()
    
    // Assert
    assertEquals(expected, viewModel.state.value)
}
```

#### 2. Flow Testing with Turbine

```kotlin
@Test
fun `flow emits correct values`() = runTest {
    repository.getAllExpenses().test {
        assertEquals(emptyList(), awaitItem())
        
        repository.insertExpense(expense)
        
        val updated = awaitItem()
        assertEquals(1, updated.size)
        
        cancelAndIgnoreRemainingEvents()
    }
}
```

#### 3. Async Operations

```kotlin
@Test
fun `async operation completes`() = runTest {
    viewModel.doAsyncThing()
    advanceUntilIdle() // Wait for all coroutines
    
    assertTrue(viewModel.isComplete.value)
}
```

#### 4. Error Handling

```kotlin
@Test
fun `repository error handled gracefully`() = runTest {
    fakeRepository.shouldThrowError = true
    
    viewModel.loadData()
    advanceUntilIdle()
    
    assertNotNull(viewModel.errorMessage.value)
    assertFalse(viewModel.isLoading.value)
}
```

---

## 📈 Progress Tracking

### Implementation Phases

#### Phase 1: Foundation (Week 1)
- [ ] Setup test dependencies
- [ ] Create fake implementations
- [ ] Write test utilities
- [ ] Implement AddExpenseViewModel tests (44)
- [ ] Implement ExpenseRepository tests (52)
- **Target:** 96 tests passing

#### Phase 2: Core Features (Week 2)
- [ ] Implement ExpenseHistoryViewModel tests (59)
- [ ] Implement DashBoardViewModel tests (31)
- [ ] Implement SettingsRepository tests (53)
- **Target:** 239 tests total

#### Phase 3: Advanced (Week 3)
- [ ] Implement ExchangeRateRepository tests (69)
- [ ] Implement SettingsViewModel tests (55)
- [ ] Implement CurrencyExchangeViewModel tests (50)
- [ ] Implement VoiceInputViewModel tests (35)
- **Target:** 448 tests total

#### Phase 4: Polish (Week 4)
- [ ] Fix all failing tests
- [ ] Achieve 80%+ coverage
- [ ] Setup CI/CD
- [ ] Documentation

### Coverage Goals

| Component | Target | Actual | Status |
|-----------|--------|--------|--------|
| AddExpenseViewModel | 85% | - | 📋 Pending |
| ExpenseHistoryViewModel | 85% | - | 📋 Pending |
| DashBoardViewModel | 80% | - | 📋 Pending |
| SettingsViewModel | 85% | - | 📋 Pending |
| CurrencyExchangeViewModel | 80% | - | 📋 Pending |
| VoiceInputViewModel | 75% | - | 📋 Pending |
| ExpenseRepository | 90% | - | 📋 Pending |
| SettingsRepository | 90% | - | 📋 Pending |
| ExchangeRateRepository | 85% | - | 📋 Pending |
| **Overall** | **80%** | - | 📋 Pending |

---

## 🎓 Learning Resources

### Official Documentation
- [Kotlin Test](https://kotlinlang.org/api/latest/kotlin.test/)
- [Coroutines Test](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/)
- [Turbine](https://github.com/cashapp/turbine)

### Best Practices
- [Testing on Android](https://developer.android.com/training/testing)
- [ViewModel Testing](https://developer.android.com/codelabs/android-testing)
- [Repository Testing](https://developer.android.com/training/data-storage/room/testing-db)

### Internal Documentation
- [Database Testing](../database/IMPLEMENTATION.md#testing)
- [Voice Feature Testing](../features/VOICE_TO_EXPENSE_DECISION_GUIDE.md#testing-strategy)

---

## ❓ FAQ

### Q: Why unit tests instead of instrumented tests?
**A:** Unit tests are faster, don't require emulator, and test business logic in isolation. We use instrumented tests for UI and integration testing.

### Q: How do I test ViewModels with singleton repositories?
**A:** Either:
1. Add dependency injection to ViewModels
2. Reset singleton instances in @AfterTest
3. Use fake implementations that don't affect real instances

### Q: What about testing platform-specific code?
**A:** Platform-specific code (expect/actual) should be tested with platform-specific tests (androidTest for Android, iosTest for iOS).

### Q: How do I test Flows and StateFlows?
**A:** Use Turbine library for comprehensive Flow testing, or collect values in tests with proper coroutine handling.

### Q: What's the difference between Fake and Mock?
**A:** 
- **Fake:** Working implementation with simplified logic (e.g., in-memory list instead of database)
- **Mock:** Stub that records calls and returns predefined values (use Mockk library if needed)

We prefer Fakes for better test reliability.

---

## 🚦 Getting Started

### For Developers New to Testing
1. Read [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md)
2. Follow Step 1-5 to setup environment
3. Copy and run the AddExpenseViewModel example
4. Gradually add more tests

### For Experienced Testers
1. Review [UNIT_TEST_PLAN.md](./UNIT_TEST_PLAN.md) for comprehensive specs
2. Check [VIEWMODEL_TEST_SPECIFICATIONS.md](./VIEWMODEL_TEST_SPECIFICATIONS.md) for examples
3. Implement tests following priority order
4. Ensure coverage goals are met

### For Project Leads
- Estimated effort: 70 hours
- Can be parallelized: Multiple ViewModels tested simultaneously
- Critical path: AddExpenseViewModel → ExpenseRepository
- Success criteria: 448 tests passing, 80%+ coverage

---

## 📝 Contributing

### Adding New Tests
1. Follow existing patterns
2. Use AAA structure (Arrange, Act, Assert)
3. Write descriptive test names
4. Group related tests
5. Add comments for complex logic

### Test Naming Convention
```kotlin
@Test
fun `component does action when condition`() {
    // Test implementation
}
```

Examples:
- ✅ `viewModel saves expense when validation passes`
- ✅ `repository emits updated list after insert`
- ❌ `test1()` (too vague)
- ❌ `testSaveExpense()` (missing context)

---

## 🎯 Success Metrics

### Quantitative
- ✅ 448/448 tests passing
- ✅ 80%+ code coverage
- ✅ < 5 minute test execution time
- ✅ Zero flaky tests

### Qualitative
- ✅ Tests are maintainable
- ✅ Tests are readable
- ✅ Tests catch real bugs
- ✅ Team confident in changes

---

## 📞 Support

### Questions or Issues?
- Check this documentation first
- Review existing test examples
- Ask team members
- Update documentation with learnings

### Documentation Updates
This is a living document. Please update it when:
- Adding new test patterns
- Discovering better approaches
- Encountering common issues
- Achieving milestones

---

## 🎉 Conclusion

This comprehensive unit testing plan provides:

✅ **448 detailed test specifications**  
✅ **Complete implementation examples**  
✅ **Reusable fake implementations**  
✅ **Step-by-step setup guide**  
✅ **Best practices and patterns**  
✅ **Progress tracking framework**  

**Next Step:** Open [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md) and start with Step 1!

---

**Last Updated:** 2024-11-19  
**Version:** 1.0  
**Status:** ✅ Ready for Implementation

