# Viewing Room Database During Development

This guide explains multiple ways to view and inspect your Room database while developing the Expense Tracker app.

## 📍 Database Location

### Android

The database is stored at:

```
/data/data/com.example.expensetracker/databases/expense_tracker.db
```

**Full path on device:**

- Internal storage: `/data/data/com.example.expensetracker/databases/expense_tracker.db`
- Accessible via ADB or Android Studio

### iOS

The database is stored in the app's Documents directory:

```
~/Documents/expense_tracker.db
```

**Full path:** Available in the iOS Simulator's file system or device via Xcode.

---

## Method 1: Android Studio Database Inspector (Recommended) ⭐

The easiest and most powerful way to view your Room database is using Android Studio's built-in Database Inspector.

### Setup Steps:

1. **Enable Database Inspector:**

   - Run your app on an emulator or connected device (API 26+)
   - In Android Studio, go to: **View → Tool Windows → App Inspection**
   - Or use the bottom toolbar: Click **App Inspection** tab

2. **Select Your Device:**

   - In the App Inspection window, select your running app process
   - You should see "Databases" section

3. **View Database:**
   - Expand "Databases" → Find `expense_tracker.db`
   - Click on the database to view tables
   - Click on `expenses` table to see all data
   - You can run SQL queries directly in the inspector!

### Features:

- ✅ Real-time updates (refreshes automatically)
- ✅ Run custom SQL queries
- ✅ Edit data directly (for testing)
- ✅ View table schemas
- ✅ Export data to CSV
- ✅ No additional setup required

### Limitations:

- Requires Android Studio
- Only works with running app (API 26+)
- Not available for iOS

---

## Method 2: ADB Commands (Android)

Use Android Debug Bridge (ADB) to pull the database file to your computer.

### Step 1: Find Your Device

```bash
adb devices
```

### Step 2: Pull the Database File

```bash
# Pull the database file to your current directory
adb pull /data/data/com.example.expensetracker/databases/expense_tracker.db

# Or pull to a specific location
adb pull /data/data/com.example.expensetracker/databases/expense_tracker.db ~/Desktop/expense_tracker.db
```

### Step 3: Open with SQLite Viewer

Once you have the file, open it with any SQLite viewer:

- **DB Browser for SQLite** (Free, cross-platform): https://sqlitebrowser.org/
- **SQLiteStudio** (Free): https://sqlitestudio.pl/
- **TablePlus** (Free/Paid, macOS/Windows): https://tableplus.com/
- **VS Code Extension**: "SQLite Viewer" by qwtel

### Quick ADB Commands:

```bash
# List all databases
adb shell run-as com.example.expensetracker ls databases/

# Pull database
adb pull /data/data/com.example.expensetracker/databases/expense_tracker.db

# Push database back (if you edited it)
adb push expense_tracker.db /data/data/com.example.expensetracker/databases/expense_tracker.db

# Open SQLite shell on device
adb shell run-as com.example.expensetracker sqlite3 databases/expense_tracker.db
```

---

## Method 3: Enable Schema Export (For Documentation)

Enable schema export to generate JSON schema files that document your database structure.

### Update ExpenseDatabase.kt:

```kotlin
@Database(
    entities = [ExpenseEntity::class],
    version = 1,
    exportSchema = true  // Change from false to true
)
```

### Configure Schema Export Location:

In `composeApp/build.gradle.kts`, you already have:

```kotlin
room {
    schemaDirectory("$projectDir/schemas")
}
```

This will generate schema files in `composeApp/schemas/` directory.

### View Generated Schemas:

- Location: `composeApp/schemas/com.example.expensetracker.data.database.ExpenseDatabase/`
- Files: `1.json`, `2.json`, etc. (one per version)
- These JSON files show the complete database schema

**Note:** Schema export is mainly for documentation and migration planning, not for viewing live data.

---

## Method 4: Add Database Path Logging (Development Only)

Add a temporary logging function to print the database path in Logcat.

### Add to DatabaseBuilder.android.kt:

```kotlin
actual fun getRoomDatabase(): ExpenseDatabase {
    return databaseInstance ?: synchronized(AndroidDatabaseContext) {
        databaseInstance ?: createDatabase(
            getDatabaseBuilder()
                .fallbackToDestructiveMigrationOnDowngrade(true)
        ).also {
            databaseInstance = it
            // Development only - log database path
            android.util.Log.d("Database", "Database path: ${AndroidDatabaseContext.context.getDatabasePath(ExpenseDatabase.DATABASE_NAME).absolutePath}")
        }
    }
}
```

Then check Logcat for the exact path when the app starts.

---

## Method 5: Third-Party Tools

### Stetho (Facebook)

Stetho allows you to inspect your database via Chrome DevTools.

