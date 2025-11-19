# Unit Testing Quick Reference Card

One-page reference for ExpenseTracker unit testing.

---

## 📊 At a Glance

```
┌─────────────────────────────────────────────────────────────┐
│                  EXPENSETRACKER UNIT TESTS                  │
├─────────────────────────────────────────────────────────────┤
│  Total Tests: 448                                           │
│  ViewModels:  274 tests (6 classes)                         │
│  Repositories: 174 tests (3 classes)                        │
│  Coverage Goal: 80%+                                        │
│  Effort: ~70 hours (2 weeks)                                │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎯 Test Distribution

### ViewModels (274 tests)
```
VoiceInputViewModel        ████████░░ 35 tests  (Low Priority)
AddExpenseViewModel        █████████░ 44 tests  (🔴 CRITICAL)
DashBoardViewModel         ██████░░░░ 31 tests  (High Priority)
SettingsViewModel          ██████████ 55 tests  (Med Priority)
ExpenseHistoryViewModel    ██████████ 59 tests  (High Priority)
CurrencyExchangeViewModel  ██████████ 50 tests  (Med Priority)
```

### Repositories (174 tests)
```
ExpenseRepository          ██████████ 52 tests  (🔴 CRITICAL)
SettingsRepository         ██████████ 53 tests  (High Priority)
ExchangeRateRepository     ██████████ 69 tests  (Med Priority)
```

---

## 🚀 Quick Start Commands

```bash
# 1. Setup
./gradlew clean build

# 2. Run all tests
./gradlew :composeApp:testDebugUnitTest

# 3. Run specific test
./gradlew :composeApp:testDebugUnitTest \
  --tests "*.AddExpenseViewModelTest"

# 4. With coverage
./gradlew :composeApp:testDebugUnitTest jacocoTestReport

# 5. View coverage report
open composeApp/build/reports/jacoco/test/html/index.html
```

---

## 📦 Dependencies (add to build.gradle.kts)

```kotlin
commonTest.dependencies {
    implementation(libs.kotlin.test)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    implementation("app.cash.turbine:turbine:1.1.0")
}
```

---

## 🧪 Test Template

```kotlin
class MyViewModelTest {
    private lateinit var viewModel: MyViewModel
    private lateinit var fakeRepository: FakeRepository
    
    @BeforeTest
    fun setup() {
        fakeRepository = FakeRepository()
        viewModel = MyViewModel(fakeRepository)
    }
    
    @AfterTest
    fun tearDown() {
        fakeRepository.reset()
    }
    
