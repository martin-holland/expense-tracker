# ViewModel Test Specifications

This document provides detailed test specifications for each ViewModel with example implementations.

## Table of Contents
1. [VoiceInputViewModel](#1-voiceinputviewmodel)
2. [AddExpenseViewModel](#2-addexpenseviewmodel)
3. [DashBoardViewModel](#3-dashboardviewmodel)
4. [SettingsViewModel](#4-settingsviewmodel)
5. [ExpenseHistoryViewModel](#5-expensehistoryviewmodel)
6. [CurrencyExchangeViewModel](#6-currencyexchangeviewmodel)

---

## 1. VoiceInputViewModel

**File:** `VoiceInputViewModel.kt`  
**Complexity:** High  
**Total Tests:** 35

### Dependencies
- `MicrophoneService` (expect/actual pattern)
- `ExpenseParser` (service object)

### State Properties

| Property | Type | Initial Value | Description |
|----------|------|---------------|-------------|
| `isRecording` | `StateFlow<Boolean>` | `false` | Audio recording status |
| `audioData` | `StateFlow<ByteArray?>` | `null` | Recorded audio bytes |
| `isProcessing` | `StateFlow<Boolean>` | `false` | Processing indicator |
| `errorMessage` | `StateFlow<String?>` | `null` | Error message |
| `showVoiceSection` | `StateFlow<Boolean>` | `false` | Voice UI visibility |
| `speechRecognitionState` | `StateFlow<SpeechRecognitionState>` | `Idle` | Recognition state |
| `partialTranscription` | `StateFlow<String>` | `""` | Partial results |
| `parsedExpenseData` | `StateFlow<ParsedExpenseData?>` | `null` | Parsed data |
| `manualEntryText` | `StateFlow<String>` | `""` | Manual entry text |
| `showManualEntry` | `StateFlow<Boolean>` | `false` | Manual entry visibility |
| `recognizerReadyForCleanup` | `StateFlow<Boolean>` | `false` | Cleanup signal |

### Test Categories

#### A. State Management Tests (15 tests)

```kotlin
class VoiceInputViewModelTest {

    private lateinit var viewModel: VoiceInputViewModel
    private lateinit var fakeMicService: FakeMicrophoneService
    
    @BeforeTest
    fun setup() {
        fakeMicService = FakeMicrophoneService()
        // Need to inject mock service somehow
        viewModel = VoiceInputViewModel()
    }
    
    @Test
    fun `initial state is Idle`() {
        assertEquals(SpeechRecognitionState.Idle, viewModel.speechRecognitionState.value)
    }
    
    @Test
    fun `toggleVoiceSection toggles visibility`() {
        assertFalse(viewModel.showVoiceSection.value)
        viewModel.toggleVoiceSection()
        assertTrue(viewModel.showVoiceSection.value)
        viewModel.toggleVoiceSection()
        assertFalse(viewModel.showVoiceSection.value)
    }
    
    @Test
    fun `startSpeechRecognition sets state to Listening`() = runTest {
        viewModel.startSpeechRecognition()
        advanceUntilIdle()
        
        assertEquals(
            SpeechRecognitionState.Listening,
            viewModel.speechRecognitionState.value
        )
        assertEquals("", viewModel.partialTranscription.value)
    }
    
    @Test
    fun `stopSpeechRecognition sets state to Processing`() {
        viewModel.stopSpeechRecognition()
        
        assertEquals(
            SpeechRecognitionState.Processing,
            viewModel.speechRecognitionState.value
        )
    }
    
    @Test
    fun `onSpeechResult with Success updates state correctly`() {
        val result = SpeechRecognitionResult.Success(
            text = "Test transcription",
            confidence = 0.95f,
            alternatives = listOf(
                Alternative("Alternative 1", 0.85f),
                Alternative("Alternative 2", 0.75f)
            )
        )
        
        viewModel.onSpeechResult(result)
        
        val state = viewModel.speechRecognitionState.value
        assertTrue(state is SpeechRecognitionState.Success)
        val successState = state as SpeechRecognitionState.Success
        assertEquals("Test transcription", successState.transcription)
        assertEquals(0.95f, successState.confidence)
        assertEquals(2, successState.alternatives.size)
        assertTrue(viewModel.recognizerReadyForCleanup.value)
    }
    
    @Test
    fun `onSpeechResult with Error updates error state`() {
        val result = SpeechRecognitionResult.Error("Recognition failed")
        
        viewModel.onSpeechResult(result)
        
        val state = viewModel.speechRecognitionState.value
        assertTrue(state is SpeechRecognitionState.Error)
        assertEquals("Recognition failed", (state as SpeechRecognitionState.Error).message)
        assertTrue(viewModel.recognizerReadyForCleanup.value)
    }
    
    @Test
    fun `onPartialTranscription updates partial text`() {
        viewModel.onPartialTranscription("Partial text")
        assertEquals("Partial text", viewModel.partialTranscription.value)
    }
    
    @Test
    fun `resetSpeechRecognition clears all state`() {
        // Setup: set some state
        viewModel.startSpeechRecognition()
        viewModel.onPartialTranscription("Some text")
        viewModel.parseTranscription("Test")
        
        // Action
        viewModel.resetSpeechRecognition()
        
        // Assert
        assertEquals(SpeechRecognitionState.Idle, viewModel.speechRecognitionState.value)
        assertEquals("", viewModel.partialTranscription.value)
        assertNull(viewModel.parsedExpenseData.value)
    }
    
    @Test
    fun `cancelVoiceInput resets to clean state`() {
        // Setup: set various states
        viewModel.startSpeechRecognition()
        viewModel.onPartialTranscription("Text")
        viewModel.parseTranscription("Test")
        viewModel.enableManualEntry()
        
        // Action
        viewModel.cancelVoiceInput()
        
        // Assert all cleared
        assertEquals(SpeechRecognitionState.Idle, viewModel.speechRecognitionState.value)
        assertEquals("", viewModel.partialTranscription.value)
        assertNull(viewModel.parsedExpenseData.value)
        assertFalse(viewModel.showManualEntry.value)
        assertEquals("", viewModel.manualEntryText.value)
        assertNull(viewModel.errorMessage.value)
        assertFalse(viewModel.recognizerReadyForCleanup.value)
    }
    
    @Test
    fun `recognizerReadyForCleanup flag works correctly`() {
        assertFalse(viewModel.recognizerReadyForCleanup.value)
        
        viewModel.onSpeechResult(SpeechRecognitionResult.Success("Test", 0.9f, emptyList()))
        assertTrue(viewModel.recognizerReadyForCleanup.value)
        
        viewModel.acknowledgeCleanup()
        assertFalse(viewModel.recognizerReadyForCleanup.value)
    }
    
    @Test
    fun `acknowledgeCleanup clears cleanup flag`() {
        viewModel.onSpeechResult(SpeechRecognitionResult.Success("Test", 0.9f, emptyList()))
        assertTrue(viewModel.recognizerReadyForCleanup.value)
        
        viewModel.acknowledgeCleanup()
        assertFalse(viewModel.recognizerReadyForCleanup.value)
    }
    
    // Additional state management tests...
}
```

#### B. Audio Recording Tests (5 tests)

**Key Test Cases:**
- Recording state updates when starting/stopping
- Audio data captured on stop
- Play audio with valid data
- Error handling for recording failures
- Clear error message

```kotlin
@Test
fun `startRecording updates isRecording state`() = runTest {
    fakeMicService.startWillSucceed = true
    
    viewModel.startRecording()
    advanceUntilIdle()
    
    assertTrue(viewModel.isRecording.value)
    assertNull(viewModel.errorMessage.value)
}

@Test
fun `stopRecording captures audio data`() = runTest {
    val testAudio = ByteArray(100) { it.toByte() }
    fakeMicService.mockAudioData = testAudio
    
    viewModel.stopRecording()
    advanceUntilIdle()
    
    assertFalse(viewModel.isRecording.value)
    assertContentEquals(testAudio, viewModel.audioData.value)
}

@Test
fun `recording failure sets error message`() = runTest {
    fakeMicService.startWillSucceed = false
    
    viewModel.startRecording()
    advanceUntilIdle()
    
    assertFalse(viewModel.isRecording.value)
    assertEquals("Failed to start recording", viewModel.errorMessage.value)
}
```

#### C. Parsing Tests (8 tests)

**Key Test Cases:**
- Parse transcription extracts expense data
- Parsed data includes all fields
- Clear parsed data
- Auto-parsing on speech success
- Update individual fields
- Field updates preserve other fields
- Handle invalid input
- Completeness calculation

```kotlin
@Test
fun `parseTranscription extracts expense data`() {
    val transcription = "Lunch at restaurant 45.50 euros"
    
    viewModel.parseTranscription(transcription)
    
    val parsed = viewModel.parsedExpenseData.value
    assertNotNull(parsed)
    assertEquals(45.5, parsed!!.amount)
    assertEquals(Currency.EUR, parsed.currency)
    assertTrue(parsed.completeness > 0)
}

@Test
fun `updateParsedField updates individual fields`() {
    // Setup initial data
    viewModel.parseTranscription("Test 10 USD")
    val initial = viewModel.parsedExpenseData.value
    
    // Update amount only
    viewModel.updateParsedField(amount = 20.0)
    
    val updated = viewModel.parsedExpenseData.value
    assertEquals(20.0, updated!!.amount)
    assertEquals(initial!!.currency, updated.currency) // Preserved
    assertEquals(initial.category, updated.category)   // Preserved
}

@Test
fun `clearParsedData clears parsed state`() {
    viewModel.parseTranscription("Test")
    assertNotNull(viewModel.parsedExpenseData.value)
    
    viewModel.clearParsedData()
    assertNull(viewModel.parsedExpenseData.value)
}
```

#### D. Manual Entry Tests (7 tests)

**Key Test Cases:**
- Enable manual entry shows UI
- Text changes update state
- Parse manual entry processes text
- Parsing hides entry
- Blank text ignored
- Cancel clears and hides
- Manual entry independent from voice

```kotlin
@Test
fun `enableManualEntry shows manual entry UI`() {
    assertFalse(viewModel.showManualEntry.value)
    
    viewModel.enableManualEntry()
    
    assertTrue(viewModel.showManualEntry.value)
    assertEquals("", viewModel.manualEntryText.value)
}

@Test
fun `parseManualEntry ignores blank text`() {
    viewModel.enableManualEntry()
    viewModel.onManualEntryTextChanged("   ")
    
    viewModel.parseManualEntry()
    
    assertNull(viewModel.parsedExpenseData.value)
    assertTrue(viewModel.showManualEntry.value) // Still shown
}

@Test
fun `cancelManualEntry clears and hides entry`() {
    viewModel.enableManualEntry()
    viewModel.onManualEntryTextChanged("Test text")
    
    viewModel.cancelManualEntry()
    
    assertFalse(viewModel.showManualEntry.value)
    assertEquals("", viewModel.manualEntryText.value)
}
```

---

## 2. AddExpenseViewModel

**File:** `AddExpenseViewModel.kt`  
**Complexity:** Medium-High  
**Total Tests:** 44

### Dependencies
- `ExpenseRepository` (singleton)

### State Properties

| Property | Type | Initial Value | Description |
|----------|------|---------------|-------------|
| `currency` | `var` | `Currency.USD` | Selected currency |
| `amount` | `var` | `""` | Amount input |
| `category` | `var` | `null` | Selected category |
| `date` | `var` | Today | Formatted date |
| `note` | `var` | `""` | Description/note |
| `errorMessage` | `var` | `null` | Validation error |
| `isSaving` | `var` | `false` | Save operation state |
| `snackbarMessage` | `var` | `null` | User feedback |

### Test Categories

#### A. Initialization Tests (3 tests)

```kotlin
class AddExpenseViewModelTest {
    
    private lateinit var viewModel: AddExpenseViewModel
    private lateinit var fakeRepository: FakeExpenseRepository
    
    @BeforeTest
    fun setup() {
        fakeRepository = FakeExpenseRepository()
        viewModel = AddExpenseViewModel(fakeRepository)
    }
    
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
    fun `date is set to today on init`() {
        // Check date is not empty and follows format
        assertTrue(viewModel.date.isNotBlank())
        assertTrue(viewModel.date.matches(Regex("\\w+ \\d+, \\d{4}")))
    }
    
    @Test
    fun `default currency is USD`() {
        assertEquals(Currency.USD, viewModel.currency)
    }
}
```

#### B. Form Field Update Tests (8 tests)

```kotlin
@Test
fun `onCurrencySelected updates currency`() {
    viewModel.onCurrencySelected(Currency.EUR)
    assertEquals(Currency.EUR, viewModel.currency)
    assertNull(viewModel.errorMessage) // Error cleared
}

@Test
fun `onAmountChanged accepts valid decimal`() {
    viewModel.onAmountChanged("45.50")
    assertEquals("45.50", viewModel.amount)
    
    viewModel.onAmountChanged("100")
    assertEquals("100", viewModel.amount)
    
    viewModel.onAmountChanged("0.99")
    assertEquals("0.99", viewModel.amount)
}

@Test
fun `onAmountChanged rejects invalid input`() {
    viewModel.onAmountChanged("45.50")
    assertEquals("45.50", viewModel.amount)
    
    // Try invalid input
    viewModel.onAmountChanged("abc")
    assertEquals("45.50", viewModel.amount) // Unchanged
    
    viewModel.onAmountChanged("12.34.56")
    assertEquals("45.50", viewModel.amount) // Unchanged
}

@Test
fun `onCategorySelected updates category`() {
    viewModel.onCategorySelected(ExpenseCategory.FOOD)
    assertEquals(ExpenseCategory.FOOD, viewModel.category)
    assertNull(viewModel.errorMessage)
}

@Test
fun `amount validation regex works correctly`() {
    val validAmounts = listOf("123", "123.45", "0.99", ".99", "")
    val invalidAmounts = listOf("abc", "12.34.56", "12a", "-10")
    
    validAmounts.forEach { amount ->
        viewModel.onAmountChanged(amount)
        assertEquals(amount, viewModel.amount)
    }
    
    viewModel.onAmountChanged("100") // Reset to valid
    invalidAmounts.forEach { amount ->
        viewModel.onAmountChanged(amount)
        assertEquals("100", viewModel.amount) // Should remain unchanged
    }
}
```

#### C. Date Management Tests (5 tests)

```kotlin
@Test
fun `parseDate handles valid date strings`() {
    // This tests the private parseDate method indirectly
    viewModel.onDateSelected("November 7, 2024")
    assertEquals("November 7, 2024", viewModel.date)
}

@Test
fun `date format is Month Day comma Year`() {
    val dateRegex = Regex("^\\w+ \\d+, \\d{4}$")
    assertTrue(viewModel.date.matches(dateRegex))
}

@Test
fun `all month names parse correctly`() {
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    
    months.forEachIndexed { index, month ->
        viewModel.onDateSelected("$month 15, 2024")
        assertEquals("$month 15, 2024", viewModel.date)
    }
}
```

#### D. Validation Tests (8 tests)

```kotlin
@Test
fun `empty amount shows error`() {
    viewModel.amount = ""
    viewModel.category = ExpenseCategory.FOOD
    viewModel.date = "November 7, 2024"
    
    viewModel.saveExpense()
    
    assertNotNull(viewModel.snackbarMessage)
    assertEquals(SnackbarType.ERROR, viewModel.snackbarMessage?.type)
    assertTrue(viewModel.snackbarMessage?.message?.contains("amount") == true)
}

@Test
fun `invalid amount shows error`() {
    viewModel.amount = "."
    viewModel.category = ExpenseCategory.FOOD
    
    viewModel.saveExpense()
    
    assertNotNull(viewModel.snackbarMessage)
    assertEquals(SnackbarType.ERROR, viewModel.snackbarMessage?.type)
}

@Test
fun `null category shows error`() {
    viewModel.amount = "50.00"
    viewModel.category = null
    
    viewModel.saveExpense()
    
    assertNotNull(viewModel.snackbarMessage)
    assertTrue(viewModel.snackbarMessage?.message?.contains("category") == true)
}

@Test
fun `valid input passes validation`() = runTest {
    viewModel.amount = "50.00"
    viewModel.category = ExpenseCategory.FOOD
    viewModel.date = "November 7, 2024"
    
    viewModel.saveExpense()
    advanceUntilIdle()
    
    // Should not have error message (but may have success)
    assertFalse(viewModel.snackbarMessage?.type == SnackbarType.ERROR)
    assertEquals(1, fakeRepository.savedExpenses.size)
}
```

#### E. Save Operation Tests (10 tests)

```kotlin
@Test
fun `saveExpense validates before saving`() = runTest {
    viewModel.amount = "" // Invalid
    
    viewModel.saveExpense()
    advanceUntilIdle()
    
    assertEquals(0, fakeRepository.savedExpenses.size) // Nothing saved
    assertNotNull(viewModel.snackbarMessage)
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
    
    assertEquals(1, fakeRepository.savedExpenses.size)
    val saved = fakeRepository.savedExpenses.first()
    assertEquals(75.5, saved.amount)
    assertEquals(ExpenseCategory.TRAVEL, saved.category)
    assertEquals("Gas station", saved.description)
}

@Test
fun `successful save shows success snackbar`() = runTest {
    viewModel.amount = "50.00"
    viewModel.category = ExpenseCategory.FOOD
    
    viewModel.saveExpense()
    advanceUntilIdle()
    
    assertNotNull(viewModel.snackbarMessage)
    assertEquals(SnackbarType.SUCCESS, viewModel.snackbarMessage?.type)
    assertTrue(viewModel.snackbarMessage?.message?.contains("success") == true)
}

@Test
fun `successful save clears form`() = runTest {
    viewModel.amount = "50.00"
    viewModel.category = ExpenseCategory.FOOD
    viewModel.note = "Test note"
    
    viewModel.saveExpense()
    advanceUntilIdle()
    
    assertEquals("", viewModel.amount)
    assertNull(viewModel.category)
    assertEquals("", viewModel.note)
}

@Test
fun `isSaving flag updates correctly`() = runTest {
    fakeRepository.delayMs = 100 // Simulate slow save
    
    viewModel.amount = "50.00"
    viewModel.category = ExpenseCategory.FOOD
    
    assertFalse(viewModel.isSaving)
    
    viewModel.saveExpense()
    assertTrue(viewModel.isSaving)
    
    advanceUntilIdle()
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

@Test
fun `note defaults to No description if empty`() = runTest {
    viewModel.amount = "50.00"
    viewModel.category = ExpenseCategory.FOOD
    viewModel.note = "" // Empty
    
    viewModel.saveExpense()
    advanceUntilIdle()
    
    val saved = fakeRepository.savedExpenses.first()
    assertEquals("No description", saved.description)
}
```

#### F. Voice Integration Tests (5 tests)

```kotlin
@Test
fun `populateFromParsedData fills all fields`() {
    val parsedData = ParsedExpenseData(
        rawText = "Lunch 45 euros",
        amount = 45.0,
        currency = Currency.EUR,
        category = ExpenseCategory.FOOD,
        description = "Lunch at restaurant",
        completeness = 1.0,
        isUsable = true
    )
    
    viewModel.populateFromParsedData(parsedData)
    
    assertEquals("45.0", viewModel.amount)
    assertEquals(Currency.EUR, viewModel.currency)
    assertEquals(ExpenseCategory.FOOD, viewModel.category)
    assertEquals("Lunch at restaurant", viewModel.note)
    assertNull(viewModel.errorMessage)
}

@Test
fun `populateFromParsedData ignores null values`() {
    viewModel.amount = "100"
    viewModel.currency = Currency.GBP
    
    val parsedData = ParsedExpenseData(
        rawText = "Test",
        amount = null, // Should be ignored
        currency = null, // Should be ignored
        category = ExpenseCategory.TRAVEL,
        description = "Test",
        completeness = 0.5,
        isUsable = false
    )
    
    viewModel.populateFromParsedData(parsedData)
    
    assertEquals("100", viewModel.amount) // Unchanged
    assertEquals(Currency.GBP, viewModel.currency) // Unchanged
    assertEquals(ExpenseCategory.TRAVEL, viewModel.category) // Updated
}
```

---

## 3. DashBoardViewModel

**File:** `DashBoardViewModel.kt`  
**Complexity:** Medium  
**Total Tests:** 31

### Dependencies
- `ExpenseRepository` (singleton)
- `ExpenseAggregator` (domain logic object)

### State Structure

```kotlin
data class DashboardUiState(
    val expenses: List<Expense> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val currentMonth: YearMonth? = null,
    val monthlyAggregate: MonthlyAggregate? = null,
    val previousMonthAggregate: MonthlyAggregate? = null,
    val monthOverMonthChange: Double? = null,
    val categoryTotals: List<CategoryTotal> = emptyList(),
    val weeklyAggregates: List<WeeklyAggregate> = emptyList(),
    val dailyAggregates: List<DailyAggregate> = emptyList()
)
```

### Test Categories

#### A. Initialization Tests (3 tests)

```kotlin
class DashBoardViewModelTest {
    
    private lateinit var viewModel: DashBoardViewModel
    private lateinit var fakeRepository: FakeExpenseRepository
    
    @BeforeTest
    fun setup() {
        fakeRepository = FakeExpenseRepository()
        // Would need dependency injection to pass repository
        viewModel = DashBoardViewModel()
    }
    
    @Test
    fun `initial state shows loading`() {
        assertTrue(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.expenses.isEmpty())
    }
    
    @Test
    fun `loadExpenses called on init`() = runTest {
        advanceUntilIdle()
        
        // Repository should be accessed
        assertTrue(fakeRepository.getAllExpensesCalled)
    }
    
    @Test
    fun `repository flow observed automatically`() = runTest {
        // Add expense after init
        fakeRepository.addExpense(createTestExpense())
        advanceUntilIdle()
        
        // State should update
        assertEquals(1, viewModel.uiState.value.expenses.size)
    }
}
```

#### B. Data Loading Tests (8 tests)

```kotlin
@Test
fun `empty expense list shows empty state`() = runTest {
    fakeRepository.setExpenses(emptyList())
    advanceUntilIdle()
    
    val state = viewModel.uiState.value
    assertTrue(state.expenses.isEmpty())
    assertFalse(state.isLoading)
    assertNull(state.error)
}

@Test
fun `expenses update UI state`() = runTest {
    val expenses = listOf(
        createTestExpense(id = "1", amount = 50.0),
        createTestExpense(id = "2", amount = 75.0),
        createTestExpense(id = "3", amount = 100.0)
    )
    
    fakeRepository.setExpenses(expenses)
    advanceUntilIdle()
    
    val state = viewModel.uiState.value
    assertEquals(3, state.expenses.size)
    assertFalse(state.isLoading)
    assertNull(state.error)
}

@Test
fun `loading flag cleared after load`() = runTest {
    assertTrue(viewModel.uiState.value.isLoading)
    
    fakeRepository.setExpenses(listOf(createTestExpense()))
    advanceUntilIdle()
    
    assertFalse(viewModel.uiState.value.isLoading)
}

@Test
fun `error state updated on failure`() = runTest {
    fakeRepository.shouldThrowError = true
    fakeRepository.errorMessage = "Database error"
    
    advanceUntilIdle()
    
    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals("Database error", state.error)
}
```

#### C. Aggregation Tests (15 tests)

```kotlin
@Test
fun `updateAggregates processes expenses`() = runTest {
    val now = LocalDateTime(2024, 11, 15, 12, 0)
    val expenses = listOf(
        createTestExpense(amount = 50.0, date = now),
        createTestExpense(amount = 75.0, date = now),
        createTestExpense(amount = 100.0, date = now)
    )
    
    fakeRepository.setExpenses(expenses)
    advanceUntilIdle()
    
    val state = viewModel.uiState.value
    assertNotNull(state.monthlyAggregate)
    assertEquals(225.0, state.monthlyAggregate?.total)
}

@Test
fun `current month aggregate calculated`() = runTest {
    val currentMonth = YearMonth(2024, kotlinx.datetime.Month.NOVEMBER)
    val expenses = listOf(
        createTestExpense(
            date = LocalDateTime(2024, 11, 15, 12, 0),
            amount = 100.0
        )
    )
    
    fakeRepository.setExpenses(expenses)
    advanceUntilIdle()
    
    val state = viewModel.uiState.value
    assertEquals(currentMonth, state.currentMonth)
    assertNotNull(state.monthlyAggregate)
}

@Test
fun `previous month aggregate calculated`() = runTest {
    val lastMonth = LocalDateTime(2024, 10, 15, 12, 0)
    val expenses = listOf(
        createTestExpense(date = lastMonth, amount = 50.0)
    )
    
    fakeRepository.setExpenses(expenses)
    advanceUntilIdle()
    
    val state = viewModel.uiState.value
    assertNotNull(state.previousMonthAggregate)
}

@Test
fun `month-over-month change computed`() = runTest {
    val thisMonth = LocalDateTime(2024, 11, 15, 12, 0)
    val lastMonth = LocalDateTime(2024, 10, 15, 12, 0)
    
    val expenses = listOf(
        createTestExpense(date = thisMonth, amount = 200.0),
        createTestExpense(date = lastMonth, amount = 100.0)
    )
    
    fakeRepository.setExpenses(expenses)
    advanceUntilIdle()
    
    val state = viewModel.uiState.value
    assertNotNull(state.monthOverMonthChange)
    // 200 vs 100 = 100% increase
    assertEquals(1.0, state.monthOverMonthChange, 0.01)
}

@Test
fun `category totals aggregated`() = runTest {
    val expenses = listOf(
        createTestExpense(category = ExpenseCategory.FOOD, amount = 50.0),
        createTestExpense(category = ExpenseCategory.FOOD, amount = 75.0),
        createTestExpense(category = ExpenseCategory.TRAVEL, amount = 100.0)
    )
    
    fakeRepository.setExpenses(expenses)
    advanceUntilIdle()
    
    val state = viewModel.uiState.value
    assertTrue(state.categoryTotals.isNotEmpty())
    
    val foodTotal = state.categoryTotals.find { it.category == "Food" }
    assertEquals(125.0, foodTotal?.total)
}

@Test
fun `YearMonth minusMonths works correctly`() {
    val nov2024 = YearMonth(2024, kotlinx.datetime.Month.NOVEMBER)
    val oct2024 = nov2024.minusMonths(1)
    val sep2024 = nov2024.minusMonths(2)
    
    assertEquals(YearMonth(2024, kotlinx.datetime.Month.OCTOBER), oct2024)
    assertEquals(YearMonth(2024, kotlinx.datetime.Month.SEPTEMBER), sep2024)
    
    // Test year rollover
    val jan2024 = YearMonth(2024, kotlinx.datetime.Month.JANUARY)
    val dec2023 = jan2024.minusMonths(1)
    assertEquals(YearMonth(2023, kotlinx.datetime.Month.DECEMBER), dec2023)
}

@Test
fun `Expense to Transaction conversion works`() {
    val expense = createTestExpense(
        id = "test-1",
        description = "Lunch",
        amount = 45.50,
        category = ExpenseCategory.FOOD
    )
    
    val transaction = expense.toTransaction()
    
    assertEquals("test-1", transaction.id)
    assertEquals("Lunch", transaction.title)
    assertEquals(45.50, transaction.amount)
    assertEquals("Food", transaction.category)
}
```

---

*Continue with remaining ViewModels...*

## Test Implementation Priority

### Phase 1 (Critical - Week 1)
1. AddExpenseViewModel (core functionality)
2. ExpenseRepository (data layer)
3. SettingsRepository (configuration)

### Phase 2 (Important - Week 2)
4. ExpenseHistoryViewModel (user-facing)
5. DashBoardViewModel (analytics)
6. ExchangeRateRepository (currency features)

### Phase 3 (Advanced - Week 3)
7. SettingsViewModel (complex state)
8. CurrencyExchangeViewModel (conversion logic)
9. VoiceInputViewModel (experimental feature)

---

## Mock Implementation Examples

### FakeExpenseRepository

```kotlin
class FakeExpenseRepository : ExpenseRepository {
    private val expenses = mutableListOf<Expense>()
    private val _expensesFlow = MutableStateFlow<List<Expense>>(emptyList())
    
    var shouldThrowError = false
    var errorMessage = "Test error"
    var delayMs = 0L
    var getAllExpensesCalled = false
    
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
        if (delayMs > 0) kotlinx.coroutines.delay(delayMs)
        if (shouldThrowError) throw Exception(errorMessage)
        
        expenses.add(expense)
        _expensesFlow.value = expenses.toList()
    }
    
    override suspend fun deleteExpense(expense: Expense) {
        expenses.removeAll { it.id == expense.id }
        _expensesFlow.value = expenses.toList()
    }
    
    override suspend fun getExpenseById(id: String): Expense? {
        return expenses.find { it.id == id }
    }
    
    fun setExpenses(newExpenses: List<Expense>) {
        expenses.clear()
        expenses.addAll(newExpenses)
        _expensesFlow.value = expenses.toList()
    }
    
    fun addExpense(expense: Expense) {
        expenses.add(expense)
        _expensesFlow.value = expenses.toList()
    }
    
    // ... implement other methods
}
```

### Test Helpers

```kotlin
// Helper functions for creating test data
fun createTestExpense(
    id: String = "test-${Random.nextInt()}",
    category: ExpenseCategory = ExpenseCategory.FOOD,
    description: String = "Test expense",
    amount: Double = 50.0,
    currency: Currency = Currency.USD,
    date: LocalDateTime = LocalDateTime(2024, 11, 15, 12, 0)
): Expense {
    return Expense(
        id = id,
        category = category,
        description = description,
        amount = amount,
        currency = currency,
        date = date
    )
}

fun createTestExpenses(count: Int): List<Expense> {
    return List(count) { index ->
        createTestExpense(
            id = "test-$index",
            amount = (index + 1) * 10.0
        )
    }
}
```

---

## Next Steps

1. ✅ Review test specifications
2. ✅ Create fake/mock implementations
3. ✅ Set up test infrastructure
4. ✅ Implement tests by priority
5. ✅ Run and debug tests
6. ✅ Measure coverage
7. ✅ Document results


