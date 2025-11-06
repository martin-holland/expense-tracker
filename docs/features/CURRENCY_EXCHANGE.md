# Epic 6: Currency Exchange Feature Specification

## Overview

This document provides a comprehensive specification for implementing currency exchange functionality in the Expense Tracker application. The feature will allow users to convert foreign currencies to their base currency, enabling accurate expense tracking across multiple currencies.

**Goal:** Convert foreign currencies to user's base currency for unified expense tracking and reporting.

**Architecture Alignment:** This feature follows the existing MVVM + Repository Pattern architecture and is designed to be compatible with both Android and iOS platforms (Kotlin Multiplatform).

---

## Architecture Considerations

### Current Architecture

- **Pattern:** MVVM with Repository Pattern
- **Database:** Room KMP (cross-platform)
- **Reactive Data:** Kotlin Flow
- **UI:** Jetpack Compose (shared)
- **Currency Model:** Already exists with 12 currencies (USD, EUR, GBP, JPY, CHF, CAD, AUD, CNY, INR, SEK, NOK, DKK)

### New Components Required

1. **Settings Storage:** User preferences (base currency)
2. **Exchange Rate API Client:** Ktor-based HTTP client
3. **Exchange Rate Repository:** Manages exchange rate data
4. **Exchange Rate Cache:** Local storage for offline support
5. **Currency Converter Service:** Business logic for conversions
6. **Background Worker:** Periodic exchange rate refresh

---

## Task Breakdown

> **Note:** Tasks are organized with UI elements first, followed by backend implementation. This allows building the UI with mock data and wiring it up to real functionality later.

### Task 1: UI Components (Currency Exchange Banner)

**Priority:** High  
**Estimated Complexity:** Low  
**Dependencies:** None (can use mock data)

#### Subtask 1.1: Create Currency Exchange Banner Component

- **File:** `commonMain/kotlin/com/example/expensetracker/view/components/CurrencyExchangeBanner.kt`
- **Description:** Reusable banner component for currency exchange feature
- **Design:**
  - Gradient background: Light blue-green (left) to medium blue (right)
  - Rounded corners: 16dp
  - Horizontal layout with padding: 16dp
  - Icon: Two horizontal arrows (exchange icon) in white, on light blue circular background (40dp)
  - Text section:
    - Title: "Currency Exchange" (white, bold, 16sp)
    - Subtitle: "Convert & track rates" (white, regular, 12sp)
  - Button: "Open" button on the right
    - White background, rounded corners (8dp)
    - Blue text, bold
    - Padding: 12dp horizontal, 8dp vertical
- **Parameters:**
  - `onOpenClick: () -> Unit` - Callback when "Open" button is clicked
  - `modifier: Modifier` - For layout customization
- **Accessibility:**
  - Content description for icon
  - Clickable area for entire banner (optional enhancement)
- **Implementation Note:** Initially uses mock callback, will be wired to navigation later

#### Subtask 1.2: Add Banner to ExpenseHistoryScreen

- **File:** `commonMain/kotlin/com/example/expensetracker/view/ExpenseHistoryScreen.kt`
- **Description:** Integrate currency exchange banner into expense history screen
- **Placement:**
  - Position: Below the collapsing header, above "Total Expenses" section
  - Inside LazyColumn as first item (before date headers)
  - Padding: 16dp horizontal, 8dp vertical (top), 16dp vertical (bottom)
- **Implementation:**
  - Add banner as first item in LazyColumn
  - Use `item(key = "currency_exchange_banner")` for proper key
  - Initially: Show toast or log when "Open" button clicked (mock implementation)
  - Later: Will navigate to currency exchange screen/dialog
- **Navigation (Future):**
  - When "Open" button clicked, navigate to currency exchange view
  - Can be a full screen, bottom sheet, or dialog (to be determined)
  - Bottom navigation bar should remain visible (banner is in scrollable content)
- **Conditional Display (Optional):**
  - Consider hiding banner if API is not configured (future enhancement)
  - Or show banner with different message prompting API configuration

#### Subtask 1.3: Create Currency Exchange Screen/Dialog (UI Only)

- **File:** `commonMain/kotlin/com/example/expensetracker/view/CurrencyExchangeScreen.kt`
- **Description:** Screen/dialog UI for viewing and managing currency conversions
- **Options for Implementation:**
  - **Option A:** Full screen (navigate to new screen)
    - Requires navigation setup
    - Bottom nav remains visible if using same navigation structure
  - **Option B:** Bottom sheet (recommended)
    - Slides up from bottom
    - Bottom nav remains visible below sheet
    - Easy to dismiss
  - **Option C:** Dialog
    - Modal overlay
    - Bottom nav remains visible
- **UI Components (Mock Data Initially):**
  - Header: "Currency Exchange" title with close button
  - Current base currency display (mock: "USD")
  - List of expenses with original and converted amounts (mock data)
  - Exchange rate information section (mock rates)
  - Last update timestamp (mock: "Updated 2 hours ago")
  - "Refresh Rates" button (initially shows toast/log)
- **State Management:**
  - Create placeholder ViewModel or use local state for now
  - Will be replaced with real ViewModel later
- **Dependencies:** None initially (uses mock data), will require CurrencyConverter and SettingsRepository later

---

### Task 2: Settings UI Implementation

**Priority:** High  
**Estimated Complexity:** Medium  
**Dependencies:** None (can use mock data initially)

#### Subtask 2.1: Create Settings Screen UI

