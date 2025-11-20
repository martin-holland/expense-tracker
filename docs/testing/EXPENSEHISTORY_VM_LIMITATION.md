# ExpenseHistoryViewModel - Unit Testing Limitation

## Issue

**ExpenseHistoryViewModel cannot be unit tested** due to architectural dependencies.

### Root Cause

The ViewModel constructor accepts `CurrencyConverter` which is a singleton:

```kotlin
class ExpenseHistoryViewModel(
    private val repository: IExpenseRepository = ExpenseRepository.getInstance(),
    private val currencyConverter: CurrencyConverter = CurrencyConverter.getInstance(),  // ❌ Problem
    private val settingsRepository: ISettingsRepository = SettingsRepository.getInstance()
) : ViewModel()
```

When `CurrencyConverter.getInstance()` is called, it tries to initialize:
1. `ExchangeRateRepository.getInstance()`
2. Which tries to access Android Room database
3. Which requires Android context
4. **Which doesn't exist in unit tests**

### Error

```
kotlin.UninitializedPropertyAccessException: lateinit property context has not been initialized
	at com.example.expensetracker.data.database.AndroidDatabaseContext.getContext(DatabaseBuilder.android.kt:12)
	at com.example.expensetracker.data.repository.ExchangeRateRepository$Companion.getInstance(ExchangeRateRepository.kt:57)
	at com.example.expensetracker.domain.CurrencyConverter$Companion.getInstance(CurrencyConverter.kt:47)
	at com.example.expensetracker.viewmodel.ExpenseHistoryViewModel.<init>(ExpenseHistoryViewModel.kt:31)
```

## Impact

- **38 tests prepared** covering initialization, loading, filtering, CRUD, conversion, edge cases
- **0 tests can run** due to initialization failure
- All tests fail immediately when creating the ViewModel

## Solutions

### Option A: Architectural Refactoring (Recommended)

Create `ICurrencyConverter` interface and inject it:

```kotlin
interface ICurrencyConverter {
    suspend fun convertAmountSync(
        amount: Double,
        fromCurrency: Currency,
        toCurrency: Currency,
        date: LocalDateTime? = null
    ): Double
}

class ExpenseHistoryViewModel(
    private val repository: IExpenseRepository = ExpenseRepository.getInstance(),
    private val currencyConverter: ICurrencyConverter = CurrencyConverter.getInstance(),  // ✅ Injectable
    private val settingsRepository: ISettingsRepository = SettingsRepository.getInstance()
) : ViewModel()
```

Then create `FakeCurrencyConverter : ICurrencyConverter` for testing.

**Pros:**
- Makes the ViewModel fully testable
- Improves architecture with dependency injection
- Consistent with other repository interfaces

**Cons:**
- Requires modifying production code
- Need to update `CurrencyConverter` to implement interface
- More setup work

### Option B: Integration Tests

Move ExpenseHistoryViewModel tests to integration tests with Android context.

**Pros:**
- Tests the actual conversion logic
- No production code changes needed

**Cons:**
- Slower test execution
- More complex setup
- Requires test database

### Option C: Skip/Document (Current Approach)

Document the limitation and skip unit testing for this ViewModel.

**Pros:**
- No code changes needed
- Quick to implement

**Cons:**
- ViewModel remains untested
- Reduced confidence in changes

## Current Status

Tests have been **prepared but not included** in the test suite due to this architectural limitation.

File: `ExpenseHistoryViewModelTest.kt` exists with 38 tests but is currently excluded from execution.

## Recommendation

**Implement Option A** - Refactor CurrencyConverter to use dependency injection.

This would:
1. Make ExpenseHistoryViewModel testable
2. Improve overall architecture
3. Be consistent with existing DI pattern (IExpenseRepository, ISettingsRepository, etc.)
4. Allow testing of currency conversion logic in isolation

##Similar ViewModels

- **CurrencyExchangeViewModel** - Also depends on CurrencyConverter, will have same issue
- **VoiceInputViewModel** - Has platform dependency on getMicrophoneService()

All three ViewModels require architectural changes to be unit testable.

