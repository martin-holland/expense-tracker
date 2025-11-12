package com.example.expensetracker.data.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android

/**
 * Android implementation of Ktor HTTP client factory
 * Uses Android engine for HTTP requests
 */
actual fun createHttpClient(
    timeoutSeconds: Long
): HttpClient {
    return HttpClient(Android) {
        configureHttpClient(timeoutSeconds)
    }
}