- **File:** `commonMain/kotlin/com/example/expensetracker/view/SettingsScreen.kt`
- **Description:** Compose UI for settings screen
- **Components:**
  - Header: "Settings" title
  - Base Currency Section:
    - Label: "Base Currency"
    - Dropdown/Selector: Show all currencies with symbols
    - Current selection highlighted (mock: USD initially)
    - Description: "All expenses will be converted to this currency"
  - Exchange Rate API Configuration Section:
    - Label: "Exchange Rate API"
    - API Key input field (masked/secure input)
    - API Base URL input field (editable, default: "https://v6.exchangerate-api.com/v6")
    - "Test API Connection" button (initially shows toast/log)
    - Status indicator (configured/not configured) - mock state
  - Exchange Rate Section:
    - Label: "Exchange Rates"
    - Last update time display (mock: "Never" or "2 hours ago")
    - "Refresh Now" button (initially shows toast/log)
    - Note: "Rates refresh automatically every 24 hours"
  - Info Section:
    - Note about offline fallback
    - Link to API provider (exchangerate-api.com)
    - Note about API key requirement
    - Information about cross-rate calculation optimization
- **State Management:**
  - Use local state (remember/mutableStateOf) initially
  - Will be replaced with SettingsViewModel later
- **Navigation:** Update `App.kt` to show SettingsScreen instead of BlankScreen

#### Subtask 2.2: Create Settings ViewModel (Placeholder)

- **File:** `commonMain/kotlin/com/example/expensetracker/viewmodel/SettingsViewModel.kt`
- **Description:** ViewModel for settings screen (initially with mock data)
- **State (Mock Initially):**
  - `baseCurrency: StateFlow<Currency>` - Current base currency (mock: USD)
  - `availableCurrencies: List<Currency>` - All supported currencies
  - `isLoading: StateFlow<Boolean>` - Loading state
  - `errorMessage: StateFlow<String?>` - Error messages
  - `apiKey: StateFlow<String>` - Current API key (mock: empty)
  - `apiBaseUrl: StateFlow<String>` - Current API base URL (mock: default URL)
  - `isApiConfigured: StateFlow<Boolean>` - Whether API is configured (mock: false)
  - `apiTestResult: StateFlow<String?>` - Result of API connection test
- **Methods (Mock Implementation):**
  - `loadSettings()` - Load mock settings
  - `updateBaseCurrency(currency: Currency)` - Update local state only (mock)
  - `updateApiKey(apiKey: String)` - Update local state only (mock)
  - `updateApiBaseUrl(baseUrl: String)` - Update local state only (mock)
  - `testApiConnection()` - Show toast/log (mock)
  - `init()` - Initialize ViewModel with mock data
- **Pattern:** Follow ExpenseHistoryViewModel pattern
- **Note:** Will be updated to use SettingsRepository in later task

---

### Task 3: Settings Storage Infrastructure

**Priority:** High  
**Estimated Complexity:** Medium  
**Dependencies:** None

#### Subtask 1.1: Create Settings Data Model

- **File:** `commonMain/kotlin/com/example/expensetracker/model/AppSettings.kt`
- **Description:** Create data class to represent app settings
- **Fields:**
  - `baseCurrency: Currency` (default: USD)
  - `lastExchangeRateUpdate: LocalDateTime?` (nullable)
  - `exchangeRateApiKey: String` (default: empty, user must configure)
  - `exchangeRateApiBaseUrl: String` (default: "https://v6.exchangerate-api.com/v6", editable)
- **Note:** Refresh interval is fixed at 24 hours (once per day) - not configurable
- **Considerations:**
  - Use existing `Currency` enum
  - Use `kotlinx.datetime.LocalDateTime` for consistency
  - API key should be stored securely (consider encryption for production)
  - API base URL allows switching providers if needed

#### Subtask 1.2: Create Settings Entity for Database

- **File:** `commonMain/kotlin/com/example/expensetracker/data/database/SettingsEntity.kt`
- **Description:** Room entity for storing settings
- **Structure:**
  - Single row table (settings table)
  - Primary key: `id: String = "settings"`
  - Fields:
    - `baseCurrency: String`
    - `lastExchangeRateUpdate: String?`
    - `exchangeRateApiKey: String` (default: empty)
    - `exchangeRateApiBaseUrl: String` (default: "https://v6.exchangerate-api.com/v6")
- **Note:** Refresh interval is fixed at 24 hours - not stored in database
- **Type Converters:** Reuse existing `Converters` class for Currency and LocalDateTime
- **Security Note:** API key is stored in plain text in database. For production, consider encryption.

#### Subtask 1.3: Create Settings DAO

- **File:** `commonMain/kotlin/com/example/expensetracker/data/database/SettingsDao.kt`
- **Description:** Data Access Object for settings operations
- **Methods:**
  - `getSettings(): Flow<SettingsEntity?>` - Observe settings changes
  - `getSettingsSync(): suspend SettingsEntity?` - Get settings synchronously
  - `insertOrUpdateSettings(settings: SettingsEntity): suspend Unit` - Save settings
  - `updateBaseCurrency(currency: String): suspend Unit` - Update base currency only
  - `updateLastExchangeRateUpdate(timestamp: String): suspend Unit` - Update refresh timestamp
  - `updateApiKey(apiKey: String): suspend Unit` - Update API key
  - `updateApiBaseUrl(baseUrl: String): suspend Unit` - Update API base URL
- **Note:** Refresh interval is fixed at 24 hours (not stored or configurable)

#### Subtask 1.4: Update ExpenseDatabase

- **File:** `commonMain/kotlin/com/example/expensetracker/data/database/ExpenseDatabase.kt`
- **Description:** Add SettingsEntity to database
- **Changes:**
  - Add `SettingsEntity::class` to entities array
  - Increment database version (1 → 2)
  - Add `settingsDao(): SettingsDao` abstract method
  - Create migration from version 1 to 2
- **Migration Strategy:**
  - Create new `settings` table
  - Insert default settings row (baseCurrency = "USD")
  - See [database/IMPLEMENTATION.md](../database/IMPLEMENTATION.md) for migration pattern

#### Subtask 1.5: Create Settings Repository

