# Room Database Implementation Guide

## Overview

This document provides comprehensive documentation for the Room database implementation in the Expense Tracker Kotlin Multiplatform application. The implementation uses Room KMP to provide a shared database solution for both Android and iOS platforms.

> **Quick Status Check:** See [STATUS.md](./STATUS.md) for current implementation status and platform support.

## Architecture

The database implementation follows the **Repository Pattern** and **MVVM Architecture**:

```
┌─────────────────┐
│   View Layer    │ (Composable UI)
└────────┬────────┘
         │
┌────────▼────────┐
│   ViewModel     │ (ExpenseHistoryViewModel)
└────────┬────────┘
         │
┌────────▼────────┐
│   Repository    │ (ExpenseRepository - Singleton)
└────────┬────────┘
         │
┌────────▼────────┐
│   Room DAO      │ (ExpenseDao)
└────────┬────────┘
         │
┌────────▼────────┐
│   Database      │ (ExpenseDatabase)
└─────────────────┘
```

## Project Structure

```
composeApp/src/
├── commonMain/kotlin/com/example/expensetracker/
│   ├── data/
│   │   ├── database/
│   │   │   ├── ExpenseDatabase.kt       # Room database definition
│   │   │   ├── ExpenseEntity.kt         # Database entity
│   │   │   ├── ExpenseDao.kt            # Data Access Object
│   │   │   ├── TypeConverters.kt        # Type converters
│   │   │   └── DatabaseBuilder.kt       # expect declarations
│   │   └── repository/
│   │       └── ExpenseRepository.kt     # Repository (Singleton)
│   ├── model/
│   │   ├── Expense.kt                   # Domain model
│   │   ├── ExpenseCategory.kt
│   │   └── Currency.kt
│   └── viewmodel/
│       └── ExpenseHistoryViewModel.kt   # ViewModel using repository
├── androidMain/kotlin/com/example/expensetracker/
│   ├── MainActivity.kt                  # Android context initialization
│   └── data/database/
│       └── DatabaseBuilder.android.kt   # Android implementation
└── iosMain/kotlin/com/example/expensetracker/
    └── data/database/
        └── DatabaseBuilder.ios.kt       # iOS implementation
```

## Components

### 1. ExpenseEntity

The database entity representing an expense in the Room database.

**Location:** `commonMain/kotlin/com/example/expensetracker/data/database/ExpenseEntity.kt`

```kotlin
@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val category: ExpenseCategory,
    val description: String,
    val amount: Double,
    val currency: Currency,
    val date: LocalDateTime
)
```

**Key Features:**

- Uses `String` as primary key for UUID support
- Includes extension functions to convert between `ExpenseEntity` and domain model `Expense`
- All fields are non-nullable for data integrity

### 2. Type Converters

Converts complex types to database-storable primitives.

**Location:** `commonMain/kotlin/com/example/expensetracker/data/database/TypeConverters.kt`

**Conversions:**

- `LocalDateTime` ↔ `String` (ISO-8601 format)
- `ExpenseCategory` ↔ `String` (enum name)
- `Currency` ↔ `String` (currency code)

### 3. ExpenseDao

Defines all database operations.

**Location:** `commonMain/kotlin/com/example/expensetracker/data/database/ExpenseDao.kt`

**Available Operations:**

| Method                            | Description                     | Return Type                      |
| --------------------------------- | ------------------------------- | -------------------------------- |
| `getAllExpenses()`                | Gets all expenses, newest first | `Flow<List<ExpenseEntity>>`      |
| `getExpenseById(id)`              | Gets single expense by ID       | `suspend fun` → `ExpenseEntity?` |
| `getExpensesByCategory(category)` | Filters by category             | `Flow<List<ExpenseEntity>>`      |
| `insertExpense(expense)`          | Insert/update single expense    | `suspend fun`                    |
| `insertExpenses(expenses)`        | Bulk insert (for seeding)       | `suspend fun`                    |
| `updateExpense(expense)`          | Update existing expense         | `suspend fun`                    |
| `deleteExpense(expense)`          | Delete expense                  | `suspend fun`                    |
| `deleteExpenseById(id)`           | Delete by ID                    | `suspend fun`                    |
| `getExpenseCount()`               | Count all expenses              | `suspend fun` → `Int`            |
| `getExpensesByDateRange()`        | Filter by date range            | `Flow<List<ExpenseEntity>>`      |
| `getExpensesByAmountRange()`      | Filter by amount range          | `Flow<List<ExpenseEntity>>`      |

### 4. ExpenseDatabase

Main Room database configuration.

**Location:** `commonMain/kotlin/com/example/expensetracker/data/database/ExpenseDatabase.kt`

**Configuration:**

- Database name: `expense_tracker.db`
- Version: 1
- Entities: `ExpenseEntity`
- Type converters: `Converters`
- Schema export: enabled (for migrations)

