package com.example.expensetracker.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime

class AddExpenseViewModel : ViewModel() {

    var currency by mutableStateOf("USD")
        private set

    var amount by mutableStateOf("")
        private set

    var category by mutableStateOf("")
        private set

    var date by mutableStateOf("")
        private set

    var note by mutableStateOf("")
        private set

    var errorMessage by mutableStateOf<String?>(null)
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

    /** Used by the DatePicker to update date manually */
    fun onDateSelected(newDate: String) {
        date = newDate
    }

    /** Other event handlers */
    fun onCurrencySelected(curr: String) {
        currency = curr
    }

    fun onAmountChanged(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*\$"))) {
            amount = value
        }
    }

    fun onCategorySelected(cat: String) {
        category = cat
    }

    fun onNoteChanged(newNote: String) {
        note = newNote
    }

    fun saveExpense() {
        errorMessage = null

        if (amount.isBlank() || amount == "." || amount.toDoubleOrNull() == null) {
            errorMessage = "Please enter a valid amount."
            return
        }

        if (category.isBlank()) {
            errorMessage = "Please select a category."
            return
        }

        println("✅ Expense saved: $currency $amount - $category ($date): $note")
    }
}
