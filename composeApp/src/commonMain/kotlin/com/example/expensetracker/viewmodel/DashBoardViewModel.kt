package com.example.expensetracker.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.ExpenseCategory
import kotlinx.datetime.LocalDateTime


//import io.github.oshai.kotlinlogging.KotlinLogging



class DashBoardViewModel: ViewModel(){
    var uiState by mutableStateOf(DashBoardUiState())
        private set

//    private val logger = KotlinLogging.logger {}


    init {
        loadMockData()
    }

    private fun loadMockData(){
        val mockExpense = generateMockExpenses()

    println("hello ${mockExpense}")


        uiState = uiState.copy(expenses = mockExpense)
    }

    private fun generateMockExpenses(): List<Expense> {
        // Mock data centered around November 7, 2025
        // Includes dates from before last 7 days, within last 7 days, and future dates
        
        // Before last 7 days (Oct 28-31, 2025)
        val oct28 = LocalDateTime(2025, 10, 28, 9, 0)
        val oct29 = LocalDateTime(2025, 10, 29, 16, 45)
        val oct30 = LocalDateTime(2025, 10, 30, 10, 15)
        val oct31 = LocalDateTime(2025, 10, 31, 14, 30)
        
        // Last 7 days (Nov 1-7, 2025) - these should show on the chart
        val nov1 = LocalDateTime(2025, 11, 1, 8, 30)   // Saturday
        val nov2 = LocalDateTime(2025, 11, 2, 12, 0)   // Sunday
        val nov3 = LocalDateTime(2025, 11, 3, 15, 20)  // Monday
        val nov4 = LocalDateTime(2025, 11, 4, 18, 45)  // Tuesday
        val nov5 = LocalDateTime(2025, 11, 5, 9, 15)   // Wednesday
        val nov6 = LocalDateTime(2025, 11, 6, 13, 30)  // Thursday
        val nov7 = LocalDateTime(2025, 11, 7, 11, 0)   // Friday (today)
        
        // Future dates (Nov 8-10, 2025) - these should NOT show on the chart
        val nov8 = LocalDateTime(2025, 11, 8, 10, 0)
        val nov9 = LocalDateTime(2025, 11, 9, 14, 30)
        val nov10 = LocalDateTime(2025, 11, 10, 16, 0)

        return listOf(
            // Old expenses (before last 7 days)
            Expense(
                id = "1",
                category = ExpenseCategory.FOOD,
                description = "Old lunch",
                amount = 45.50,
                currency = Currency.USD,
                date = oct28
            ),
            Expense(
                id = "2",
                category = ExpenseCategory.TRAVEL,
                description = "Old gas",
                amount = 120.00,
                currency = Currency.USD,
                date = oct29
            ),
            
            // Last 7 days expenses (Nov 1-7)
            Expense(
                id = "3",
                category = ExpenseCategory.FOOD,
                description = "Saturday brunch",
                amount = 35.00,
                currency = Currency.USD,
                date = nov1
            ),
            Expense(
                id = "4",
                category = ExpenseCategory.TRAVEL,
                description = "Saturday taxi",
                amount = 25.00,
                currency = Currency.USD,
                date = nov1
            ),
            Expense(
                id = "5",
                category = ExpenseCategory.FOOD,
                description = "Sunday dinner",
                amount = 65.50,
                currency = Currency.USD,
                date = nov2
            ),
            Expense(
                id = "6",
                category = ExpenseCategory.UTILITIES,
                description = "Internet bill",
                amount = 80.00,
                currency = Currency.USD,
                date = nov3
            ),
            Expense(
                id = "7",
                category = ExpenseCategory.FOOD,
                description = "Monday coffee",
                amount = 5.50,
                currency = Currency.USD,
                date = nov3
            ),
            Expense(
                id = "8",
                category = ExpenseCategory.TRAVEL,
                description = "Tuesday gas",
                amount = 55.00,
                currency = Currency.USD,
                date = nov4
            ),
            Expense(
                id = "9",
                category = ExpenseCategory.FOOD,
                description = "Tuesday lunch",
                amount = 18.75,
                currency = Currency.USD,
                date = nov4
            ),
            Expense(
                id = "10",
                category = ExpenseCategory.OTHER,
                description = "Wednesday subscription",
                amount = 12.99,
                currency = Currency.USD,
                date = nov5
            ),
            Expense(
                id = "11",
                category = ExpenseCategory.FOOD,
                description = "Wednesday groceries",
                amount = 95.30,
                currency = Currency.USD,
                date = nov5
            ),
            Expense(
                id = "12",
                category = ExpenseCategory.FOOD,
                description = "Thursday breakfast",
                amount = 15.00,
                currency = Currency.USD,
                date = nov6
            ),
            Expense(
                id = "13",
                category = ExpenseCategory.UTILITIES,
                description = "Thursday electricity",
                amount = 120.00,
                currency = Currency.USD,
                date = nov6
            ),
            Expense(
                id = "14",
                category = ExpenseCategory.FOOD,
                description = "Friday lunch",
                amount = 22.50,
                currency = Currency.USD,
                date = nov7
            ),
            Expense(
                id = "15",
                category = ExpenseCategory.TRAVEL,
                description = "Friday uber",
                amount = 18.00,
                currency = Currency.USD,
                date = nov7
            ),
            
            // Future expenses (should be filtered out)
            Expense(
                id = "16",
                category = ExpenseCategory.FOOD,
                description = "Future expense 1",
                amount = 50.00,
                currency = Currency.USD,
                date = nov8
            ),
            Expense(
                id = "17",
                category = ExpenseCategory.TRAVEL,
                description = "Future expense 2",
                amount = 100.00,
                currency = Currency.USD,
                date = nov9
            ),
            Expense(
                id = "18",
                category = ExpenseCategory.OTHER,
                description = "Future expense 3",
                amount = 75.00,
                currency = Currency.USD,
                date = nov10
            )
        )
    }

}


data class DashBoardUiState(
    val expenses: List<Expense> = emptyList()
)