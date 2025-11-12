package com.example.expensetracker.services

import io.github.aakira.napier.Napier

// commonMain
expect fun initializeNapier()

fun initApp() {
    initializeNapier()
    Napier.d("App initialized")
}