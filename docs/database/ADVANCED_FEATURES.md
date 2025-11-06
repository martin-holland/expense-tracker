# Room KMP Advanced Features & Limitations

## Based on Official Documentation

Source: [Android Developer - Room KMP](https://developer.android.com/kotlin/multiplatform/room)

> **Note:** For basic implementation details, see [IMPLEMENTATION.md](./IMPLEMENTATION.md). For current status, see [STATUS.md](./STATUS.md).

---

## 🚀 Advanced Features (For Future Use)

### Transaction APIs

Room KMP provides advanced transaction APIs for complex database operations. While our current implementation doesn't need these (DAO operations are already atomic), they're available for future features.

#### Write Transactions

According to the [official documentation](https://developer.android.com/kotlin/multiplatform/room), write transactions ensure multiple queries write data atomically:

```kotlin
val database: RoomDatabase = getRoomDatabase()

database.useWriterConnection { transactor ->
    transactor.immediateTransaction {
        // Multiple write operations execute atomically
        expenseDao.insertExpense(expense1)
        expenseDao.insertExpense(expense2)
        expenseDao.deleteExpenseById("old-id")
    }
}
```

**Transaction Types:**

1. **immediateTransaction** (Recommended for most cases)

   - Acquires lock when transaction starts
   - Readers can continue reading (in WAL mode)
   - Best for predictable write operations

2. **deferredTransaction**

   - Lock acquired only on first write statement
   - Use when you're unsure if writes are needed
   - Optimization for conditional operations

3. **exclusiveTransaction**
   - Identical to `immediateTransaction` in WAL mode
   - In non-WAL modes, blocks all other connections
   - Use only when necessary

**Example Use Case:**

```kotlin
// Bulk operations that must succeed or fail together
suspend fun transferExpenseBetweenCategories(
    expenseId: String,
    newCategory: ExpenseCategory
) {
    getRoomDatabase().useWriterConnection { transactor ->
        transactor.immediateTransaction {
            val expense = expenseDao.getExpenseById(expenseId)
            if (expense != null) {
                val updated = expense.copy(category = newCategory)
                expenseDao.updateExpense(updated)
                // Other related operations...
            }
        }
    }
}
```

#### Read Transactions

For consistent multi-query reads as documented in the [official guide](https://developer.android.com/kotlin/multiplatform/room):

```kotlin
val database: RoomDatabase = getRoomDatabase()

database.useReaderConnection { transactor ->
    transactor.deferredTransaction {
        // Multiple read operations see consistent data snapshot
        val allExpenses = expenseDao.getAllExpenses()
        val count = expenseDao.getExpenseCount()
        // Data is consistent between these two queries
    }
}
```

**When to use:**

- Multiple separate queries without JOIN
- Need consistent snapshot across queries
- Complex read operations

**Note:** Only deferred transactions allowed in reader connections. Immediate or exclusive transactions will throw an exception.

---

## ⚠️ Features NOT Available in KMP

Based on the [official limitations documentation](https://developer.android.com/kotlin/multiplatform/room#not-available), these Android-only features are not available in common code:

### 1. Query Callbacks ❌

**Not Available:**

```kotlin
// Android-only - won't work in commonMain
RoomDatabase.Builder.setQueryCallback(...)
RoomDatabase.QueryCallback
```

**Status:** Support planned for future Room versions

**Our Workaround:** Use logging in DAO methods if needed for debugging

### 2. Auto-Closing Database ❌

**Not Available:**

```kotlin
// Android-only - won't work in commonMain
RoomDatabase.Builder.setAutoCloseTimeout(...)
```

**Status:** Android-only feature, no KMP support planned

**Impact:** Minimal - iOS handles memory management efficiently via ARC

### 3. Pre-Packaged Databases ❌

**Not Available:**

```kotlin
// Android-only - won't work in commonMain
RoomDatabase.Builder.createFromAsset(...)
RoomDatabase.Builder.createFromFile(...)
RoomDatabase.Builder.createFromInputStream(...)
```

**Status:** Support planned for future Room versions

**Our Solution:** ✅ We're using seed data in Repository instead:

```kotlin
private suspend fun seedDatabaseIfEmpty() {
    val count = expenseDao.getExpenseCount()
    if (count == 0) {
        val seedData = generateSeedData()
        expenseDao.insertExpenses(seedData.map { it.toEntity() })
    }
}
```

This approach works perfectly across all platforms!

### 4. Multi-Instance Invalidation ❌

**Not Available:**

```kotlin
// Android-only - won't work in commonMain
RoomDatabase.Builder.enableMultiInstanceInvalidation()
```

**Status:** Android-only feature, no KMP support planned

**Impact:** Minimal - Most apps use single database instance

---

## 📋 Migration Checklist

If migrating existing Android Room code to KMP, according to [official migration guide](https://developer.android.com/kotlin/multiplatform/room#migrate):

### Convert Blocking to Suspend Functions

**Before (Android-only):**

```kotlin
@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses")
    fun getAllExpenses(): List<Expense>  // Blocking

    @Transaction
    fun blockingTransaction() { ... }  // Blocking
}
```

**After (KMP-compatible):**

```kotlin
@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses")
    suspend fun getAllExpenses(): List<Expense>  // ✅ Suspend

    @Transaction
    suspend fun transaction() { ... }  // ✅ Suspend
}
```

### Convert LiveData to Flow

**Before (Android-only):**

```kotlin
@Query("SELECT * FROM expenses")
fun getExpensesLiveData(): LiveData<List<Expense>>  // Android-only
```

**After (KMP-compatible):**

```kotlin
@Query("SELECT * FROM expenses")
fun getExpensesFlow(): Flow<List<Expense>>  // ✅ KMP Flow
```

### Update Transaction APIs

**Before (Android-only):**

```kotlin
database.withTransaction {
    // operations
}
```

**After (KMP-compatible):**

```kotlin
database.useWriterConnection { transactor ->
    transactor.immediateTransaction {
        // operations
    }
}
```

---

## 🎯 What We're Already Doing Right

Based on the [official best practices](https://developer.android.com/kotlin/multiplatform/room):

### ✅ Suspend Functions

Our DAO uses suspend for all write operations:

```kotlin
@Dao
interface ExpenseDao {
    suspend fun insertExpense(expense: ExpenseEntity)
    suspend fun updateExpense(expense: ExpenseEntity)
    suspend fun deleteExpense(expense: ExpenseEntity)
}
```

### ✅ Flow for Reactive Queries

We use Flow instead of LiveData:

```kotlin
@Query("SELECT * FROM expenses ORDER BY date DESC")
fun getAllExpenses(): Flow<List<ExpenseEntity>>
```

### ✅ Proper Database Structure

```kotlin
@Database(entities = [ExpenseEntity::class], version = 1)
@TypeConverters(Converters::class)
@ConstructedBy(ExpenseDatabaseConstructor::class)
abstract class ExpenseDatabase : RoomDatabase()
```

### ✅ Platform-Specific Builders

Android and iOS implementations properly separated using expect/actual pattern.

### ✅ BundledSQLiteDriver

Using the recommended driver for cross-platform consistency:

```kotlin
builder
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.IO)
```

---

## 💡 When You Might Need Advanced Features

### Use Transactions When:

1. **Bulk operations must be atomic**
   - Importing multiple expenses from file
   - Batch updates that must all succeed or fail together
2. **Complex data dependencies**

   - Updating expense and related category statistics
   - Moving data between tables atomically

3. **Consistent multi-query reads**
   - Generating reports from multiple queries
   - Need snapshot consistency across queries

### Example: Bulk Import with Transactions

```kotlin
suspend fun importExpenses(expenses: List<Expense>) {
    getRoomDatabase().useWriterConnection { transactor ->
        transactor.immediateTransaction {
            // All insertions succeed or all fail
            expenses.forEach { expense ->
                expenseDao.insertExpense(expense.toEntity())
            }
        }
    }
}
```

### Current Implementation: No Transactions Needed ✅

Our current operations are already atomic:

- Single insert/update/delete operations
- DAO operations are automatically atomic
- No complex multi-step operations yet

**Conclusion:** We can add transactions when needed for future features!

---

## 📚 Additional Resources

### Official Documentation

1. **Room KMP Setup Guide**  
   https://developer.android.com/kotlin/multiplatform/room

2. **Migration Codelab**  
   https://developer.android.com/codelabs/kmp-migrate-room

3. **SQLite Documentation** (for transaction details)  
   https://www.sqlite.org/lang_transaction.html

4. **Coroutines Guide**  
   https://developer.android.com/kotlin/coroutines

5. **Flow Guide**  
   https://developer.android.com/kotlin/flow

### Project Documentation

- [Implementation Guide](./IMPLEMENTATION.md) - Complete technical reference
- [Implementation Status](./STATUS.md) - Current status and quick reference
- [iOS Updates](./IOS_UPDATES.md) - iOS-specific details
- [Documentation Index](../README.md) - Main documentation index

---

## 🎓 Summary

### What Our Implementation Has:

✅ Suspend functions (required for KMP)  
✅ Flow-based queries (instead of LiveData)  
✅ Proper @ConstructedBy annotation  
✅ Platform-specific builders  
✅ Type converters  
✅ Seed data (instead of pre-packaged DB)  
✅ BundledSQLiteDriver

### What We Don't Need Yet:

⏸️ Advanced transactions (operations are already atomic)  
⏸️ Query callbacks (not available in KMP anyway)  
⏸️ Auto-closing (iOS handles this well)  
⏸️ Multi-instance invalidation (single instance is fine)

### What's Coming in Future Room Versions:

🔜 Query callback support  
🔜 Pre-packaged database support for iOS  
🔜 More KMP features

---

**Bottom Line:** Our implementation follows all current best practices from the official documentation. Advanced features like transactions are available when needed for future enhancements!

> **For basic implementation details and usage examples, see [IMPLEMENTATION.md](./IMPLEMENTATION.md)**

_Based on: [Official Android Room KMP Documentation](https://developer.android.com/kotlin/multiplatform/room)_  
_Last Updated: November 2025_
