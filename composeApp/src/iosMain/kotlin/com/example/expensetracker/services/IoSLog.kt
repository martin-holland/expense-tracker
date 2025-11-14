package com.example.expensetracker.services

// In iosMain/kotlin/com/your_package/Log.kt
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

actual fun initializeNapier() {
    Napier.base(DebugAntilog())
    Napier.v("Napier Initialized on iOS")

}