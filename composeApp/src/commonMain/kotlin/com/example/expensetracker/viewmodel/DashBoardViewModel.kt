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


        uiState = uiState.copy(expense = mockExpense)
    }

    private fun generateMockExpenses(): List<Expense> {
        // Create static dates for mock data (no Clock.System needed for iOS compatibility)
//    val nov1 = LocalDateTime(2024, 11, 1, 12, 0)
        val oct31 = LocalDateTime(2024, 10, 31, 14, 30)
        val oct30 = LocalDateTime(2024, 10, 30, 10, 15)
        val oct29 = LocalDateTime(2024, 10, 29, 16, 45)
        val oct28 = LocalDateTime(2024, 10, 28, 9, 0)

        return listOf(
            Expense(
                id = "1",
                category = ExpenseCategory.FOOD,
                description = "Lunch at restaurant",
                amount = 45.50,
                currency = Currency.USD,
                date = oct28
            ),
            Expense(
                id = "2",
                category = ExpenseCategory.TRAVEL,
                description = "Gas station",
                amount = 120.00,
                currency = Currency.USD,
                date = oct29
            ),
            Expense(
                id = "3",
                category = ExpenseCategory.FOOD,
                description = "Coffee shop",
                amount = 15.99,
                currency = Currency.USD,
                date = oct31
            ),
            Expense(
                id = "4",
                category = ExpenseCategory.UTILITIES,
                description = "Electricity bill",
                amount = 85.00,
                currency = Currency.USD,
                date = oct31
            ),
            Expense(
                id = "5",
                category = ExpenseCategory.FOOD,
                description = "Grocery shopping",
                amount = 32.50,
                currency = Currency.USD,
                date = oct30
            ),
            Expense(
                id = "6",
                category = ExpenseCategory.TRAVEL,
                description = "Uber ride",
                amount = 50.00,
                currency = Currency.USD,
                date = oct30
            ),
            Expense(
                id = "7",
                category = ExpenseCategory.OTHER,
                description = "Online subscription",
                amount = 25.99,
                currency = Currency.EUR,
                date = oct29
            ),
            Expense(
                id = "8",
                category = ExpenseCategory.UTILITIES,
                description = "Internet bill",
                amount = 180.00,
                currency = Currency.USD,
                date = oct28
            )
        )
    }

}


data class DashBoardUiState(
    val expense: List<Expense> = emptyList()
)