### 5. Database Builders (Platform-Specific)

#### Common (expect declarations)

**Location:** `commonMain/kotlin/com/example/expensetracker/data/database/DatabaseBuilder.kt`

```kotlin
expect fun getRoomDatabase(): ExpenseDatabase
expect fun getDatabaseBuilder(): RoomDatabase.Builder<ExpenseDatabase>
```

#### Android Implementation

**Location:** `androidMain/kotlin/com/example/expensetracker/data/database/DatabaseBuilder.android.kt`

- Uses `ApplicationContext` for database access
- Database location: Standard Android app database directory
- Requires initialization in `MainActivity`

**Initialization:**

```kotlin
// In MainActivity.onCreate()
AndroidDatabaseContext.init(this)
```

#### iOS Implementation

**Location:** `iosMain/kotlin/com/example/expensetracker/data/database/DatabaseBuilder.ios.kt`

- Uses iOS Document Directory for database storage
- No initialization required
- Automatically finds appropriate storage location

### 6. ExpenseRepository (Singleton)

Provides clean API for data access and handles seeding.

**Location:** `commonMain/kotlin/com/example/expensetracker/data/repository/ExpenseRepository.kt`

**Key Features:**

- Singleton pattern for app-wide access
- Automatic database seeding on first launch
- Converts between entity and domain models
- Provides Flow-based reactive data

**Usage Example:**

```kotlin
val repository = ExpenseRepository.getInstance()

// Get all expenses (observes changes)
viewModelScope.launch {
    repository.getAllExpenses()
        .collect { expenses ->
            // Update UI
        }
}

// Insert expense
viewModelScope.launch {
    repository.insertExpense(expense)
}

// Delete expense
viewModelScope.launch {
    repository.deleteExpense(expense)
}
```

### 7. ExpenseHistoryViewModel

ViewModel that uses the repository.

**Location:** `commonMain/kotlin/com/example/expensetracker/viewmodel/ExpenseHistoryViewModel.kt`

**Key Updates:**

- Injects `ExpenseRepository` (default = singleton instance)
- Observes database changes via Flow
- All CRUD operations persist to database
- UI updates automatically when data changes

## Database Seeding

The database is automatically seeded with 8 sample expenses on first launch.

**Seed Data Location:** `ExpenseRepository.generateSeedData()`

**Seed Logic:**

1. Repository checks expense count on initialization
2. If count = 0, inserts seed data
3. Seed data includes variety of categories and currencies
4. Seeding happens on background thread (doesn't block UI)

**Seed Data:**

- 8 expenses spanning 5 days (Oct 28 - Nov 1, 2024)
- Categories: Food, Travel, Utilities, Other
- Currencies: USD, EUR
- Amounts range from $15.99 to $180.00

## How to Use the Database

### For ViewModel Developers

**Step 1: Get Repository Instance**

```kotlin
class MyViewModel(
    private val repository: ExpenseRepository = ExpenseRepository.getInstance()
) : ViewModel() {
    // ...
}
```

**Step 2: Observe Data (Reactive)**

```kotlin
private fun loadData() {
    viewModelScope.launch {
        repository.getAllExpenses()
            .catch { exception ->
                // Handle errors
            }
            .collect { expenses ->
                // Update UI state
            }
    }
}
```

**Step 3: Perform CRUD Operations**

```kotlin
// Create/Update
fun saveExpense(expense: Expense) {
    viewModelScope.launch {
        try {
            repository.insertExpense(expense)
            // UI updates automatically via Flow
        } catch (e: Exception) {
            // Handle error
        }
    }
}

// Delete
fun deleteExpense(expense: Expense) {
    viewModelScope.launch {
        try {
            repository.deleteExpense(expense)
            // UI updates automatically via Flow
        } catch (e: Exception) {
            // Handle error
        }
    }
}

// Query with filters
fun loadExpensesByCategory(category: ExpenseCategory) {
    viewModelScope.launch {
        repository.getExpensesByCategory(category)
            .collect { expenses ->
                // Update UI
            }
    }
}
```

### For New Feature Developers

If you're creating a new feature that needs expense data:

1. **Get the repository:**

   ```kotlin
   val repository = ExpenseRepository.getInstance()
   ```

2. **Access data via repository methods** (never access DAO directly)

3. **Use coroutines for all operations:**

   ```kotlin
   viewModelScope.launch {
       val count = repository.getExpenseCount()
   }
   ```

4. **Leverage Flows for reactive UI:**
   ```kotlin
   repository.getAllExpenses()
       .collect { expenses ->
           // UI updates automatically
       }
   ```

## Data Flow Example

**User Action → Database → UI Update:**

```
User clicks "Delete" on an expense
         ↓
ViewModel.confirmDeleteExpense()
         ↓
Repository.deleteExpense(expense)
         ↓
ExpenseDao.deleteExpense(entity)
         ↓
Room Database (SQL DELETE)
         ↓
Flow emits updated list
         ↓
Repository Flow transformation
         ↓
ViewModel collects update
         ↓
UI State updates
         ↓
Composable recomposes with new data
```

## Platform Initialization

### Android

**Required:** Initialize database context in `MainActivity`:

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidDatabaseContext.init(this) // Required!
        setContent { /* ... */ }
    }
}
```

### iOS

**No initialization required** - database location is determined automatically.

> **Note:** For iOS-specific setup details and migration path, see [IOS_UPDATES.md](./IOS_UPDATES.md)

## Testing

### Unit Testing ViewModels

Mock the repository for isolated testing:

```kotlin
class MockExpenseRepository : ExpenseRepository {
    // Mock implementation
}

