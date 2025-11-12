package com.example.expensetracker.data.network

import io.ktor.client.HttpClient
import io.ktor.client.darwin.Darwin

/**
 * iOS implementation of Ktor HTTP client factory
 * Uses Darwin engine for HTTP requests
 */
actual fun createHttpClient(
    timeoutSeconds: Long
): HttpClient {
    return HttpClient(Darwin) {
        configureHttpClient(timeoutSeconds)
    }
}

