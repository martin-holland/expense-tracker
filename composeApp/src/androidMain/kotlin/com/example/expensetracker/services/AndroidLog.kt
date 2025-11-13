package com.example.expensetracker.services

// In androidMain/kotlin/com/your_package/Log.kt
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier


actual fun initializeNapier() {
    Napier.base(DebugAntilog())
    Napier.v("Napier Initialized on Android")

}