**Add dependency:**

```kotlin
// In composeApp/build.gradle.kts, androidMain dependencies
debugImplementation("com.facebook.stetho:stetho:1.6.0")
```

**Initialize in MainActivity:**

```kotlin
if (BuildConfig.DEBUG) {
    com.facebook.stetho.Stetho.initializeWithDefaults(this)
}
```

**Access:**

- Open Chrome → `chrome://inspect`
- Click "inspect" under your device
- Go to "Resources" → "Web SQL" → Your database

### Room Database Viewer (Android Studio Plugin)

- Install plugin: **File → Settings → Plugins → Search "Room Database Viewer"**
- Provides enhanced database viewing capabilities

---

## Method 6: iOS Database Access

### iOS Simulator:

1. Open **Finder**
2. Go to: `~/Library/Developer/CoreSimulator/Devices/[DEVICE_ID]/data/Containers/Data/Application/[APP_ID]/Documents/`
3. Find `expense_tracker.db`
4. Copy to your Mac and open with SQLite viewer

### Find Device ID:

```bash
xcrun simctl list devices
```

### Find App ID:

The app's Documents directory path is logged when the database is created, or you can find it in Xcode's device logs.

### Using Xcode:

1. Run app in Simulator
2. In Xcode: **Window → Devices and Simulators**
3. Select your simulator → Your app → Download Container
4. Right-click container → Show Package Contents
5. Navigate to `AppData/Documents/expense_tracker.db`

---

## Quick Reference: Database File Locations

### Android

```
Device: /data/data/com.example.expensetracker/databases/expense_tracker.db
ADB Pull: adb pull /data/data/com.example.expensetracker/databases/expense_tracker.db
```

### iOS

```
Simulator: ~/Library/Developer/CoreSimulator/Devices/[DEVICE]/data/Containers/Data/Application/[APP]/Documents/expense_tracker.db
Device: Access via Xcode → Devices and Simulators → Download Container
```

---

## Recommended Workflow

### For Daily Development:

1. **Use Android Studio Database Inspector** (Method 1)
   - Fastest and most convenient
   - Real-time updates
   - Built-in SQL query editor

### For Deep Analysis:

1. **Pull database with ADB** (Method 2)
2. **Open in DB Browser for SQLite**
   - Better for complex queries
   - Export data to CSV/JSON
   - Visual table relationships

### For Schema Documentation:

1. **Enable schema export** (Method 3)
   - Keep schema files in version control
   - Useful for migration planning

---

## Troubleshooting

### Database Inspector Not Showing Data:

- Ensure app is running on device/emulator
- Check that database has been initialized (app must have run at least once)
- Try restarting Android Studio
- Verify API level is 26+ (required for Database Inspector)

### ADB Permission Denied:

```bash
# If you get permission denied, use run-as:
adb shell run-as com.example.expensetracker cp databases/expense_tracker.db /sdcard/
adb pull /sdcard/expense_tracker.db
```

### Database File Not Found:

- Ensure the app has been run at least once (database is created on first access)
- Check that `AndroidDatabaseContext.init()` was called in MainActivity
- Verify database name matches: `expense_tracker.db`

### iOS Database Not Found:

- Database is created on first app launch
- Check that the app has write permissions
- Verify the Documents directory path in iOS logs

---

## SQL Queries for Common Tasks

### View All Expenses:

```sql
SELECT * FROM expenses ORDER BY date DESC;
```

### Count Expenses by Category:

```sql
SELECT category, COUNT(*) as count
FROM expenses
GROUP BY category;
```

### View Expenses in Specific Currency:

```sql
SELECT * FROM expenses WHERE currency = 'USD';
```

### View Expenses by Date Range:

```sql
SELECT * FROM expenses
WHERE date >= '2024-10-01T00:00:00'
AND date <= '2024-11-01T23:59:59'
ORDER BY date DESC;
```

### Check Database Size:

```sql
SELECT page_count * page_size as size
FROM pragma_page_count(), pragma_page_size();
```

---

## Best Practices

1. **Never edit production database directly** - Use migrations instead
2. **Use Database Inspector for quick checks** during development
3. **Pull database files for backup** before major changes
4. **Enable schema export** for documentation
5. **Remove debug logging** before production builds

---

## Additional Resources

- [Android Studio Database Inspector Guide](https://developer.android.com/studio/inspect/database)
- [Room Database Documentation](https://developer.android.com/training/data-storage/room)
- [DB Browser for SQLite](https://sqlitebrowser.org/)
- [ADB Documentation](https://developer.android.com/studio/command-line/adb)

---

**Last Updated:** 2024-11-01  
**Database Name:** `expense_tracker.db`  
**Current Version:** 1