    @Test
    fun `descriptive test name`() = runTest {
        // Arrange
        val input = createTestData()
        
        // Act
        viewModel.doSomething(input)
        advanceUntilIdle()
        
        // Assert
        assertEquals(expected, viewModel.state.value)
    }
}
```

---

## 🔧 Common Test Patterns

### 1. Testing State Updates
```kotlin
@Test
fun `state updates correctly`() {
    viewModel.updateSomething("new value")
    assertEquals("new value", viewModel.something.value)
}
```

### 2. Testing Async Operations
```kotlin
@Test
fun `async operation completes`() = runTest {
    viewModel.loadData()
    advanceUntilIdle()
    assertFalse(viewModel.isLoading.value)
}
```

### 3. Testing Flows
```kotlin
@Test
fun `flow emits correct values`() = runTest {
    repository.getData().test {
        assertEquals(initial, awaitItem())
        repository.update(new)
        assertEquals(new, awaitItem())
        cancelAndIgnoreRemainingEvents()
    }
}
```

### 4. Testing Validation
```kotlin
@Test
fun `invalid input shows error`() {
    viewModel.amount = "" // Invalid
    viewModel.save()
    assertNotNull(viewModel.errorMessage)
}
```

### 5. Testing Error Handling
```kotlin
@Test
fun `repository error handled`() = runTest {
    fakeRepository.shouldThrowError = true
    viewModel.loadData()
    advanceUntilIdle()
    assertNotNull(viewModel.errorMessage.value)
}
```

---

## 📋 Implementation Checklist

### Phase 1: Foundation (Week 1)
- [ ] Add test dependencies
- [ ] Create test directory structure
- [ ] Implement FakeExpenseRepository
- [ ] Implement FakeSettingsRepository
- [ ] Create TestData helpers
- [ ] AddExpenseViewModel tests (44)
- [ ] ExpenseRepository tests (52)

### Phase 2: Core (Week 2)
- [ ] FakeExchangeRateRepository
- [ ] ExpenseHistoryViewModel tests (59)
- [ ] DashBoardViewModel tests (31)
- [ ] SettingsRepository tests (53)

### Phase 3: Advanced (Week 3)
- [ ] FakeMicrophoneService
- [ ] FakeExchangeRateApiService
- [ ] ExchangeRateRepository tests (69)
- [ ] SettingsViewModel tests (55)
- [ ] CurrencyExchangeViewModel tests (50)
- [ ] VoiceInputViewModel tests (35)

### Phase 4: Polish (Week 4)
- [ ] Fix all failing tests
- [ ] Achieve 80%+ coverage
- [ ] Setup CI/CD
- [ ] Documentation updates

---

## 🎯 Priority Matrix

```
            High Value
               ↑
    ┌──────────┼──────────┐
    │   🔴     │    🟡    │
    │ AddVM    │ HistoryVM│
    │ ExpRepo  │ DashVM   │
High├──────────┼──────────┤Low
Need│   🟢     │    ⚪    │Need
    │ SettVM   │ VoiceVM  │
    │ RatesVM  │          │
    └──────────┼──────────┘
               ↓
            Low Value
