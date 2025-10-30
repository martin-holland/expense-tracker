package com.example.expensetracker

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform