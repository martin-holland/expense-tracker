# Database Migration: Version 3 → 4

## Overview

This migration adds theme and voice input settings to the database, allowing user preferences to persist across app launches.

## Date
November 14, 2025

## Changes

### Schema Changes

Added two new columns to the `settings` table:

| Column | Type | Default | Description |
|--------|------|---------|-------------|
| `themeOption` | TEXT | 'SYSTEM' | User's theme preference (LIGHT, DARK, or SYSTEM) |
| `isVoiceInputEnabled` | INTEGER | 0 | Whether voice input feature is enabled (0=false, 1=true) |

### Migration Details

**Location:** `composeApp/src/commonMain/kotlin/com/example/expensetracker/data/database/DatabaseMigrations.kt`

**Migration Class:** `MIGRATION_3_4`

**SQL Statements:**
```sql
-- Add themeOption column
ALTER TABLE settings 
ADD COLUMN themeOption TEXT NOT NULL DEFAULT 'SYSTEM';

-- Add isVoiceInputEnabled column
ALTER TABLE settings 
ADD COLUMN isVoiceInputEnabled INTEGER NOT NULL DEFAULT 0;
```

### Data Preservation

✅ **All existing data is preserved** during this migration:
- Base currency settings
- Exchange rate API configuration
- Last exchange rate update timestamp
- All expense records
- All exchange rate records

Only new columns are added with sensible defaults:
- `themeOption` defaults to `'SYSTEM'` (follows system theme)
- `isVoiceInputEnabled` defaults to `0` (disabled for privacy/security)

## Implementation

### 1. Database Schema (`SettingsEntity.kt`)
```kotlin
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: String = "settings",
    val baseCurrency: Currency,
    val lastExchangeRateUpdate: LocalDateTime?,
    val exchangeRateApiKey: String,
    val exchangeRateApiBaseUrl: String,
    val themeOption: ThemeOption = ThemeOption.SYSTEM,        // NEW
    val isVoiceInputEnabled: Boolean = false                  // NEW
)
```

### 2. Domain Model (`AppSettings.kt`)
```kotlin
data class AppSettings(
    val baseCurrency: Currency = Currency.USD,
    val lastExchangeRateUpdate: LocalDateTime? = null,
    val exchangeRateApiKey: String = "",
    val exchangeRateApiBaseUrl: String = "https://v6.exchangerate-api.com/v6",
    val themeOption: ThemeOption = ThemeOption.SYSTEM,        // NEW
    val isVoiceInputEnabled: Boolean = false                  // NEW
)
```

### 3. Type Converters (`TypeConverters.kt`)
Added converters for `ThemeOption` enum:
- `fromThemeOption(ThemeOption?): String?`
- `toThemeOption(String?): ThemeOption?`

### 4. DAO Methods (`SettingsDao.kt`)
Added update methods:
- `updateThemeOption(themeOption: String)`
- `updateVoiceInputEnabled(isEnabled: Boolean)`

### 5. Repository Methods (`SettingsRepository.kt`)
Added repository methods with Flow support:
- `getThemeOption(): Flow<ThemeOption>`
- `getThemeOptionSync(): ThemeOption`
- `setThemeOption(themeOption: ThemeOption)`
- `getVoiceInputEnabled(): Flow<Boolean>`
- `getVoiceInputEnabledSync(): Boolean`
- `setVoiceInputEnabled(isEnabled: Boolean)`

### 6. ViewModel Integration (`SettingsViewModel.kt`)
Updated to:
- Load theme and voice settings from database on init
- Observe changes via Flow for reactive updates
- Persist changes with error handling and rollback

### 7. UI Integration (`AddExpenseScreen.kt`)
- Added voice input enable/disable functionality
- Visual indicators for voice input status
- Microphone permission request integration

## Testing the Migration

### Before Migration (Version 3)
```sql
sqlite> .schema settings
CREATE TABLE settings (
    id TEXT NOT NULL PRIMARY KEY,
    baseCurrency TEXT NOT NULL,
    lastExchangeRateUpdate TEXT,
    exchangeRateApiKey TEXT NOT NULL,
    exchangeRateApiBaseUrl TEXT NOT NULL
);
```

### After Migration (Version 4)
```sql
sqlite> .schema settings
CREATE TABLE settings (
    id TEXT NOT NULL PRIMARY KEY,
    baseCurrency TEXT NOT NULL,
    lastExchangeRateUpdate TEXT,
    exchangeRateApiKey TEXT NOT NULL,
    exchangeRateApiBaseUrl TEXT NOT NULL,
    themeOption TEXT NOT NULL DEFAULT 'SYSTEM',
    isVoiceInputEnabled INTEGER NOT NULL DEFAULT 0
);
```

### Verify Migration Success
```sql
sqlite> SELECT * FROM settings;
settings|USD|NULL||https://v6.exchangerate-api.com/v6|SYSTEM|0
```

## Migration Execution Flow

1. **App Launch:** `MainActivity.onCreate()` initializes `AndroidDatabaseContext`
2. **Database Access:** First access to database triggers Room to check version
3. **Version Check:** Room detects version 3 → 4 upgrade needed
4. **Migration Execution:** `MIGRATION_3_4.migrate()` is called
5. **Schema Update:** SQL statements add new columns with defaults
6. **Completion:** Database is now at version 4, data preserved

## Rollback Plan

If you need to rollback to version 3:

### Option 1: Downgrade with Data Loss
```kotlin
.fallbackToDestructiveMigrationOnDowngrade(true)  // Already enabled
```

### Option 2: Manual Downgrade (Preserve Data)
If needed, create a downgrade migration:
```kotlin
val MIGRATION_4_3 = object : Migration(4, 3) {
    override fun migrate(database: SQLiteConnection) {
        // SQLite doesn't support DROP COLUMN directly
        // Would need to recreate table without new columns
        // This is complex, so downgrade not recommended
    }
}
```

**Recommendation:** Don't downgrade. If issues arise, fix forward with a new migration.

## Production Deployment Checklist

✅ Migration tested in development  
✅ Data preservation verified  
✅ No destructive migration flags (except downgrade)  
✅ Error handling in place (ViewModel rollback)  
✅ Default values are sensible  
✅ Documentation complete  

## Notes

- Migration is automatic on app update
- Users won't notice any disruption
- Settings will load defaults until user changes them
- Theme will follow system preference by default
- Voice input will be disabled by default (user must enable)

## Related Files

- `DatabaseMigrations.kt` - Migration definitions
- `DatabaseBuilder.kt` - Migration registration
- `SettingsEntity.kt` - Database schema
- `AppSettings.kt` - Domain model
- `TypeConverters.kt` - Type conversion
- `SettingsDao.kt` - Database operations
- `SettingsRepository.kt` - Data access layer
- `SettingsViewModel.kt` - Business logic
- `AddExpenseScreen.kt` - UI integration

## Future Migrations

When adding future migrations:
1. Create new `MIGRATION_X_Y` in `DatabaseMigrations.kt`
2. Follow the same pattern (prepare → step → close)
3. Add to `createDatabase()` in `DatabaseBuilder.kt`
4. Update database version in `ExpenseDatabase.kt`
5. Test thoroughly before release
6. Document in similar format