- **File:** `commonMain/kotlin/com/example/expensetracker/data/repository/SettingsRepository.kt`
- **Description:** Repository for settings management (singleton pattern, similar to ExpenseRepository)
- **Methods:**
  - `getBaseCurrency(): Flow<Currency>` - Observe base currency
  - `getBaseCurrencySync(): suspend Currency` - Get base currency synchronously
  - `setBaseCurrency(currency: Currency): suspend Unit` - Update base currency
  - `getLastExchangeRateUpdate(): Flow<LocalDateTime?>` - Observe last update time
  - `updateLastExchangeRateUpdate(timestamp: LocalDateTime): suspend Unit` - Update timestamp
  - `getSettings(): Flow<AppSettings>` - Get all settings
  - `getApiKey(): Flow<String>` - Get API key
  - `getApiKeySync(): suspend String` - Get API key synchronously
  - `setApiKey(apiKey: String): suspend Unit` - Update API key
  - `getApiBaseUrl(): Flow<String>` - Get API base URL
  - `getApiBaseUrlSync(): suspend String` - Get API base URL synchronously
  - `setApiBaseUrl(baseUrl: String): suspend Unit` - Update API base URL
  - `isApiConfigured(): suspend Boolean` - Check if API key is set
- **Pattern:** Follow ExpenseRepository singleton pattern
- **Initialization:** Default base currency = USD, default API URL = "https://v6.exchangerate-api.com/v6" if no settings exist

---

### Task 4: HTTP Client Setup (Ktor)

**Priority:** High  
**Estimated Complexity:** Medium  
**Dependencies:** None

#### Subtask 2.1: Add Ktor Dependencies

- **File:** `gradle/libs.versions.toml`
- **Description:** Add Ktor client dependencies for KMP
- **Dependencies to Add:**
  ```toml
  ktor-client-core = "3.0.0"
  ktor-client-content-negotiation = "3.0.0"
  ktor-serialization-kotlinx-json = "3.0.0"
  kotlinx-serialization-json = "1.7.0"
  ```
- **Platform-Specific:**
  - Android: `ktor-client-android`
  - iOS: `ktor-client-darwin`
- **Note:** Use Ktor 3.0.0+ for best KMP support

#### Subtask 2.2: Create Ktor Client Factory

- **File:** `commonMain/kotlin/com/example/expensetracker/data/network/KtorClientFactory.kt`
- **Description:** Factory for creating platform-specific Ktor clients
- **Pattern:** Use `expect/actual` pattern for platform-specific engines
- **Common Interface:**
  ```kotlin
  expect fun createHttpClient(): HttpClient
  ```
- **Android Implementation:**
  - File: `androidMain/kotlin/com/example/expensetracker/data/network/KtorClientFactory.android.kt`
  - Use `Android` engine
- **iOS Implementation:**
  - File: `iosMain/kotlin/com/example/expensetracker/data/network/KtorClientFactory.ios.kt`
  - Use `Darwin` engine
- **Configuration:**
  - JSON content negotiation
  - Timeout: 30 seconds
  - Base URL: Configurable (default: `https://v6.exchangerate-api.com/v6`)
  - Base URL should be injected from SettingsRepository

#### Subtask 2.3: Create Exchange Rate API Models

- **File:** `commonMain/kotlin/com/example/expensetracker/data/network/model/ExchangeRateResponse.kt`
- **Description:** Data classes for API responses from exchangerate-api.com
- **Models:**
  - `ExchangeRateResponse` - Main response from exchangerate-api.com
    - `result: String` - "success" or error code
    - `base_code: String` - Base currency code
    - `time_last_update_utc: String` - Last update timestamp (ISO format)
    - `conversion_rates: Map<String, Double>` - Currency code to rate mapping (all rates relative to base)
  - `ErrorResponse` - Error response structure (optional)
    - `result: String` - Error code
    - `error-type: String` - Error type description
- **Serialization:** Use `@Serializable` from kotlinx-serialization
- **Property Names:** Use `@SerialName` to map JSON field names (e.g., `base_code`, `conversion_rates`)
- **API Reference:** https://www.exchangerate-api.com/docs/standard-requests

---

### Task 5: Exchange Rate API Integration

**Priority:** High  
**Estimated Complexity:** Medium  
**Dependencies:** Task 2

#### Subtask 3.1: Create Exchange Rate API Service

- **File:** `commonMain/kotlin/com/example/expensetracker/data/network/ExchangeRateApiService.kt`
- **Description:** Service interface and implementation for exchangerate-api.com
- **Interface Methods:**
  - `getLatestRates(apiKey: String, baseCurrency: String, baseUrl: String): Result<ExchangeRateResponse>` - Get latest rates
  - **Note:** Historical rates not included in initial implementation (can be added later)
- **Implementation:**
  - Use Ktor client from factory
  - Endpoint: `GET {baseUrl}/{apiKey}/latest/{baseCurrency}`
  - Example: `https://v6.exchangerate-api.com/v6/YOUR-API-KEY/latest/USD`
  - Error handling: Wrap in `Result` type for safe error handling
  - Network errors: Return `Result.failure(Exception)`
  - **Key Optimization:** This single call returns ALL rates for the base currency, allowing cross-rate calculations
- **Error Handling:**
  - Network timeouts
  - Invalid API key (check `result` field in response)
  - Invalid responses
  - API errors (non-200 status codes)
  - Rate limit exceeded
- **Response Validation:**
  - Check `result == "success"` before using rates
  - Handle `error-type` field if present

#### Subtask 3.2: Create Exchange Rate Entity

- **File:** `commonMain/kotlin/com/example/expensetracker/data/database/ExchangeRateEntity.kt`
- **Description:** Room entity for caching exchange rates
- **Structure:**
  - `id: String` (primary key) - Format: "{baseCurrency}_{targetCurrency}_{date}" (e.g., "USD_EUR_2024-11-01")
  - `baseCurrency: String` - Base currency code
  - `targetCurrency: String` - Target currency code
  - `rate: Double` - Exchange rate (from baseCurrency to targetCurrency)
  - `date: String` - Date of the rate (ISO format: YYYY-MM-DD)
  - `lastUpdated: String` - Timestamp when cached (LocalDateTime as String)
