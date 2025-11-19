# Unit Testing Plan - ExpenseTracker

## Table of Contents
1. [Executive Summary](#executive-summary)
2. [Testing Strategy](#testing-strategy)
3. [ViewModels Testing Plan](#viewmodels-testing-plan)
4. [Repositories Testing Plan](#repositories-testing-plan)
5. [Test Dependencies & Setup](#test-dependencies--setup)
6. [Mock Strategies](#mock-strategies)
7. [Test Coverage Goals](#test-coverage-goals)

---

## Executive Summary

### Current State
- **Total ViewModels:** 6
- **Total Repositories:** 3
- **Current Test Coverage:** Minimal (only example test exists)
- **Testing Framework:** Kotlin Test (already configured)

### Identified Components

#### ViewModels (6)
1. `VoiceInputViewModel` - Voice recognition and parsing
2. `AddExpenseViewModel` - Expense creation form
3. `DashBoardViewModel` - Analytics and aggregates
4. `SettingsViewModel` - App settings management
5. `ExpenseHistoryViewModel` - Expense list with filtering
6. `CurrencyExchangeViewModel` - Currency conversion display

#### Repositories (3)
1. `SettingsRepository` - Settings persistence
2. `ExchangeRateRepository` - Exchange rate management
3. `ExpenseRepository` - Expense CRUD operations

---

## Testing Strategy

### Testing Approach
- **Unit Tests Only:** Focus on logic, state management, and data transformations
- **Mocking Strategy:** Mock all external dependencies (DAOs, API services, platform services)
- **Coroutine Testing:** Use `kotlinx-coroutines-test` for testing suspend functions and flows
- **Isolation:** Each test should be independent and not rely on real database or network

### Test Categories

1. **State Management Tests**
   - Verify initial state
   - Test state transitions
   - Validate state updates

2. **Business Logic Tests**
   - Input validation
   - Data transformation
   - Calculation accuracy

3. **Flow/LiveData Tests**
   - Flow emissions
   - State flow updates
   - Flow transformations

4. **Error Handling Tests**
   - Exception handling
   - Error state management
   - Recovery scenarios

---

## ViewModels Testing Plan

### 1. VoiceInputViewModel

**File:** `VoiceInputViewModel.kt`

**Dependencies to Mock:**
- `MicrophoneService` (platform-specific)
- `ExpenseParser` (service)

**Test Cases:**

#### State Management (15 tests)
- ✅ Initial state is Idle
- ✅ `toggleVoiceSection()` toggles visibility
- ✅ `startSpeechRecognition()` sets state to Listening
- ✅ `stopSpeechRecognition()` sets state to Processing
- ✅ `onSpeechResult(Success)` updates state correctly
- ✅ `onSpeechResult(Error)` updates error state
- ✅ `onPartialTranscription()` updates partial text
- ✅ `resetSpeechRecognition()` clears all state
- ✅ `cancelVoiceInput()` resets to clean state
- ✅ `recognizerReadyForCleanup` flag works correctly
- ✅ `acknowledgeCleanup()` clears cleanup flag

#### Audio Recording (5 tests)
- ✅ `startRecording()` updates isRecording state
- ✅ `stopRecording()` captures audio data
- ✅ `playAudio()` works with valid audio data
- ✅ `clearError()` clears error message
- ✅ Recording failure sets error message

#### Parsing (8 tests)
- ✅ `parseTranscription()` extracts expense data
- ✅ Parsed data includes amount, currency, category
- ✅ `clearParsedData()` clears parsed state
- ✅ Auto-parsing triggers on speech success
- ✅ `updateParsedField()` updates individual fields
- ✅ `updateParsedField()` preserves other fields
- ✅ Parsing handles empty/invalid input
- ✅ Completeness percentage calculated correctly

#### Manual Entry (7 tests)
- ✅ `enableManualEntry()` shows manual entry UI
- ✅ `onManualEntryTextChanged()` updates text
- ✅ `parseManualEntry()` processes manual text
- ✅ `parseManualEntry()` hides entry after parsing
- ✅ `parseManualEntry()` ignores blank text
- ✅ `cancelManualEntry()` clears and hides entry
- ✅ Manual entry state independent from voice state

**Total: 35 tests**

**Complexity:** High (many state interactions)

---

### 2. AddExpenseViewModel

**File:** `AddExpenseViewModel.kt`

**Dependencies to Mock:**
- `ExpenseRepository`

**Test Cases:**

#### Initialization (3 tests)
- ✅ Initial state has empty form fields
- ✅ Date is set to today on init
- ✅ Default currency is USD

#### Form Field Updates (8 tests)
- ✅ `onCurrencySelected()` updates currency
- ✅ `onAmountChanged()` accepts valid decimal
- ✅ `onAmountChanged()` rejects invalid input
- ✅ `onCategorySelected()` updates category
- ✅ `onNoteChanged()` updates note
- ✅ `onDateSelected()` updates date
- ✅ Field updates clear error messages
- ✅ Amount validation regex works correctly

#### Date Management (5 tests)
- ✅ `parseDate()` handles valid date strings
- ✅ `parseDate()` returns null for invalid dates
- ✅ Date format is "Month Day, Year"
- ✅ All month names parse correctly
- ✅ `updateCurrentDate()` sets today's date

#### Validation (8 tests)
- ✅ Empty amount shows error
- ✅ Invalid amount (just ".") shows error
- ✅ Null category shows error
- ✅ Invalid date shows error
- ✅ Valid input passes validation
- ✅ Error messages are descriptive
- ✅ Validation runs before save
- ✅ Multiple validation errors handled

#### Save Operation (10 tests)
- ✅ `saveExpense()` validates before saving
- ✅ Valid expense saves to repository
- ✅ Successful save shows success snackbar
- ✅ Successful save clears form
- ✅ Save failure shows error snackbar
- ✅ `isSaving` flag updates correctly
- ✅ Generated expense ID is unique
- ✅ Note defaults to "No description" if empty
- ✅ Currency preserved after save
- ✅ Repository error handled gracefully

#### Form Management (5 tests)
- ✅ `resetForm()` clears all fields
- ✅ `resetForm()` sets default category
- ✅ `dismissSnackbar()` clears message
- ✅ `clearForm()` (private) clears correctly
- ✅ Form state persists category selection

#### Voice Integration (5 tests)
- ✅ `populateFromParsedData()` fills amount
- ✅ `populateFromParsedData()` fills currency
- ✅ `populateFromParsedData()` fills category
- ✅ `populateFromParsedData()` fills description
- ✅ Null values in parsed data are ignored

**Total: 44 tests**

**Complexity:** Medium-High (validation logic)

---

### 3. DashBoardViewModel

**File:** `DashBoardViewModel.kt`

**Dependencies to Mock:**
- `ExpenseRepository`
- `ExpenseAggregator` (domain logic)

**Test Cases:**

#### Initialization (3 tests)
- ✅ Initial state shows loading
- ✅ `loadExpenses()` called on init
- ✅ Repository flow observed automatically

#### Data Loading (8 tests)
- ✅ Empty expense list shows empty state
- ✅ Expenses update UI state
- ✅ Loading flag cleared after load
- ✅ Error state updated on failure
- ✅ Flow collection continues after error
- ✅ Multiple updates handled correctly
- ✅ State transitions properly
- ✅ isLoading becomes false when done

#### Aggregation (15 tests)
- ✅ `updateAggregates()` processes expenses
- ✅ Current month aggregate calculated
- ✅ Previous month aggregate calculated
- ✅ Month-over-month change computed
- ✅ Category totals aggregated
- ✅ Weekly aggregates generated
- ✅ Daily aggregates generated
- ✅ Empty list handled gracefully
- ✅ Single expense aggregates correctly
- ✅ Multiple categories aggregated
- ✅ Date ranges handled correctly
- ✅ `YearMonth.minusMonths()` works
- ✅ Expense to Transaction conversion works
- ✅ Aggregate sums are accurate
- ✅ Time zone handling in date calculations

#### UI State (5 tests)
- ✅ UI state reflects all aggregates
- ✅ Current month displayed correctly
- ✅ Error message propagated to UI
- ✅ State updates are immutable
- ✅ All state fields populated correctly

**Total: 31 tests**

**Complexity:** Medium (aggregation logic)

---

### 4. SettingsViewModel

**File:** `SettingsViewModel.kt`

**Dependencies to Mock:**
- `SettingsRepository`
- `ExchangeRateRepository`
- `MicrophoneService`

**Test Cases:**

#### Initialization (5 tests)
- ✅ Initial state has default values
- ✅ `loadSettings()` called on init
- ✅ Settings flow observed
- ✅ All flows start observing
- ✅ Default currency is USD

#### Settings Loading (10 tests)
- ✅ `loadSettings()` fetches from repository
- ✅ Base currency loaded correctly
- ✅ API key loaded correctly
- ✅ API base URL loaded correctly
- ✅ Theme option loaded correctly
- ✅ Voice input setting loaded correctly
- ✅ Last update timestamp formatted
- ✅ Loading flag set during fetch
- ✅ Error handled gracefully
- ✅ Loading flag cleared after fetch

#### Currency Management (8 tests)
- ✅ `updateBaseCurrency()` updates repository
- ✅ `setCurrency()` alias works
- ✅ Same currency ignored
- ✅ State updated optimistically
- ✅ Rollback on repository error
- ✅ Error message set on failure
- ✅ Available currencies list complete
- ✅ Currency changes propagate

#### API Configuration (12 tests)
- ✅ `updateApiKey()` saves to repository
- ✅ `updateApiBaseUrl()` saves to repository
- ✅ `isApiConfigured` reflects key status
- ✅ `testApiConnection()` validates setup
- ✅ Test shows success message
- ✅ Test shows error message
- ✅ Test requires API key
- ✅ Test fetches rates on success
- ✅ Loading flag during test
- ✅ `refreshExchangeRates()` calls repository
- ✅ Refresh updates timestamp
- ✅ Refresh error handled

#### Theme Settings (5 tests)
- ✅ `setThemeOption()` updates repository
- ✅ Theme changes persist
- ✅ Rollback on error
- ✅ Default theme is SYSTEM
- ✅ All theme options supported

#### Voice Input Settings (7 tests)
- ✅ `checkMicrophonePermission()` queries service
- ✅ Permission state updated correctly
- ✅ `toggleVoiceInput()` with permission
- ✅ `toggleVoiceInput()` without permission
- ✅ Toggle requests permission when needed
- ✅ Voice setting saves to repository
- ✅ Rollback on repository error

#### Flow Observations (8 tests)
- ✅ Base currency flow observed
- ✅ API key flow observed
- ✅ API base URL flow observed
- ✅ Theme option flow observed
- ✅ Voice input flow observed
- ✅ Last update flow observed
- ✅ Flow errors caught and logged
- ✅ Multiple flow updates handled

**Total: 55 tests**

**Complexity:** High (many settings, flows, and integrations)

---

### 5. ExpenseHistoryViewModel

**File:** `ExpenseHistoryViewModel.kt`

**Dependencies to Mock:**
- `ExpenseRepository`
- `CurrencyConverter`
- `SettingsRepository`

**Test Cases:**

#### Initialization (4 tests)
- ✅ Initial state shows loading
- ✅ `loadExpenses()` called on init
- ✅ Base currency observed
- ✅ Show converted amounts enabled by default

#### Expense Loading (8 tests)
- ✅ Expenses loaded from repository
- ✅ Flow emissions update state
- ✅ Loading cleared after load
- ✅ Error state set on failure
- ✅ Empty list handled
- ✅ Large list handled
- ✅ State updates are immutable
- ✅ Expenses sorted by date

#### Currency Conversion (12 tests)
- ✅ `convertExpenses()` converts all expenses
- ✅ Same currency returns original amount
- ✅ Different currency uses converter
- ✅ Converted expenses include base currency
- ✅ Base currency change triggers reconversion
- ✅ Conversion preserves original expense
- ✅ Conversion errors handled
- ✅ `toggleShowConvertedAmounts()` works
- ✅ Conversion uses expense date
- ✅ Multiple currencies converted correctly
- ✅ Converted list emitted to flow
- ✅ Conversion synchronized with expense updates

#### Filtering (15 tests)
- ✅ `applyFilters()` sets category filter
- ✅ `applyFilters()` sets date range filter
- ✅ `applyFilters()` sets amount range filter
- ✅ `clearFilters()` removes all filters
- ✅ `getFilteredExpenses()` applies category filter
- ✅ `getFilteredExpenses()` applies date filter
- ✅ `getFilteredExpenses()` applies amount filter
- ✅ Multiple filters work together
- ✅ `showFilterDialog()` shows dialog
- ✅ `hideFilterDialog()` hides dialog
- ✅ Filtered expenses sorted correctly
- ✅ Empty filter set returns all
- ✅ `getFilteredExpensesWithConversion()` works
- ✅ Filtered conversions match filtered expenses
- ✅ Filter state persists until cleared

#### Expense Deletion (8 tests)
- ✅ `requestDeleteExpense()` marks for deletion
- ✅ Delete request shows dialog
- ✅ `confirmDeleteExpense()` deletes from repository
- ✅ Delete closes dialog
- ✅ Delete handles errors
- ✅ `cancelDeleteExpense()` clears selection
- ✅ Null expense handled gracefully
- ✅ UI updates after delete via flow

#### Expense Editing (7 tests)
- ✅ `openEditDialog()` sets expense to edit
- ✅ Edit dialog shown
- ✅ `closeEditDialog()` clears selection
- ✅ `saveExpense()` updates repository
- ✅ Save closes dialog
- ✅ Save error handled
- ✅ Edited expense updates via flow

#### UI State (5 tests)
- ✅ All UI state fields populated
- ✅ Dialog states managed correctly
- ✅ Error message displayed
- ✅ State immutability maintained
- ✅ Complex state updates work

**Total: 59 tests**

**Complexity:** High (filtering, conversion, CRUD)

---

### 6. CurrencyExchangeViewModel

**File:** `CurrencyExchangeViewModel.kt`

**Dependencies to Mock:**
- `CurrencyConverter`
- `SettingsRepository`
- `ExchangeRateRepository`
- `ExpenseRepository`

**Test Cases:**

#### Initialization (6 tests)
- ✅ Initial state has default values
- ✅ Base currency observed
- ✅ Expenses observed
- ✅ Last update observed
- ✅ Exchange rates loaded on init
- ✅ Initial conversion triggered

#### Base Currency (5 tests)
- ✅ Base currency flow updates state
- ✅ Currency change triggers conversion
- ✅ Currency change reloads rates
- ✅ Error handled on currency load
- ✅ Currency state synchronized

#### Expense Conversion (10 tests)
- ✅ `convertExpenses()` converts all
- ✅ Same currency returns original
- ✅ Different currency uses converter
- ✅ Conversion includes base currency
- ✅ Expense list updates trigger conversion
- ✅ Conversion uses expense date
- ✅ Conversion errors logged
- ✅ Empty list handled
- ✅ Null expense list handled
- ✅ Converted list emitted

#### Exchange Rate Display (12 tests)
- ✅ `loadExchangeRates()` fetches rates
- ✅ Rates for all supported currencies
- ✅ Base currency rate is 1.0
- ✅ Cross-rate calculation works
- ✅ `calculateRatesViaCrossRate()` accurate
- ✅ Missing rates handled gracefully
- ✅ Rate map populated correctly
- ✅ Error during load logged
- ✅ Stale rates shown if available
- ✅ No rates available handled
- ✅ Rate display updated on change
- ✅ Multiple rate updates handled

#### Rate Refresh (8 tests)
- ✅ `refreshExchangeRates()` calls repository
- ✅ Refresh success reloads rates
- ✅ Refresh failure shows error
- ✅ Loading flag during refresh
- ✅ Error message set on failure
- ✅ Last update timestamp updated
- ✅ Refresh with invalid API key
- ✅ Network error handled

#### Last Update (4 tests)
- ✅ Timestamp flow observed
- ✅ Timestamp formatted correctly
- ✅ `formatTimestamp()` pads minutes
- ✅ Null timestamp handled

#### Error Handling (5 tests)
- ✅ Repository errors caught
- ✅ Converter errors caught
- ✅ Error messages displayed
- ✅ Partial failures handled
- ✅ Error state doesn't block UI

**Total: 50 tests**

**Complexity:** High (multiple repositories, conversions)

---

## Repositories Testing Plan

### 1. SettingsRepository

**File:** `SettingsRepository.kt`

**Dependencies to Mock:**
- `SettingsDao`

**Test Cases:**

#### Singleton Pattern (3 tests)
- ✅ `getInstance()` returns same instance
- ✅ `resetInstance()` clears singleton
- ✅ Thread-safe initialization

#### Base Currency (8 tests)
- ✅ `getBaseCurrency()` returns flow
- ✅ `getBaseCurrencySync()` returns value
- ✅ `updateBaseCurrency()` saves to DAO
- ✅ `setBaseCurrency()` alias works
- ✅ Default is USD if not set
- ✅ Currency changes emit via flow
- ✅ Invalid currency code handled
- ✅ DAO errors propagated

#### Settings CRUD (10 tests)
- ✅ `getSettings()` returns flow
- ✅ `getSettingsSync()` returns snapshot
- ✅ `saveSettings()` persists all
- ✅ Default settings created if missing
- ✅ `initializeDefaultSettingsIfNeeded()` works
- ✅ Settings entity conversion works
- ✅ Null settings returns defaults
- ✅ Partial settings handled
- ✅ Settings updates atomic
- ✅ Multiple fields updated together

#### API Configuration (10 tests)
- ✅ `getApiKey()` returns flow
- ✅ `getApiKeySync()` returns value
- ✅ `updateApiKey()` saves to DAO
- ✅ `setApiKey()` alias works
- ✅ `getApiBaseUrl()` returns flow
- ✅ `getApiBaseUrlSync()` returns value
- ✅ `updateApiBaseUrl()` saves to DAO
- ✅ `setApiBaseUrl()` alias works
- ✅ `isApiConfigured()` checks key
- ✅ Default API URL provided

#### Exchange Rate Timestamp (6 tests)
- ✅ `getLastExchangeRateUpdate()` returns flow
- ✅ `updateLastExchangeRateUpdate()` with timestamp
- ✅ `updateLastExchangeRateUpdate()` with current time
- ✅ Timestamp formatted correctly
- ✅ Null timestamp handled
- ✅ Timestamp parsing errors caught

#### Theme Settings (6 tests)
- ✅ `getThemeOption()` returns flow
- ✅ `getThemeOptionSync()` returns value
- ✅ `updateThemeOption()` saves to DAO
- ✅ `setThemeOption()` alias works
- ✅ Default is SYSTEM
- ✅ All theme options supported

#### Voice Input Settings (6 tests)
- ✅ `getVoiceInputEnabled()` returns flow
- ✅ `getVoiceInputEnabledSync()` returns value
- ✅ `updateVoiceInputEnabled()` saves to DAO
- ✅ `setVoiceInputEnabled()` alias works
- ✅ Default is false
- ✅ Boolean state persists

#### Error Handling (4 tests)
- ✅ DAO exceptions caught
- ✅ Flow errors handled
- ✅ Suspend function errors handled
- ✅ Partial failures don't corrupt state

**Total: 53 tests**

**Complexity:** Medium (mostly CRUD with flows)

---

### 2. ExchangeRateRepository

**File:** `ExchangeRateRepository.kt`

**Dependencies to Mock:**
- `ExchangeRateDao`
- `SettingsRepository`
- `ExchangeRateApiService`

**Test Cases:**

#### Singleton Pattern (3 tests)
- ✅ `getInstance()` returns same instance
- ✅ `resetInstance()` clears singleton
- ✅ Cleanup called on init

#### Direct Rate Lookup (10 tests)
- ✅ `getExchangeRate()` returns flow
- ✅ `getExchangeRateSync()` returns value
- ✅ Same currency returns 1.0
- ✅ Direct rate found in DAO
- ✅ Historical rate with date
- ✅ Latest rate when no date
- ✅ Null returned if not found
- ✅ Stale rate still returned
- ✅ Stale rate logged warning
- ✅ Flow emits on rate change

#### Reverse Rate Calculation (5 tests)
- ✅ Reverse rate inverted correctly
- ✅ Fallback to latest if date missing
- ✅ Zero rate handled safely
- ✅ Reverse used when direct missing
- ✅ Stale reverse rate logged

#### Cross-Rate Calculation (12 tests)
- ✅ Cross-rate via user base currency
- ✅ Cross-rate via any available base
- ✅ Multiple strategies attempted
- ✅ First working strategy used
- ✅ Cross-rate formula correct (r2/r1)
- ✅ Historical cross-rate works
- ✅ Zero rate in cross-rate handled
- ✅ All base currencies tried
- ✅ Stale cross-rates logged
- ✅ Complex conversions work
- ✅ Currency enum lookup works
- ✅ Invalid currency code handled

#### Rate Refresh (15 tests)
- ✅ `refreshExchangeRates()` calls API
- ✅ API success stores all rates
- ✅ Timestamp updated on success
- ✅ Rate entities created correctly
- ✅ Entity ID generation works
- ✅ Batch insert to DAO
- ✅ API failure returns error
- ✅ Missing API key returns error
- ✅ Network error handled
- ✅ Settings updated after refresh
- ✅ Current date used for rates
- ✅ All currencies from API saved
- ✅ Duplicate rates overwritten
- ✅ Rate refresh idempotent
- ✅ API base URL used correctly

#### Staleness Checks (8 tests)
- ✅ `isRateStale()` detects old rates
- ✅ Fresh rates not stale (< 24h)
- ✅ Old rates stale (> 24h)
- ✅ Empty cache is stale
- ✅ Invalid timestamp is stale
- ✅ Partial cache handled
- ✅ Days/hours calculation correct
- ✅ Timezone handled correctly

#### Cache Management (6 tests)
- ✅ `clearOldRates()` deletes old entries
- ✅ 30-day threshold used
- ✅ Recent rates preserved
- ✅ Empty cache handled
- ✅ Date calculation correct
- ✅ DAO errors caught

#### Bulk Operations (5 tests)
- ✅ `getAllRatesForBase()` fetches all
- ✅ Date parameter works
- ✅ Latest rates if no date
- ✅ Map conversion correct
- ✅ Empty result handled

#### Error Handling (5 tests)
- ✅ DAO exceptions caught
- ✅ API exceptions caught
- ✅ Settings errors handled
- ✅ Invalid data handled
- ✅ Partial failures logged

**Total: 69 tests**

**Complexity:** Very High (complex rate calculation logic)

---

### 3. ExpenseRepository

**File:** `ExpenseRepository.kt`

**Dependencies to Mock:**
- `ExpenseDao`

**Test Cases:**

#### Singleton Pattern (3 tests)
- ✅ `getInstance()` returns same instance
- ✅ `resetInstance()` clears singleton
- ✅ Seed data called on init

#### CRUD Operations (15 tests)
- ✅ `getAllExpenses()` returns flow
- ✅ `getExpenseById()` returns expense
- ✅ `getExpenseById()` returns null if missing
- ✅ `insertExpense()` saves to DAO
- ✅ `insertExpenses()` batch saves
- ✅ `updateExpense()` modifies existing
- ✅ `deleteExpense()` removes from DAO
- ✅ `deleteExpenseById()` removes by ID
- ✅ Insert converts to entity
- ✅ Query converts from entity
- ✅ Entity conversion bidirectional
- ✅ DAO errors propagated
- ✅ Flow emits on changes
- ✅ Multiple operations work
- ✅ Concurrent operations safe

#### Category Queries (5 tests)
- ✅ `getExpensesByCategory()` filters correctly
- ✅ Multiple categories work
- ✅ Empty category returns empty
- ✅ Flow updates on changes
- ✅ Category name conversion works

#### Date Range Queries (8 tests)
- ✅ `getExpensesByDateRange()` filters correctly
- ✅ Start date inclusive
- ✅ End date inclusive
- ✅ Same date returns expenses
- ✅ Empty range returns empty
- ✅ Date string formatting correct
- ✅ Timezone handled properly
- ✅ Flow updates on changes

#### Amount Range Queries (6 tests)
- ✅ `getExpensesByAmountRange()` filters correctly
- ✅ Min amount inclusive
- ✅ Max amount inclusive
- ✅ Exact amount works
- ✅ Empty range returns empty
- ✅ Flow updates on changes

#### Count Operations (3 tests)
- ✅ `getExpenseCount()` returns total
- ✅ Empty database returns 0
- ✅ Count updates on changes

#### Seed Data (8 tests)
- ✅ `seedDatabaseIfEmpty()` checks count
- ✅ Seed data inserted if empty
- ✅ Seed skipped if data exists
- ✅ Seed data valid
- ✅ Seed categories diverse
- ✅ Seed currencies include EUR
- ✅ Seed dates realistic
- ✅ Seed amounts positive

#### Error Handling (4 tests)
- ✅ DAO exceptions caught
- ✅ Flow errors handled
- ✅ Invalid data rejected
- ✅ Concurrent access safe

**Total: 52 tests**

**Complexity:** Medium (standard CRUD with queries)

---

## Test Dependencies & Setup

### Required Dependencies

Add to `build.gradle.kts` in `commonTest` section:

```kotlin
commonTest.dependencies {
    implementation(libs.kotlin.test)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    implementation("app.cash.turbine:turbine:1.1.0") // For Flow testing
}
```

### Test Configuration

```kotlin
// In libs.versions.toml
[versions]
coroutines-test = "1.9.0"
turbine = "1.1.0"

[libraries]
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines-test" }
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }
```

---

## Mock Strategies

### 1. Repository Mocks

Create fake implementations that mimic real behavior:

```kotlin
class FakeExpenseRepository : ExpenseRepository {
    private val expenses = mutableListOf<Expense>()
    private val expensesFlow = MutableStateFlow<List<Expense>>(emptyList())
    
    override fun getAllExpenses(): Flow<List<Expense>> = expensesFlow
    
    override suspend fun insertExpense(expense: Expense) {
        expenses.add(expense)
        expensesFlow.value = expenses.toList()
    }
    
    // ... other methods
}
```

### 2. DAO Mocks

Use test fakes for database layer:

```kotlin
class FakeSettingsDao : SettingsDao {
    private var settings: SettingsEntity? = null
    private val settingsFlow = MutableStateFlow<SettingsEntity?>(null)
    
    override fun getSettings(): Flow<SettingsEntity?> = settingsFlow
    
    override suspend fun insertOrUpdateSettings(entity: SettingsEntity) {
        settings = entity
        settingsFlow.value = entity
    }
    
    // ... other methods
}
```

### 3. Service Mocks

Platform-specific services need test doubles:

```kotlin
class FakeMicrophoneService : MicrophoneService {
    var hasPermission = false
    var isRecording = false
    var recordedData: ByteArray? = null
    
    override fun hasMicrophonePermission() = hasPermission
    override fun startRecording() = true
    override fun stopRecording() = recordedData
    override fun playAudio(data: ByteArray) {}
    override fun requestMicrophonePermission() {}
}
```

### 4. API Service Mocks

Network calls need controlled responses:

```kotlin
class FakeExchangeRateApiService : ExchangeRateApiService {
    var shouldSucceed = true
    var mockRates = mapOf("EUR" to 0.85, "GBP" to 0.73)
    
    override suspend fun getLatestRates(
        apiKey: String,
        baseCurrency: String,
        baseUrl: String
    ): Result<ExchangeRateResponse> {
        return if (shouldSucceed) {
            Result.success(ExchangeRateResponse(
                result = "success",
                baseCode = baseCurrency,
                conversionRates = mockRates
            ))
        } else {
            Result.failure(Exception("API Error"))
        }
    }
}
```

---

## Test Coverage Goals

### Target Coverage
- **ViewModels:** 80-90% line coverage
- **Repositories:** 85-95% line coverage
- **Business Logic:** 95%+ coverage

### Priority Order
1. **Critical Path:** Save expense, load expenses, currency conversion
2. **User Flows:** Add expense with voice, filter history, change settings
3. **Edge Cases:** Error handling, validation, empty states
4. **Polish:** Optimistic updates, state persistence, complex filtering

### Excluded from Coverage
- Platform-specific implementations (`expect`/`actual`)
- UI Composables (use instrumented tests instead)
- Main Application class
- Simple data classes with no logic
- Database entities (simple DTOs)

---

## Implementation Phases

### Phase 1: Foundation (Week 1)
- ✅ Add test dependencies
- ✅ Create mock base classes
- ✅ Write repository tests
- Target: 50+ tests

### Phase 2: Core ViewModels (Week 2)
- ✅ AddExpenseViewModel tests
- ✅ ExpenseHistoryViewModel tests
- ✅ DashBoardViewModel tests
- Target: 100+ tests

### Phase 3: Advanced ViewModels (Week 3)
- ✅ SettingsViewModel tests
- ✅ CurrencyExchangeViewModel tests
- ✅ VoiceInputViewModel tests
- Target: 140+ tests

### Phase 4: Integration & Coverage (Week 4)
- ✅ Fix failing tests
- ✅ Achieve coverage goals
- ✅ Documentation
- Target: 100% planned tests passing

---

## Summary

### Total Test Count: 448 tests

#### ViewModels: 274 tests
- VoiceInputViewModel: 35 tests
- AddExpenseViewModel: 44 tests
- DashBoardViewModel: 31 tests
- SettingsViewModel: 55 tests
- ExpenseHistoryViewModel: 59 tests
- CurrencyExchangeViewModel: 50 tests

#### Repositories: 174 tests
- SettingsRepository: 53 tests
- ExchangeRateRepository: 69 tests
- ExpenseRepository: 52 tests

### Estimated Effort
- **Setup:** 4 hours
- **Repository Tests:** 20 hours
- **ViewModel Tests:** 35 hours
- **Debugging & Refinement:** 10 hours
- **Total:** ~70 hours (2 weeks for 1 developer)

### Next Steps
1. Review and approve this plan
2. Add test dependencies to build.gradle.kts
3. Create test utility classes and mocks
4. Implement tests following priority order
5. Track progress with coverage reports

