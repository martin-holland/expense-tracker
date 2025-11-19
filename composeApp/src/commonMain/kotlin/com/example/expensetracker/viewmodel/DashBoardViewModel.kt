package com.example.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.domain.analytics.CategoryTotal
import com.example.expensetracker.domain.analytics.DailyAggregate
import com.example.expensetracker.domain.analytics.ExpenseAggregator
import com.example.expensetracker.domain.analytics.MonthlyAggregate
import com.example.expensetracker.domain.analytics.Transaction
import com.example.expensetracker.domain.analytics.WeeklyAggregate
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.data.repository.IExpenseRepository
import com.example.expensetracker.model.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlin.time.ExperimentalTime
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus



/*────────────────────────────────────────────────────*/
/*                 DASHBOARD UI STATE                  */
/*────────────────────────────────────────────────────*/

data class DashboardUiState(
    val expenses: List<Expense> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,


    // Aggregated data
    val currentMonth: YearMonth? = null,
    val monthlyAggregate: MonthlyAggregate? = null,
    val previousMonthAggregate: MonthlyAggregate? = null,
    val monthOverMonthChange: Double? = null,

    val categoryTotals: List<CategoryTotal> = emptyList(),
    val weeklyAggregates: List<WeeklyAggregate> = emptyList(),
    val dailyAggregates: List<DailyAggregate> = emptyList()
)

/*────────────────────────────────────────────────────*/
/*                 VIEWMODEL IMPLEMENTATION            */
/*────────────────────────────────────────────────────*/

class DashBoardViewModel(
    private val repository: IExpenseRepository = ExpenseRepository.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadExpenses()
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            try {
                repository.getAllExpenses().collectLatest { expenses ->
                    updateAggregates(expenses)
                }
            } catch (e: Exception) {
                _uiState.value = uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    /*────────────────────────────────────────────────────*/
    /*                   AGGREGATION LOGIC                */
    /*────────────────────────────────────────────────────*/
    @OptIn(ExperimentalTime::class)
    private fun updateAggregates(expenses: List<Expense>) {
        if (expenses.isEmpty()) {
            _uiState.value = DashboardUiState(
                expenses = emptyList(),
                isLoading = false
            )
            return
        }

        // Convert Expense → Transaction for analytics
        val transactions = expenses.map { it.toTransaction() }

        val today = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date

        val currentMonth = YearMonth(today.year, today.month)
        val previousMonth = currentMonth.minusMonths(1)

        val currentAgg = ExpenseAggregator.getMonthlyAggregate(
            transactions = transactions,
            month = currentMonth
        )

        val prevAgg = ExpenseAggregator.getMonthlyAggregate(
            transactions = transactions,
            month = previousMonth
        )

        val momChange = ExpenseAggregator.calculateMonthOverMonthChange(
            currentAgg,
            prevAgg
        )

        _uiState.value = DashboardUiState(
            expenses = expenses,
            isLoading = false,
            error = null,

            currentMonth = currentMonth,
            monthlyAggregate = currentAgg,
            previousMonthAggregate = prevAgg,
            monthOverMonthChange = momChange,

            categoryTotals = currentAgg.categories,
            weeklyAggregates = currentAgg.weekly,
            dailyAggregates = currentAgg.daily
        )
    }

    /*           UTIL: YearMonth minusMonths()            */

    private fun YearMonth.minusMonths(months: Int): YearMonth {
        val firstDay = LocalDate(this.year, this.month.number, 1)
        val adjusted = firstDay.minus(months, DateTimeUnit.MONTH)
        return YearMonth(adjusted.year, adjusted.month)
    }
}

/*     EXTENSION: Map Expense → Transaction model      */

fun Expense.toTransaction(): Transaction =
    Transaction(
        id = this.id,
        title = this.description,
        amount = this.amount,
        category = this.category.displayName,
        date = this.date.date        // LocalDateTime → LocalDate
    )