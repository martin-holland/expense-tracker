package com.example.expensetracker.domain.analytics

import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.ExpenseCategory
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

object ExpenseAggregator {

    /* ----------------------------------------------------
     *  FILTERING UTILITIES
     * ---------------------------------------------------- */

    fun filterByDateRange(
        expenses: List<Expense>,
        start: LocalDate,
        end: LocalDate
    ): List<Expense> =
        expenses.filter { it.date.date in start..end }

    fun search(
        expenses: List<Expense>,
        query: String
    ): List<Expense> =
        expenses.filter {
            it.description.contains(query, ignoreCase = true) ||
                    it.category.name.contains(query, ignoreCase = true)
        }

    /* ----------------------------------------------------
     *  CATEGORY GROUPING
     * ---------------------------------------------------- */

    fun groupByCategory(
        expenses: List<Expense>
    ): List<CategoryTotal> {

        val total = expenses.sumOf { it.amount }

        return expenses
            .groupBy { it.category }
            .map { (category, list) ->
                val sum = list.sumOf { it.amount }

                CategoryTotal(
                    category = category,
                    total = sum,
                    count = list.size,
                    percent = if (total == 0.0) 0.0 else (sum / total * 100)
                )
            }
            .sortedByDescending { it.total }
    }

    fun topCategories(
        expenses: List<Expense>,
        limit: Int = 3
    ): List<CategoryTotal> =
        groupByCategory(expenses).take(limit)

    /* ----------------------------------------------------
     *  DAILY AGGREGATION  (REAL CATEGORY TOTALS)
     * ---------------------------------------------------- */

    fun groupByDay(expenses: List<Expense>): List<DailyAggregate> =
        expenses
            .groupBy { it.date.date }
            .map { (date, list) ->

                val categoryTotals: Map<ExpenseCategory, Double> =
                    list.groupBy { it.category }
                        .mapValues { (_, items) -> items.sumOf { it.amount } }

                DailyAggregate(
                    date = date,
                    total = list.sumOf { it.amount },
                    categoryTotals = categoryTotals
                )
            }
            .sortedBy { it.date }

    /* ----------------------------------------------------
     *  WEEKLY AGGREGATION
     * ---------------------------------------------------- */

    fun groupByWeek(expenses: List<Expense>): List<WeeklyAggregate> {

        val daily = groupByDay(expenses)

        return daily
            .groupBy { day ->
                (day.date.day - 1) / 7 + 1
            }
            .map { (week, days) ->
                WeeklyAggregate(
                    weekOfMonth = week,
                    total = days.sumOf { it.total },
                    days = days
                )
            }
            .sortedBy { it.weekOfMonth }
    }

    /* ----------------------------------------------------
     *  MONTHLY AGGREGATION
     * ---------------------------------------------------- */

    fun getMonthlyAggregate(
        expenses: List<Expense>,
        month: YearMonth
    ): MonthlyAggregate {

        val monthExpenses = expenses.filter {
            YearMonth(it.date.year, it.date.month) == month
        }

        val total = monthExpenses.sumOf { it.amount }
        val daily = groupByDay(monthExpenses)
        val weekly = groupByWeek(monthExpenses)
        val categories = groupByCategory(monthExpenses)

        val averageDaily =
            if (daily.isEmpty()) 0.0 else total / daily.size

        val averageWeekly =
            if (weekly.isEmpty()) 0.0 else total / weekly.size

        return MonthlyAggregate(
            month = month,
            totalExpenses = total,
            transactionCount = monthExpenses.size,
            categories = categories,
            daily = daily,
            weekly = weekly,
            averageDaily = averageDaily,
            averageWeekly = averageWeekly
        )
    }

    /* ----------------------------------------------------
     *  MONTH-OVER-MONTH CHANGE
     * ---------------------------------------------------- */

    fun calculateMonthOverMonthChange(
        current: MonthlyAggregate,
        previous: MonthlyAggregate?
    ): Double?  {

        if (previous == null || previous.totalExpenses == 0.0)
            return null

        return ((current.totalExpenses - previous.totalExpenses) /
                previous.totalExpenses) * 100.0
    }
}