```

**🔴 Do First** → **🟡 Do Second** → **🟢 Do Third** → **⚪ Do Last**

---

## 📊 Test Coverage Targets

| Component | Target | Reasoning |
|-----------|--------|-----------|
| AddExpenseViewModel | 85% | Critical user path |
| ExpenseHistoryViewModel | 85% | Core functionality |
| DashBoardViewModel | 80% | Analytics/display |
| SettingsViewModel | 85% | Configuration |
| CurrencyExchangeViewModel | 80% | Display only |
| VoiceInputViewModel | 75% | Experimental |
| ExpenseRepository | 90% | Data integrity |
| SettingsRepository | 90% | Configuration |
| ExchangeRateRepository | 85% | Complex logic |
| **Overall** | **80%** | Industry standard |

---

## 🐛 Common Issues & Solutions

### Issue: "Cannot find symbol: runTest"
```kotlin
// Solution: Add coroutines-test
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
```

### Issue: "Flow not emitting"
```kotlin
// Solution: Use advanceUntilIdle()
@Test
fun test() = runTest {
    viewModel.doSomething()
    advanceUntilIdle() // ← Add this
    assertEquals(expected, viewModel.state.value)
}
```

### Issue: "Singleton dependencies"
```kotlin
// Solution: Reset in tearDown
@AfterTest
fun tearDown() {
    ExpenseRepository.resetInstance()
    SettingsRepository.resetInstance()
}
```

### Issue: "Tests are flaky"
```kotlin
// Solution: Proper test dispatcher
@Test
fun test() = runTest {
    val testDispatcher = StandardTestDispatcher(testScheduler)
    Dispatchers.setMain(testDispatcher)
    // Test code
    Dispatchers.resetMain()
}
```

---

## 📚 Documentation Links

| Document | Use Case |
|----------|----------|
| [README.md](./README.md) | Overview & getting started |
| [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md) | Step-by-step setup |
| [UNIT_TEST_PLAN.md](./UNIT_TEST_PLAN.md) | Detailed specifications |
| [VIEWMODEL_TEST_SPECIFICATIONS.md](./VIEWMODEL_TEST_SPECIFICATIONS.md) | Code examples |

---

## ✅ Definition of Done

### Test Implementation
- [ ] All 448 tests implemented
- [ ] All tests passing
- [ ] No flaky tests
- [ ] Tests run in < 5 minutes

### Code Quality
- [ ] 80%+ coverage achieved
- [ ] Fakes are reusable
- [ ] Tests are maintainable
- [ ] Documentation complete

### Integration
- [ ] CI/CD pipeline setup
- [ ] Coverage reports automated
- [ ] Team training complete
- [ ] Best practices documented

---

## 🎓 Key Testing Principles

### 1. AAA Pattern
```kotlin
// Arrange - Setup test data
// Act     - Execute code under test
// Assert  - Verify results
```

### 2. Test Independence
- Each test runs in isolation
- No shared state between tests
- Use @BeforeTest and @AfterTest

### 3. Clear Test Names
```kotlin
✅ `saves expense when validation passes`
❌ `test1()`
```

### 4. Single Responsibility
- One test = One concept
- If you use "and" in test name, split it

### 5. Fast Execution
- Unit tests should be fast
- Mock external dependencies
- Avoid Thread.sleep()

---

## 💡 Pro Tips

### Tip 1: Use Test Data Builders
```kotlin
fun createExpense(
    amount: Double = 50.0,
    category: ExpenseCategory = ExpenseCategory.FOOD
) = Expense(...)
```

### Tip 2: Verify Side Effects
```kotlin
assertTrue(repository.insertCalled)
assertEquals(1, repository.savedExpenses.size)
```

### Tip 3: Test Edge Cases
```kotlin
// Empty input
// Maximum values
// Null values
// Concurrent operations
```

### Tip 4: Use Descriptive Assertions
```kotlin
assertEquals(
    expected = 50.0,
    actual = expense.amount,
    message = "Expense amount should match input"
)
```

### Tip 5: Group Related Tests
```kotlin
// Validation Tests
@Test fun `empty amount shows error`()
@Test fun `invalid amount shows error`()
@Test fun `null category shows error`()
```

---

## 📈 Progress Tracking

### Week 1 Goals
```
[====------] 40% Complete
✅ Setup dependencies
✅ Create fakes
🟡 AddExpenseViewModel (44 tests)
🟡 ExpenseRepository (52 tests)
```

### Week 2 Goals
```
[====------] 40% Complete
🟡 ExpenseHistoryViewModel (59 tests)
🟡 DashBoardViewModel (31 tests)
🟡 SettingsRepository (53 tests)
```

### Week 3 Goals
```
[====------] 40% Complete
🟡 ExchangeRateRepository (69 tests)
🟡 SettingsViewModel (55 tests)
🟡 CurrencyExchangeViewModel (50 tests)
🟡 VoiceInputViewModel (35 tests)
```

### Week 4 Goals
```
[====------] 40% Complete
🟡 Fix failing tests
🟡 Achieve 80%+ coverage
🟡 CI/CD integration
🟡 Documentation
```

---

## 🎯 Success Metrics

### Quantitative
- ✅ 448/448 tests passing
- ✅ 80%+ code coverage
- ✅ < 5 min test execution
- ✅ Zero flaky tests

### Qualitative
- ✅ Team confidence in changes
- ✅ Bugs caught early
- ✅ Maintainable test suite
- ✅ Clear documentation

---

## 🚦 Next Steps

1. ✅ Read [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md)
2. ✅ Setup test dependencies
3. ✅ Create first test file
4. ✅ Run and verify
5. ✅ Continue with priority order
6. ✅ Track progress daily
7. ✅ Celebrate milestones! 🎉

---

**Quick Access:**
- 📖 [Full Documentation](./README.md)
- 🚀 [Setup Guide](./IMPLEMENTATION_GUIDE.md)
- 📋 [Detailed Plan](./UNIT_TEST_PLAN.md)
- 💻 [Code Examples](./VIEWMODEL_TEST_SPECIFICATIONS.md)

---

**Last Updated:** 2024-11-19  
**Print this page for quick reference while coding!**