- **Indexes:**
  - Add index on `(baseCurrency, date)` for fast lookups
  - Add index on `(baseCurrency, targetCurrency, date)` for specific rate queries
- **Optimization Note:** Store all rates for a base currency in a single API call, then calculate cross-rates as needed

#### Subtask 3.3: Create Exchange Rate DAO

- **File:** `commonMain/kotlin/com/example/expensetracker/data/database/ExchangeRateDao.kt`
- **Description:** Data Access Object for exchange rate cache
- **Methods:**
  - `getRate(baseCurrency: String, targetCurrency: String, date: String?): suspend ExchangeRateEntity?` - Get specific rate
  - `getLatestRates(baseCurrency: String): Flow<List<ExchangeRateEntity>>` - Get all latest rates for base currency
  - `insertOrUpdateRate(rate: ExchangeRateEntity): suspend Unit` - Save/update rate
  - `insertOrUpdateRates(rates: List<ExchangeRateEntity>): suspend Unit` - Bulk insert
  - `deleteOldRates(olderThan: String): suspend Unit` - Cleanup old rates (older than 30 days)
  - `getRateCount(): suspend Int` - Count cached rates

#### Subtask 3.4: Update ExpenseDatabase for Exchange Rates

- **File:** `commonMain/kotlin/com/example/expensetracker/data/database/ExpenseDatabase.kt`
- **Description:** Add ExchangeRateEntity to database
- **Changes:**
  - Add `ExchangeRateEntity::class` to entities array
  - Increment database version (2 → 3)
  - Add `exchangeRateDao(): ExchangeRateDao` abstract method
  - Create migration from version 2 to 3
- **Migration:** Create `exchange_rates` table with indexes

#### Subtask 3.5: Create Exchange Rate Repository

- **File:** `commonMain/kotlin/com/example/expensetracker/data/repository/ExchangeRateRepository.kt`
- **Description:** Repository for exchange rate management
- **Dependencies:** Inject `SettingsRepository` to get API key and base URL
- **Methods:**
  - `getExchangeRate(baseCurrency: Currency, targetCurrency: Currency, date: LocalDateTime? = null): Flow<Double?>` - Get rate (uses cross-rate calculation)
  - `getExchangeRateSync(baseCurrency: Currency, targetCurrency: Currency, date: LocalDateTime? = null): suspend Double?` - Synchronous version
  - `refreshExchangeRates(baseCurrency: Currency): suspend Result<Unit>` - Fetch and cache ALL rates for base currency (single API call)
  - `isRateStale(baseCurrency: Currency): suspend Boolean` - Check if rates need refresh
  - `clearOldRates(): suspend Unit` - Cleanup old cached rates
  - `getAllRatesForBase(baseCurrency: Currency, date: LocalDateTime? = null): suspend Map<Currency, Double>` - Get all cached rates for a base currency
- **Caching Strategy:**
  - Check cache first (by date and base currency)
  - If cache miss or stale (>24 hours), fetch from API (gets ALL rates in one call)
  - Store ALL fetched rates in database (one row per currency pair)
  - Return cached rate if available, even if slightly stale (offline fallback)
- **Cross-Rate Calculation:**
  - If direct rate not found, calculate using base currency rates
  - Formula: `rate(A→B) = rate(base→B) / rate(base→A)`
  - Example: If we have USD→EUR and USD→GBP, calculate EUR→GBP = (USD→GBP) / (USD→EUR)

---

### Task 6: Currency Conversion Service

**Priority:** High  
**Estimated Complexity:** Low  
**Dependencies:** Task 3, Task 5

#### Subtask 6.1: Create Currency Converter

- **File:** `commonMain/kotlin/com/example/expensetracker/domain/CurrencyConverter.kt`
- **Description:** Business logic for currency conversion with cross-rate optimization
- **Class:** `CurrencyConverter` (inject ExchangeRateRepository and SettingsRepository)
- **Methods:**
  - `convertToBaseCurrency(expense: Expense): Flow<Expense>` - Convert expense to base currency
  - `convertToBaseCurrencySync(expense: Expense): suspend Expense` - Synchronous conversion
  - `convertAmount(amount: Double, fromCurrency: Currency, toCurrency: Currency, date: LocalDateTime? = null): Flow<Double?>` - Convert amount
  - `convertAmountSync(amount: Double, fromCurrency: Currency, toCurrency: Currency, date: LocalDateTime? = null): suspend Double?` - Synchronous amount conversion
- **Optimized Conversion Logic:**
  - If `fromCurrency == toCurrency`, return amount as-is
  - Get base currency from SettingsRepository
  - **Direct Rate:** If we have cached rate for `fromCurrency → toCurrency`, use it
  - **Via Base Currency (Optimized):**
    - Get rate `baseCurrency → fromCurrency` (rate1)
    - Get rate `baseCurrency → toCurrency` (rate2)
    - Calculate: `amount * (rate2 / rate1)`
    - Example: Convert 100 EUR to GBP
      - Get USD→EUR = 0.85, USD→GBP = 0.73
      - Calculate: 100 \* (0.73 / 0.85) = 85.88 GBP
  - **Fallback:** If base currency rates unavailable, try direct rate lookup
- **Key Optimization:**
  - Single API call fetches ALL rates for base currency
  - All conversions calculated from those rates (no additional API calls)
  - Dramatically reduces API usage
- **Error Handling:**
  - Return `null` if rate unavailable
  - Log errors for debugging
  - Handle division by zero (if rate1 is 0)

#### Subtask 6.2: Add Converted Amount to Expense Model (Optional)

- **File:** `commonMain/kotlin/com/example/expensetracker/model/Expense.kt`
- **Description:** Add helper method for converted amount
- **Changes:**
  - Add extension function: `fun Expense.getConvertedAmount(baseCurrency: Currency, converter: CurrencyConverter): Flow<Double?>`
  - Keep original amount unchanged (preserve original currency)
