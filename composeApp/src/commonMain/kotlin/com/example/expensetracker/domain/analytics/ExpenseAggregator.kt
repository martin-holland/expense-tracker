package com.example.expensetracker.domain.analytics

import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

object ExpenseAggregator {

    /*     FILTERING UTILITIES     */

    fun filterByDateRange(
        transactions: List<Transaction>,
        start: LocalDate,
        end: LocalDate
    ): List<Transaction> =
        transactions.filter { it.date in start..end }

    fun search(
        transactions: List<Transaction>,
        query: String
    ): List<Transaction> =
        transactions.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)
        }

    /*   CATEGORY GROUPING LOGIC   */

    fun groupByCategory(
        transactions: List<Transaction>
    ): List<CategoryTotal> {
        val total = transactions.sumOf { it.amount }

        return transactions
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
        transactions: List<Transaction>,
        limit: Int = 3
    ): List<CategoryTotal> =
        groupByCategory(transactions).take(limit)

    /*      DAILY AGGREGATION      */

    fun groupByDay(transactions: List<Transaction>): List<DailyAggregate> =
        transactions
            .groupBy { it.date }
            .map { (date, list) ->
                DailyAggregate(
                    date = date,
                    total = list.sumOf { it.amount }
                )
            }
            .sortedBy { it.date }

    /*      WEEKLY AGGREGATION     */

    fun groupByWeek(transactions: List<Transaction>): List<WeeklyAggregate> {
        val daily = groupByDay(transactions)

        return daily
            .groupBy { it.date.dayOfMonth - 1/ 7 + 1 }   // week-of-month
            .map { (week, days) ->
                WeeklyAggregate(
                    weekOfMonth = week,
                    total = days.sumOf { it.total },
                    days = days
                )
            }
            .sortedBy { it.weekOfMonth }
    }

    /*      MONTHLY AGGREGATION    */

    fun getMonthlyAggregate(
        transactions: List<Transaction>,
        month: YearMonth
    ): MonthlyAggregate {

        val monthTransactions = transactions.filter {
            YearMonth(it.date.year, it.date.month) == month
        }

        val total = monthTransactions.sumOf { it.amount }
        val daily = groupByDay(monthTransactions)
        val weekly = groupByWeek(monthTransactions)
        val categories = groupByCategory(monthTransactions)

        val averageDaily =
            if (daily.isEmpty()) 0.0 else total / daily.size

        val averageWeekly =
            if (weekly.isEmpty()) 0.0 else total / weekly.size

        return MonthlyAggregate(
            month = month,
            totalExpenses = total,
            transactionCount = monthTransactions.size,
            categories = categories,
            daily = daily,
            weekly = weekly,
            averageDaily = averageDaily,
            averageWeekly = averageWeekly
        )
    }

    /*      MONTH-TO-MONTH TREND   */

    fun calculateMonthOverMonthChange(
        current: MonthlyAggregate,
        previous: MonthlyAggregate?
    ): Double? {
        if (previous == null || previous.totalExpenses == 0.0) return null

        return ((current.totalExpenses - previous.totalExpenses) /
                previous.totalExpenses) * 100.0
    }
}