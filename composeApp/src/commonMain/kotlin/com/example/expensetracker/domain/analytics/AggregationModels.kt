package com.example.expensetracker.domain.analytics


import kotlinx.datetime.*

data class Transaction(
    val id: String,
    val title: String,
    val amount: Double,
    val category: String,
    val date: LocalDate,
)

data class CategoryTotal(
    val category: String,
    val total: Double,
    val count: Int,
    val percent: Double
)

data class MonthlyAggregate(
    val month: YearMonth,
    val totalExpenses: Double,
    val transactionCount: Int,
    val categories: List<CategoryTotal>,
    val daily: List<DailyAggregate>,
    val weekly: List<WeeklyAggregate>,
    val averageDaily: Double,
    val averageWeekly: Double
)

data class WeeklyAggregate(
    val weekOfMonth: Int,        // 1–5
    val total: Double,
    val days: List<DailyAggregate>
)

data class DailyAggregate(
    val date: LocalDate,
    val total: Double
)