- **Note:** Don't modify existing Expense structure, use extension functions

---

### Task 7: Wire Settings UI to Backend

**Priority:** High  
**Estimated Complexity:** Low  
**Dependencies:** Task 2, Task 3

#### Subtask 7.1: Update Settings ViewModel to Use Repository

- **File:** `commonMain/kotlin/com/example/expensetracker/viewmodel/SettingsViewModel.kt`
- **Description:** Replace mock implementation with real SettingsRepository integration
- **Changes:**
  - Inject `SettingsRepository` instead of using mock data
  - Update `loadSettings()` to use `SettingsRepository.getSettings()`
  - Update `updateBaseCurrency()` to call `SettingsRepository.setBaseCurrency()`
  - Update `updateApiKey()` to call `SettingsRepository.setApiKey()`
  - Update `updateApiBaseUrl()` to call `SettingsRepository.setApiBaseUrl()`
  - Update `testApiConnection()` to use real API service (from Task 5)
  - Observe settings changes via Flow from repository
- **State Updates:**
  - Replace mock StateFlows with real data from repository
  - Handle loading states during API operations
  - Show error messages from actual API calls

#### Subtask 7.2: Wire Settings Screen to ViewModel

- **File:** `commonMain/kotlin/com/example/expensetracker/view/SettingsScreen.kt`
- **Description:** Connect SettingsScreen UI to real ViewModel
- **Changes:**
  - Replace local state with ViewModel state
  - Connect all inputs to ViewModel methods
  - Show real loading states and error messages
  - Display actual settings data from repository

#### Subtask 7.3: Update App Navigation

- **File:** `commonMain/kotlin/com/example/expensetracker/App.kt`
- **Description:** Replace BlankScreen("Settings") with SettingsScreen
- **Changes:**
  - Import SettingsScreen
  - Replace `AppScreen.SETTINGS -> BlankScreen("Settings")` with `AppScreen.SETTINGS -> SettingsScreen()`

---

### Task 8: Wire Currency Exchange UI to Backend

**Priority:** High  
**Estimated Complexity:** Low  
**Dependencies:** Task 1, Task 6

#### Subtask 8.1: Wire Banner to Navigation

- **File:** `commonMain/kotlin/com/example/expensetracker/view/ExpenseHistoryScreen.kt`
- **Description:** Connect banner "Open" button to actual navigation
- **Changes:**
  - Replace mock callback with real navigation to CurrencyExchangeScreen
  - Implement proper navigation (bottom sheet, dialog, or full screen)
  - Pass necessary parameters (ViewModel, etc.) to CurrencyExchangeScreen

#### Subtask 8.2: Wire Currency Exchange Screen to Backend

- **File:** `commonMain/kotlin/com/example/expensetracker/view/CurrencyExchangeScreen.kt`
- **Description:** Connect CurrencyExchangeScreen to real data and services
- **Changes:**
  - Create or update CurrencyExchangeViewModel with real dependencies
  - Inject `CurrencyConverter` and `SettingsRepository`
  - Replace mock data with real expense data from ExpenseRepository
  - Replace mock rates with real exchange rates from ExchangeRateRepository
  - Connect "Refresh Rates" button to actual refresh functionality
  - Show real last update timestamp from SettingsRepository
  - Display actual converted amounts using CurrencyConverter
- **State Management:**
  - Replace placeholder ViewModel/local state with real ViewModel
  - Handle loading states during rate refresh
  - Show error messages from API calls

---

### Task 9: Currency Conversion on Display

**Priority:** High  
**Estimated Complexity:** Medium  
**Dependencies:** Task 6, Task 8

#### Subtask 9.1: Update ExpenseHistoryViewModel

- **File:** `commonMain/kotlin/com/example/expensetracker/viewmodel/ExpenseHistoryViewModel.kt`
- **Description:** Add currency conversion to expense display
- **Changes:**
  - Inject `CurrencyConverter` and `SettingsRepository`
  - Add `convertedExpenses: StateFlow<List<ExpenseWithConversion>>` - Expenses with converted amounts
  - Add `showConvertedAmounts: StateFlow<Boolean>` - Toggle for showing conversions
- **Data Class:** `ExpenseWithConversion`
  - `expense: Expense` - Original expense
  - `convertedAmount: Double?` - Converted amount (nullable if conversion fails)
  - `baseCurrency: Currency` - Base currency used
- **Logic:**
  - Observe base currency from SettingsRepository
  - Convert each expense when base currency changes
  - Handle null conversions gracefully (show original amount)

#### Subtask 9.2: Update Expense Display Components

- **File:** `commonMain/kotlin/com/example/expensetracker/view/components/SwipeableExpenseItem.kt`
- **Description:** Show converted amount alongside original
- **Changes:**
  - Accept `convertedAmount: Double?` and `baseCurrency: Currency?` as parameters
  - Display format: "€25.99 (≈ $28.50)" when converted amount available
  - Show only original amount if conversion unavailable
  - Use muted color for converted amount
- **UI Design:**
  - Original amount: Primary text, larger
  - Converted amount: Secondary text, smaller, muted color
  - Format: `{original} (≈ {converted})`

#### Subtask 9.3: Update ExpenseHistoryScreen

- **File:** `commonMain/kotlin/com/example/expensetracker/view/ExpenseHistoryScreen.kt`
- **Description:** Pass converted amounts to expense items
- **Changes:**
  - Use `convertedExpenses` from ViewModel
  - Pass `convertedAmount` and `baseCurrency` to SwipeableExpenseItem
  - Add toggle button to show/hide conversions (optional)

---

### Task 10: Currency Conversion on Save

**Priority:** Medium  
**Estimated Complexity:** Low  
**Dependencies:** Task 6, Task 8

#### Subtask 10.1: Update Add/Edit Expense Flow

