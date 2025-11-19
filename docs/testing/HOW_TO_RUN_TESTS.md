# 🧪 How to Run Your Unit Tests

Complete guide to running and viewing test results in your ExpenseTracker project.

---

## Quick Start

```bash
# Navigate to project
cd /Users/martinholland/Documents/Metropolia/DesignPatterns/Android/ExpenseTracker

# Run all tests
./gradlew :composeApp:testDebugUnitTest

# View report
open composeApp/build/reports/tests/testDebugUnitTest/index.html
```

---

## 📋 Table of Contents

1. [Command Line Methods](#command-line-methods)
2. [IDE Methods](#ide-methods)
3. [Viewing Results](#viewing-results)
4. [Troubleshooting](#troubleshooting)
5. [Best Practices](#best-practices)

---

## Command Line Methods

### Run All Tests

```bash
./gradlew :composeApp:testDebugUnitTest
```

**Output:**
```
BUILD SUCCESSFUL in 2s
30 actionable tasks: 3 executed, 27 up-to-date
```

### Run Specific Test Class

```bash
# Full package name
./gradlew :composeApp:testDebugUnitTest \
  --tests "com.example.expensetracker.viewmodel.AddExpenseViewModelSimpleTest"

# With wildcards (shorter)
./gradlew :composeApp:testDebugUnitTest \
  --tests "*.AddExpenseViewModelSimpleTest"

# All tests in a package
./gradlew :composeApp:testDebugUnitTest \
  --tests "com.example.expensetracker.viewmodel.*"
```

### Run Specific Test Method

```bash
# Run single test
./gradlew :composeApp:testDebugUnitTest \
  --tests "*.AddExpenseViewModelSimpleTest.initial state has empty form fields"

# Run multiple specific tests
./gradlew :composeApp:testDebugUnitTest \
  --tests "*.AddExpenseViewModelSimpleTest.onAmountChanged*"
```

### Run with Console Output

```bash
# Show test results in console
./gradlew :composeApp:testDebugUnitTest --info

# Show even more details
./gradlew :composeApp:testDebugUnitTest --debug

# Show test names as they run
./gradlew :composeApp:testDebugUnitTest --info | grep "Test "
```

### Clean Before Running

```bash
# Clean build cache and run tests
./gradlew clean :composeApp:testDebugUnitTest

# Force re-run all tests (ignore cache)
./gradlew :composeApp:testDebugUnitTest --rerun-tasks
```

### Continuous Testing

```bash
# Run tests automatically when code changes
./gradlew :composeApp:testDebugUnitTest --continuous
```

---

## IDE Methods

### IntelliJ IDEA / Android Studio

#### Method 1: Right-Click Menu (Easiest!)

1. **Open test file** in editor:
   - `AddExpenseViewModelSimpleTest.kt`

2. **Right-click** on:
   - Class name → "Run 'AddExpenseViewModelSimpleTest'"
   - Test method → "Run 'test name'"
   - Green arrow in gutter → Quick run

3. **View results** in bottom panel:
   - ✅ Green = Passed
   - ❌ Red = Failed
   - Click test to see details

#### Method 2: Run Configuration

1. **Create run configuration:**
   - Run → Edit Configurations
   - ➕ Add New → Gradle
   - **Name:** "All Unit Tests"
   - **Tasks:** `:composeApp:testDebugUnitTest`
   - **Save**

2. **Run from toolbar:**
   - Select configuration from dropdown
   - Click ▶️ (Run) or 🐛 (Debug)

#### Method 3: Test Explorer

1. **Open Test Explorer:**
   - View → Tool Windows → Test Results
   - Or: `Cmd+5` (Mac) / `Alt+5` (Windows)

2. **Run tests:**
   - Click 🔄 to discover tests
   - Right-click package/class → Run
   - See hierarchical results

#### Method 4: Keyboard Shortcuts

| Action | Mac | Windows/Linux |
|--------|-----|---------------|
| Run test at cursor | `Ctrl+Shift+R` | `Ctrl+Shift+F10` |
| Re-run last tests | `Ctrl+R` | `Shift+F10` |
| Run with coverage | `Ctrl+Shift+R` with Coverage | `Ctrl+Shift+F10` with Coverage |
| Debug test | `Ctrl+Shift+D` | `Ctrl+Shift+F9` |

---

## Viewing Results

### 1. HTML Report (BEST! 🌟)

**Location:**
```
composeApp/build/reports/tests/testDebugUnitTest/index.html
```

**Open it:**
```bash
# Mac
open composeApp/build/reports/tests/testDebugUnitTest/index.html

# Linux
xdg-open composeApp/build/reports/tests/testDebugUnitTest/index.html

# Windows
start composeApp/build/reports/tests/testDebugUnitTest/index.html
```

**What you'll see:**
- 📊 **Summary page:**
  - Total tests: 11
  - Passed: 11 ✅
  - Failed: 0
  - Duration: 0.234s

- 📦 **Package view:**
  - Tests organized by package
  - Click to drill down

- 🔍 **Test details:**
  - Test name
  - Execution time
  - Status
  - Stack traces (if failed)

**Screenshot of Report Structure:**
```
┌─────────────────────────────────────────────┐
│ Test Summary                                 │
├─────────────────────────────────────────────┤
│ Tests: 11    Failures: 0    Duration: 0.2s  │
│ ✅ 100% successful                           │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│ Packages                                     │
├─────────────────────────────────────────────┤
│ 📦 com.example.expensetracker.viewmodel     │
│    └─ AddExpenseViewModelSimpleTest (11)    │
└─────────────────────────────────────────────┘

┌─────────────────────────────────────────────┐
│ Test Cases                                   │
├─────────────────────────────────────────────┤
│ ✅ initial state has empty form fields      │
│ ✅ default currency is USD                  │
│ ✅ onCurrencySelected updates currency      │
│ ✅ onAmountChanged accepts valid decimal    │
│ ... 7 more tests ...                        │
└─────────────────────────────────────────────┘
```

### 2. Console Output

**Basic:**
```
> Task :composeApp:testDebugUnitTest

BUILD SUCCESSFUL in 2s
```

**With --info flag:**
```
> Task :composeApp:testDebugUnitTest
AddExpenseViewModelSimpleTest > initial state has empty form fields PASSED
AddExpenseViewModelSimpleTest > default currency is USD PASSED
AddExpenseViewModelSimpleTest > onCurrencySelected updates currency PASSED
...
BUILD SUCCESSFUL in 2s
```

### 3. IDE Test Panel

**Features:**
- 🌳 **Tree view** of all tests
- ⏱️ **Execution time** per test
- 🔍 **Search/filter** tests
- 📝 **Console output** for each test
- 🐛 **Debug mode** with breakpoints
- 📊 **Coverage visualization**

**Example IDE Output:**
```
✅ AddExpenseViewModelSimpleTest (11 tests) - 234ms
  ✅ initial state has empty form fields - 12ms
  ✅ default currency is USD - 8ms
  ✅ onCurrencySelected updates currency - 15ms
  ✅ onAmountChanged accepts valid decimal - 18ms
  ✅ onCategorySelected updates category - 14ms
  ✅ onNoteChanged updates note - 11ms
  ✅ onDateSelected updates date - 13ms
  ✅ date format is consistent - 22ms
  ✅ amount validation works for empty input - 19ms
  ✅ resetForm clears fields correctly - 47ms
  ✅ saveExpense with valid data sets up correctly - 55ms
```

### 4. XML Report (for CI/CD)

**Location:**
```
composeApp/build/test-results/testDebugUnitTest/
```

**Files:**
- `TEST-*.xml` - JUnit XML format
- Used by CI systems (GitHub Actions, Jenkins, etc.)

---

## Troubleshooting

### Tests Don't Run

**Problem:** Nothing happens when running tests

**Solutions:**
```bash
# 1. Clean and rebuild
./gradlew clean build

# 2. Invalidate caches (IDE)
File → Invalidate Caches → Invalidate and Restart

# 3. Check Gradle sync
./gradlew --refresh-dependencies
```

### Tests Pass but Report Not Generated

**Problem:** Build succeeds but no HTML report

**Solution:**
```bash
# Reports are only generated when tests run
# Force tests to run even if cached
./gradlew :composeApp:testDebugUnitTest --rerun-tasks
```

### Can't Find Test Report

**Problem:** Report file doesn't exist

**Check locations:**
```bash
# Main location
composeApp/build/reports/tests/testDebugUnitTest/index.html

# Alternative (if variant is different)
ls -la composeApp/build/reports/tests/

# List all HTML reports
find composeApp/build -name "index.html" -path "*/tests/*"
```

### Tests Fail with "Class not found"

**Problem:** `ClassNotFoundException` or `NoClassDefFoundError`

**Solution:**
```bash
# Rebuild project
./gradlew clean build

# Check dependencies
./gradlew :composeApp:dependencies
```

### IDE Doesn't Show Tests

**Problem:** Test class doesn't appear in test explorer

**Solutions:**
1. **Sync Gradle:**
   - File → Sync Project with Gradle Files
   - Or click 🐘 (Gradle sync) icon

2. **Rebuild:**
   - Build → Rebuild Project

3. **Check test location:**
   - Must be in `src/commonTest/kotlin/` or `src/test/kotlin/`
   - Must have `@Test` annotations

---

## Best Practices

### 1. Run Tests Frequently

```bash
# Before committing
./gradlew :composeApp:testDebugUnitTest

# Quick test during development (specific class)
./gradlew :composeApp:testDebugUnitTest --tests "*.AddExpenseViewModelSimpleTest"
```

### 2. Use Continuous Testing

```bash
# Auto-run tests when files change
./gradlew :composeApp:testDebugUnitTest --continuous
```

Press `Ctrl+C` to stop.

### 3. Check Coverage

```bash
# Run with coverage
./gradlew :composeApp:testDebugUnitTestCoverage

# View coverage report
open composeApp/build/reports/coverage/test/debug/index.html
```

### 4. Run in CI/CD

**GitHub Actions example:**
```yaml
- name: Run tests
  run: ./gradlew :composeApp:testDebugUnitTest

- name: Upload test results
  uses: actions/upload-artifact@v3
  if: always()
  with:
    name: test-results
    path: composeApp/build/reports/tests/
```

### 5. Parallel Execution

```bash
# Run tests in parallel (faster!)
./gradlew :composeApp:testDebugUnitTest --parallel --max-workers=4
```

### 6. Filter by Tags (Future)

When you add tags to tests:
```kotlin
@Tag("fast")
@Test
fun `quick test`() { ... }
```

Run specific tags:
```bash
./gradlew :composeApp:testDebugUnitTest --tests "*[fast]*"
```

---

## Quick Reference

### Most Common Commands

```bash
# Run all tests
./gradlew :composeApp:testDebugUnitTest

# Run specific test class
./gradlew :composeApp:testDebugUnitTest --tests "*.AddExpenseViewModelSimpleTest"

# View report
open composeApp/build/reports/tests/testDebugUnitTest/index.html

# Clean and test
./gradlew clean :composeApp:testDebugUnitTest

# Force re-run
./gradlew :composeApp:testDebugUnitTest --rerun-tasks

# Watch mode
./gradlew :composeApp:testDebugUnitTest --continuous
```

### IDE Quick Actions

| Action | Shortcut (Mac) |
|--------|----------------|
| Run test at cursor | `Ctrl+Shift+R` |
| Re-run last | `Ctrl+R` |
| Debug test | `Ctrl+Shift+D` |
| Show test results | `Cmd+5` |

---

## Example Session

```bash
# 1. Navigate to project
cd /Users/martinholland/Documents/Metropolia/DesignPatterns/Android/ExpenseTracker

# 2. Run tests
./gradlew :composeApp:testDebugUnitTest

# Expected output:
# > Task :composeApp:testDebugUnitTest
# BUILD SUCCESSFUL in 2s
# 30 actionable tasks: 3 executed, 27 up-to-date

# 3. View report
open composeApp/build/reports/tests/testDebugUnitTest/index.html

# You'll see:
# ┌─────────────────────────────────────────┐
# │ Test Summary                             │
# │ Tests: 11 | Passed: 11 | Failed: 0      │
# │ ✅ 100% successful                       │
# └─────────────────────────────────────────┘

# 4. Run specific test
./gradlew :composeApp:testDebugUnitTest \
  --tests "*.AddExpenseViewModelSimpleTest.initial state has empty form fields"

# 5. Watch mode for development
./gradlew :composeApp:testDebugUnitTest --continuous

# Now tests auto-run when you save files!
```

---

## Summary

**Fastest way:**
```bash
./gradlew :composeApp:testDebugUnitTest && \
open composeApp/build/reports/tests/testDebugUnitTest/index.html
```

**In IDE:**
1. Right-click test file → Run
2. View results in bottom panel ✅

**Pro tip:** 
Use continuous mode during development:
```bash
./gradlew :composeApp:testDebugUnitTest --continuous
```

---

**Happy Testing!** 🧪✨

*Last updated: November 19, 2025*