@Test
fun testViewModel() {
    val mockRepo = MockExpenseRepository()
    val viewModel = ExpenseHistoryViewModel(repository = mockRepo)
    // Test logic
}
```

### Integration Testing

Reset repository between tests:

```kotlin
@After
fun tearDown() {
    ExpenseRepository.resetInstance()
}
```

## Migration Strategy

When you need to update the database schema:

1. **Update Entity:**

   ```kotlin
   @Entity(tableName = "expenses")
   data class ExpenseEntity(
       // Add new field
       val newField: String = ""
   )
   ```

2. **Increment Database Version:**

   ```kotlin
   @Database(
       entities = [ExpenseEntity::class],
       version = 2, // Increment
       exportSchema = true
   )
   ```

3. **Add Migration:**

   ```kotlin
   val MIGRATION_1_2 = object : Migration(1, 2) {
       override fun migrate(database: SupportSQLiteDatabase) {
           database.execSQL(
               "ALTER TABLE expenses ADD COLUMN newField TEXT NOT NULL DEFAULT ''"
           )
       }
   }
   ```

4. **Apply Migration in Builder:**
   ```kotlin
   getDatabaseBuilder()
       .addMigrations(MIGRATION_1_2)
       .build()
   ```

## Best Practices

### ✅ DO

- Always use `ExpenseRepository.getInstance()` to access data
- Use `viewModelScope.launch` for all database operations
- Collect Flows in ViewModels, not in Composables
- Handle exceptions when performing database operations
- Use the repository pattern - never access DAO directly from ViewModels
- Keep database logic in the repository

### ❌ DON'T

- Don't create multiple repository instances
- Don't access DAO directly from ViewModels
- Don't perform database operations on the main thread (without coroutines)
- Don't expose entities to the View layer - use domain models
- Don't hardcode database paths
- Don't forget to initialize on Android

## Troubleshooting

### Common Issues

**Issue:** App crashes with "lateinit property context has not been initialized"  
**Solution:** Ensure `AndroidDatabaseContext.init(this)` is called in `MainActivity.onCreate()`

**Issue:** Database is empty even after seeding  
**Solution:** Check that repository is initialized before accessing data. Seeding happens asynchronously.

**Issue:** UI doesn't update after database changes  
**Solution:** Ensure you're collecting the Flow from the repository in `viewModelScope`

**Issue:** Build errors with KSP  
**Solution:** Clean and rebuild: `./gradlew clean build`

## Dependencies

Current versions (as of implementation):

```toml
androidx-room = "2.7.0-alpha12"
androidx-sqlite = "2.5.0-alpha12"
ksp = "2.2.20-1.0.29"
kotlin = "2.2.20"
```

## Further Reading

### Official Documentation

- [Room KMP Official Documentation](https://developer.android.com/kotlin/multiplatform/room)
- [Kotlin Flows](https://kotlinlang.org/docs/flow.html)
- [MVVM Architecture](https://developer.android.com/topic/architecture)
- [Repository Pattern](https://developer.android.com/codelabs/basic-android-kotlin-compose-add-repository)

### Project Documentation

- [Implementation Status](./STATUS.md) - Quick status and platform support
- [iOS Updates & Best Practices](./IOS_UPDATES.md) - iOS-specific details
- [Advanced Features](./ADVANCED_FEATURES.md) - Transactions and KMP limitations
- [Documentation Index](../README.md) - Main documentation index

## Summary

This implementation provides:

✅ **Shared Database** for Android and iOS  
✅ **Automatic Seeding** on first launch  
✅ **Reactive Data** via Kotlin Flows  
✅ **Clean Architecture** with Repository Pattern  
✅ **Type-Safe** queries with Room  
✅ **Easy to Extend** for new features  
✅ **Production-Ready** error handling

The database is ready to use across your entire application. Simply get the repository instance and start querying!

> **For current implementation status and platform support, see [STATUS.md](./STATUS.md)**