- **Files:**
  - `commonMain/kotlin/com/example/expensetracker/viewmodel/AddExpenseViewModel.kt` (to be created)
  - `commonMain/kotlin/com/example/expensetracker/view/AddExpenseScreen.kt` (to be created)
- **Description:** When saving expense, optionally show converted amount preview
- **Implementation:**
  - In expense form, show preview: "This will be ≈ $X.XX in your base currency"
  - Update preview when currency or amount changes
  - Use CurrencyConverter to get converted amount
- **Note:** This task depends on Add Expense screen implementation (may be future work)

#### Subtask 10.2: Update ExpenseRepository (Optional Enhancement)

- **File:** `commonMain/kotlin/com/example/expensetracker/data/repository/ExpenseRepository.kt`
- **Description:** Add method to get expenses with conversions
- **Method:**
  - `getAllExpensesWithConversion(baseCurrency: Currency, converter: CurrencyConverter): Flow<List<ExpenseWithConversion>>`
- **Note:** This is optional - conversion can be done in ViewModel instead

---

### Task 11: Background Exchange Rate Refresh

**Priority:** Medium  
**Estimated Complexity:** High  
**Dependencies:** Task 5

#### Subtask 11.1: Create Exchange Rate Refresh Worker

- **File:** `commonMain/kotlin/com/example/expensetracker/data/worker/ExchangeRateRefreshWorker.kt`
- **Description:** Background worker to refresh exchange rates
- **Platform-Specific:**
  - **Android:** Use WorkManager (Android-specific)
    - File: `androidMain/kotlin/com/example/expensetracker/data/worker/ExchangeRateRefreshWorker.android.kt`
    - Periodic work: Every 24 hours (fixed, not configurable)
    - Constraints: Network required, charging not required
  - **iOS:** Use background tasks (iOS-specific)
    - File: `iosMain/kotlin/com/example/expensetracker/data/worker/ExchangeRateRefreshWorker.ios.kt`
    - Background fetch: Every 24 hours (fixed, not configurable)
    - Note: iOS background tasks are limited
- **Common Interface:**
  - `expect fun scheduleExchangeRateRefresh()`
  - `expect fun cancelExchangeRateRefresh()`
