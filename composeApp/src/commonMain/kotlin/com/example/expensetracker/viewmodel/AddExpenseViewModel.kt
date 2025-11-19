package com.example.expensetracker.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.ExpenseCategory
import com.example.expensetracker.view.components.SnackbarMessage
import com.example.expensetracker.view.components.SnackbarType
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class AddExpenseViewModel(
        private val repository: ExpenseRepository = ExpenseRepository.getInstance()
) : ViewModel() {

    var currency by mutableStateOf(Currency.USD)
        private set

    var amount by mutableStateOf("")
        private set

    var category by mutableStateOf<ExpenseCategory?>(null)
        private set

    var date by mutableStateOf("")
        private set

    var note by mutableStateOf("")
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isSaving by mutableStateOf(false)
        private set

    var snackbarMessage by mutableStateOf<SnackbarMessage?>(null)
        private set

    init {
        updateCurrentDate()
    }

    /** ✅ Use kotlinx.datetime to get today's date */
    @OptIn(ExperimentalTime::class)
    private fun getCurrentDate(): kotlinx.datetime.LocalDate {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    /** Automatically formats today's date */
    private fun updateCurrentDate() {
        val today = getCurrentDate()
        val monthName = today.month.name.lowercase().replaceFirstChar { it.uppercase() }
        date = "$monthName ${today.day}, ${today.year}"
    }
    fun resetForm() {
        amount = ""
        note = ""
        updateCurrentDate()
        category = ExpenseCategory.FOOD
        currency = Currency.USD
    }
    /** Used by the DatePicker to update date manually */
    fun onDateSelected(newDate: String) {
        date = newDate
    }

    /** Other event handlers */
    fun onCurrencySelected(curr: Currency) {
        currency = curr
        errorMessage = null
    }

    fun onAmountChanged(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*\$"))) {
            amount = value
            errorMessage = null
        }
    }

    fun onCategorySelected(cat: ExpenseCategory) {
        category = cat
        errorMessage = null
    }

    fun onNoteChanged(newNote: String) {
        note = newNote
    }

    /**
     * Parses the formatted date string back to LocalDateTime Format: "Month Day, Year" (e.g.,
     * "November 7, 2024")
     */
    private fun parseDate(dateString: String): LocalDateTime? {
        return try {
            val parts = dateString.split(" ")
            if (parts.size != 3) return null

            val monthName = parts[0]
            val day = parts[1].removeSuffix(",").toIntOrNull() ?: return null
            val year = parts[2].toIntOrNull() ?: return null

            val month =
                    when (monthName.lowercase()) {
                        "january" -> 1
                        "february" -> 2
                        "march" -> 3
                        "april" -> 4
                        "may" -> 5
                        "june" -> 6
                        "july" -> 7
                        "august" -> 8
                        "september" -> 9
                        "october" -> 10
                        "november" -> 11
                        "december" -> 12
                        else -> return null
                    }

            // Get current time for the date
            @OptIn(ExperimentalTime::class)
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            LocalDateTime(year, month, day, now.hour, now.minute)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Generates a unique ID for the expense Uses timestamp in milliseconds plus a random component
     */
    @OptIn(ExperimentalTime::class)
    private fun generateExpenseId(): String {
        val timestamp = Clock.System.now().toEpochMilliseconds()
        val random = (1000..9999).random()
        return "expense_${timestamp}_$random"
    }

    /**
     * Validates all input fields before saving
     * @return null if validation passes, error message otherwise
     */
    private fun validateInput(): String? {
        // Validate amount - matches original logic
        if (amount.isBlank() || amount == "." || amount.toDoubleOrNull() == null) {
            return "Please enter a valid amount."
        }

        // Validate category - changed from isBlank() to == null due to type change (String ->
        // ExpenseCategory?)
        if (category == null) {
            return "Please select a category."
        }

        // Validate date - necessary because we need to parse date to save to database
        val parsedDate = parseDate(date)
        if (parsedDate == null) {
            return "Please select a valid date."
        }

        return null
    }

    /**
     * Saves the expense to the database Validates input, creates Expense object, and persists to
     * repository
     */
    fun saveExpense() {
        errorMessage = null
        snackbarMessage = null

        // Validate input
        val validationError = validateInput()
        if (validationError != null) {
            snackbarMessage = SnackbarMessage(
                message = validationError,
                type = SnackbarType.ERROR
            )
            return
        }

        // Parse date
        val parsedDate = parseDate(date)
        if (parsedDate == null) {
            snackbarMessage = SnackbarMessage(
                message = "Failed to parse date. Please try again.",
                type = SnackbarType.ERROR
            )
            return
        }

        // Create expense object
        val expense =
                Expense(
                        id = generateExpenseId(),
                        category = category!!,
                        description = note.ifBlank { "No description" },
                        amount = amount.toDouble(),
                        currency = currency,
                        date = parsedDate
                )

        // Save to repository
        isSaving = true
        viewModelScope.launch {
            try {
                repository.insertExpense(expense)
                snackbarMessage = SnackbarMessage(
                    message = "✓ Expense saved successfully!",
                    type = SnackbarType.SUCCESS
                )
                // Clear form after successful save
                clearForm()
            } catch (e: Exception) {
                snackbarMessage = SnackbarMessage(
                    message = "Failed to save expense: ${e.message}",
                    type = SnackbarType.ERROR
                )
                println("Error saving expense: ${e.message}")
            } finally {
                isSaving = false
            }
        }
    }

    /** Clears all form fields after successful save */
    private fun clearForm() {
        amount = ""
        category = null
        note = ""
        updateCurrentDate()
        // Keep currency as is (user preference)
    }

    /** Dismisses the snackbar message */
    fun dismissSnackbar() {
        snackbarMessage = null
    }
}