- **Logic:**
  - Get base currency from SettingsRepository
  - Call ExchangeRateRepository.refreshExchangeRates()
  - Update last refresh timestamp
  - Handle errors gracefully (don't crash on network failure)

#### Subtask 11.2: Initialize Background Worker

- **File:** `androidMain/kotlin/com/example/expensetracker/MainActivity.kt`
- **Description:** Schedule background refresh on app start
- **Changes:**
  - Call `scheduleExchangeRateRefresh()` in `onCreate()`
  - Only schedule if not already scheduled
- **iOS:** Initialize in `iosApp/iOSApp.swift` or equivalent

#### Subtask 11.3: Manual Refresh Trigger

- **File:** `commonMain/kotlin/com/example/expensetracker/viewmodel/SettingsViewModel.kt`
- **Description:** Add manual refresh functionality
- **Method:**
  - `refreshExchangeRatesNow(): suspend Unit`
  - Show loading state during refresh
  - Show success/error message
- **UI:** Connect to "Refresh Now" button in SettingsScreen

---

### Task 12: Offline Fallback

**Priority:** High  
**Estimated Complexity:** Low  
**Dependencies:** Task 5

#### Subtask 12.1: Enhance Exchange Rate Repository

- **File:** `commonMain/kotlin/com/example/expensetracker/data/repository/ExchangeRateRepository.kt`
- **Description:** Improve offline fallback logic
- **Changes:**
  - `getExchangeRate()` should always check cache first
  - If cache hit (even if stale), return cached rate
  - Only return `null` if no cache exists at all
  - Log warning when using stale rates
- **Stale Rate Handling:**
  - Consider rates < 7 days old as acceptable for offline use
  - Rates > 7 days old: Still return but log warning
  - No rates: Return null (show original currency)

#### Subtask 12.2: Update Currency Converter for Offline

- **File:** `commonMain/kotlin/com/example/expensetracker/domain/CurrencyConverter.kt`
- **Description:** Handle offline scenarios gracefully
- **Changes:**
  - When rate is null, return original amount with original currency
  - Don't fail conversion - gracefully degrade
  - Log when using cached/stale rates

#### Subtask 12.3: UI Feedback for Offline Mode

- **File:** `commonMain/kotlin/com/example/expensetracker/view/components/SwipeableExpenseItem.kt`
- **Description:** Show indicator when using cached rates
- **Changes:**
  - Add small icon/tooltip: "Using cached rate" when rate is stale
  - Show warning if rates are very old (>7 days)
- **Optional:** Add refresh button in expense list header

---

### Task 13: Testing & Error Handling

**Priority:** Medium  
**Estimated Complexity:** Medium  
**Dependencies:** All previous tasks

#### Subtask 13.1: Unit Tests for Currency Converter

- **File:** `commonTest/kotlin/com/example/expensetracker/domain/CurrencyConverterTest.kt`
- **Description:** Test conversion logic
- **Test Cases:**
  - Same currency conversion (should return same amount)
  - Direct conversion (USD to EUR)
  - Inverse conversion (EUR to USD)
  - Via base currency conversion (GBP to JPY via USD)
  - Null rate handling
  - Invalid currency handling

#### Subtask 13.2: Unit Tests for Exchange Rate Repository

- **File:** `commonTest/kotlin/com/example/expensetracker/data/repository/ExchangeRateRepositoryTest.kt`
- **Description:** Test repository logic
- **Test Cases:**
  - Cache hit scenarios
  - Cache miss scenarios
  - Stale rate detection
  - API error handling
  - Offline fallback

#### Subtask 13.3: Integration Tests

- **File:** `androidTest/kotlin/com/example/expensetracker/integration/CurrencyExchangeIntegrationTest.kt`
- **Description:** End-to-end tests
- **Test Cases:**
  - Full flow: API fetch → Cache → Display
  - Offline scenario: Cache only
  - Settings change: Base currency update triggers re-conversion

#### Subtask 13.4: Error Handling & User Feedback

- **Files:** All ViewModels and UI components
- **Description:** Comprehensive error handling
- **Error Scenarios:**
  - Network unavailable: Show cached rates with indicator
  - API error: Show error message, use cached rates
  - Invalid currency: Fallback to original amount
  - Database error: Log and show user-friendly message
- **User Feedback:**
  - Loading indicators during API calls
  - Success messages after refresh
  - Error toasts/snackbars
  - Offline indicators

---

## Implementation Order

### Phase 1: UI Components (Week 1)

1. ✅ Task 1: UI Components (Currency Exchange Banner)
   - Create banner component
   - Add to ExpenseHistoryScreen
   - Create Currency Exchange Screen/Dialog UI (mock data)
2. ✅ Task 2: Settings UI Implementation
   - Create Settings Screen UI
   - Create Settings ViewModel (placeholder with mock data)
   - Update App Navigation

### Phase 2: Backend Infrastructure (Week 2)

3. ✅ Task 3: Settings Storage Infrastructure
4. ✅ Task 4: HTTP Client Setup (Ktor)
5. ✅ Task 5: Exchange Rate API Integration
6. ✅ Task 6: Currency Conversion Service

### Phase 3: Wire UI to Backend (Week 3)

7. ✅ Task 7: Wire Settings UI to Backend
   - Update SettingsViewModel to use SettingsRepository
   - Connect SettingsScreen to real data
8. ✅ Task 8: Wire Currency Exchange UI to Backend
   - Wire banner to navigation
   - Connect CurrencyExchangeScreen to real data and services
9. ✅ Task 9: Currency Conversion on Display
   - Show converted amounts in expense list

### Phase 4: Polish & Optimization (Week 4)

10. ✅ Task 10: Currency Conversion on Save (if Add Expense screen exists)
11. ✅ Task 11: Background Exchange Rate Refresh (fixed at 24 hours)
12. ✅ Task 12: Offline Fallback
13. ✅ Task 13: Testing & Error Handling

---

## Technical Decisions

### HTTP Library: Ktor

**Rationale:**

- ✅ Full Kotlin Multiplatform support
- ✅ Works on both Android and iOS
- ✅ Modern, coroutine-based API
- ✅ Excellent serialization support
- ✅ Easy to test and mock

### API Provider: exchangerate-api.com

**Rationale:**

- ✅ Free tier available (1,500 requests/month)
- ✅ Simple REST API
- ✅ Returns ALL rates for base currency in single call (optimization)
- ✅ Good documentation
- ✅ Configurable API key and base URL (allows switching providers)
- ✅ Supports cross-rate calculations

**API Key Management:**

- User must configure API key in settings
- API key stored in database (consider encryption for production)
- Base URL is editable (allows switching to alternative providers if needed)

**Free Tier Limits:**

- 1,500 requests/month
- **Optimization:** Single API call gets all rates, then calculate cross-rates locally
- This means we only need 1 API call per day (or per base currency change)
- With 1 call/day, free tier supports ~50 days of usage

### Caching Strategy

- **Cache Duration:** 24 hours (fixed, not configurable)
- **Refresh Interval:** Once per day (24 hours) - fixed
- **Stale Rate Tolerance:** 7 days for offline use
- **Cleanup:** Remove rates older than 30 days
- **Storage:** Room database (persistent, works offline)

### Conversion Strategy (Optimized with Cross-Rates)

- **Single API Call:** Fetch ALL rates for base currency (e.g., USD → all currencies)
- **Direct Conversion:** If both currencies have rates from base, calculate directly
  - Formula: `rate(A→B) = rate(base→B) / rate(base→A)`
- **Via Base Currency:** Always use base currency rates (stored in cache)
  - Example: EUR → GBP = (USD → GBP) / (USD → EUR)
- **Fallback:** If base currency rates unavailable, show original amount
- **Optimization:** No need for multiple API calls - all conversions calculated from one fetch

---

## Database Schema Changes

### Version 2: Settings Table

```sql
CREATE TABLE settings (
    id TEXT PRIMARY KEY,
    baseCurrency TEXT NOT NULL,
    lastExchangeRateUpdate TEXT,
    exchangeRateApiKey TEXT NOT NULL DEFAULT '',
    exchangeRateApiBaseUrl TEXT NOT NULL DEFAULT 'https://v6.exchangerate-api.com/v6'
);
```

### Version 3: Exchange Rates Table

```sql
CREATE TABLE exchange_rates (
    id TEXT PRIMARY KEY,
    baseCurrency TEXT NOT NULL,
    targetCurrency TEXT NOT NULL,
    rate REAL NOT NULL,
    date TEXT NOT NULL,
    lastUpdated TEXT NOT NULL
);

CREATE INDEX idx_exchange_rates_base_date
ON exchange_rates(baseCurrency, date);
```

---

## Dependencies to Add

### gradle/libs.versions.toml

```toml
[versions]
ktor = "3.0.0"
kotlinx-serialization-json = "1.7.0"

[libraries]
# Ktor Client
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization-json" }

# Platform-specific
ktor-client-android = { module = "io.ktor:ktor-client-android", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
```

### composeApp/build.gradle.kts

Add to `commonMain.dependencies`:

```kotlin
implementation(libs.ktor.client.core)
implementation(libs.ktor.client.content.negotiation)
implementation(libs.ktor.serialization.kotlinx.json)
implementation(libs.kotlinx.serialization.json)
```

Add to `androidMain.dependencies`:

```kotlin
implementation(libs.ktor.client.android)
```

Add to `iosMain.dependencies`:

```kotlin
implementation(libs.ktor.client.darwin)
```

---

## API Integration Details

### Endpoint: Latest Rates

- **URL:** `https://v6.exchangerate-api.com/v6/{API_KEY}/latest/{BASE_CURRENCY}`
- **Method:** GET
- **Example:** `https://v6.exchangerate-api.com/v6/YOUR-API-KEY/latest/USD`
- **Parameters:**
  - `{API_KEY}`: User's API key (from settings)
  - `{BASE_CURRENCY}`: Base currency code (e.g., "USD")
- **Response:**
  ```json
  {
    "result": "success",
    "base_code": "USD",
    "time_last_update_utc": "2024-11-01T12:00:00+00:00",
    "conversion_rates": {
      "USD": 1.0,
      "EUR": 0.85,
      "GBP": 0.73,
      "JPY": 110.0,
      "CHF": 0.92,
      "CAD": 1.35
      // ... all supported currencies
    }
  }
  ```
- **Error Response:**
  ```json
  {
    "result": "error",
    "error-type": "invalid-key"
  }
  ```

### Cross-Rate Calculation Optimization

**Key Insight:** One API call returns ALL rates for the base currency. We can then calculate any currency pair conversion without additional API calls.

**Example:**

1. API call: `GET /v6/API_KEY/latest/USD` returns:

   - USD → EUR: 0.85
   - USD → GBP: 0.73
   - USD → JPY: 110.0
   - ... (all currencies)

2. To convert EUR → GBP:

   - Get USD → EUR = 0.85
   - Get USD → GBP = 0.73
   - Calculate: EUR → GBP = (USD → GBP) / (USD → EUR) = 0.73 / 0.85 = 0.859

3. To convert GBP → JPY:
   - Get USD → GBP = 0.73
   - Get USD → JPY = 110.0
   - Calculate: GBP → JPY = (USD → JPY) / (USD → GBP) = 110.0 / 0.73 = 150.68

**Benefits:**

- ✅ Single API call per day (or per base currency change)
- ✅ Calculate unlimited currency pairs locally
- ✅ Dramatically reduces API usage
- ✅ Works offline with cached rates

### Rate Limiting

- **Free Tier:** 1,500 requests/month
- **Strategy:**
  - Cache aggressively (24 hours)
  - Refresh once per day (or when base currency changes)
  - Calculate all conversions from cached rates
  - With 1 call/day, free tier supports ~50 days
- **Fallback:** Use cached rates if limit exceeded or offline

---

## Future Enhancements (Out of Scope)

1. **Historical Rate Charts:** Show exchange rate trends
2. **Multiple Base Currencies:** Support for multiple base currencies
3. **Custom Exchange Rate Sources:** Allow users to configure API
4. **Rate Alerts:** Notify when rates change significantly
5. **Batch Conversion:** Convert multiple expenses at once
6. **Export with Conversions:** Include converted amounts in exports

---

## Success Criteria

✅ **Functional Requirements:**

- User can set base currency in settings
- Exchange rates are fetched from API
- Expenses are converted to base currency on display
- Offline mode works with cached rates
- Background refresh updates rates automatically

✅ **Non-Functional Requirements:**

- App works offline (with cached rates)
- API calls don't block UI
- Error handling is graceful
- No crashes on network errors
- Performance: Conversion happens in <100ms

✅ **User Experience:**

- Settings are intuitive
- Converted amounts are clearly displayed
- Offline indicators are visible
- Error messages are user-friendly

---

## Notes

- **iOS Compatibility:** All common code is KMP-compatible. Platform-specific code (WorkManager, background tasks) will be implemented separately for iOS when needed.
- **Testing:** Focus on Android initially, but ensure code structure supports iOS testing later.
- **Migration:** Database migrations must be tested thoroughly to avoid data loss.
- **API Changes:** If exchangerate-api.com API changes, update models and service accordingly.
- **API Key Security:** API key is stored in database. For production, consider encryption or Android Keystore.
- **Cross-Rate Optimization:** Single API call fetches all rates for base currency. All other conversions calculated locally from cached rates.

---

## References

- [Room KMP Documentation](https://developer.android.com/kotlin/multiplatform/room)
- [Ktor Client Documentation](https://ktor.io/docs/client.html)
- [exchangerate-api.com API Docs](https://www.exchangerate-api.com/docs/standard-requests)
- [Kotlin Flow Documentation](https://kotlinlang.org/docs/flow.html)
- [Project Database Implementation](../database/IMPLEMENTATION.md)

---

**Document Version:** 1.1  
**Last Updated:** 2024-11-01  
**Status:** Specification Complete - Ready for Implementation

## Recent Updates (v1.1)

### API Provider Change

- ✅ Switched from `exchangerate.host` to `exchangerate-api.com`
- ✅ API key and base URL are now configurable in settings
- ✅ Follows [exchangerate-api.com documentation](https://www.exchangerate-api.com/docs/standard-requests)

### Cross-Rate Calculation Optimization

- ✅ **Key Optimization:** Single API call fetches ALL rates for base currency
- ✅ All currency pair conversions calculated locally from cached rates
- ✅ Dramatically reduces API usage (1 call/day instead of multiple calls)
- ✅ Free tier (1,500 requests/month) supports ~50 days with 1 call/day
- ✅ Formula: `rate(A→B) = rate(base→B) / rate(base→A)`

### Settings Enhancements

- ✅ Added API key configuration field
- ✅ Added API base URL configuration (editable, allows switching providers)
- ✅ Added API connection test functionality
- ✅ Settings UI includes API configuration section
- ✅ Refresh interval fixed at 24 hours (once per day, not configurable)

### UI Integration

- ✅ Added Currency Exchange Banner component for Expense History screen
- ✅ Banner placed below header, above expense list
- ✅ Gradient design with exchange icon and "Open" button
- ✅ Bottom navigation bar remains visible (banner in scrollable content)
- ✅ **UI-First Approach:** UI components created first with mock data, then wired to backend
- ✅ Settings Screen UI created before backend implementation
- ✅ Currency Exchange Screen UI created before backend implementation
