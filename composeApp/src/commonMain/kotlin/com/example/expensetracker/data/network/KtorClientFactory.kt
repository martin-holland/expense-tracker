package com.example.expensetracker.data.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.HttpTimeout
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Factory for creating platform-specific Ktor HTTP clients
 * 
 * Uses expect/actual pattern to provide platform-specific engine implementations
 * while maintaining a common interface for configuration.
 * 
 * Usage:
 * ```kotlin
 * val client = createHttpClient(timeoutSeconds = 30)
 * // Base URL will be set when making requests
 * ```
 */
expect fun createHttpClient(
    timeoutSeconds: Long = 30
): HttpClient

/**
 * Common configuration for Ktor HTTP client
 * This function is called by platform-specific implementations
 */
internal fun HttpClientConfig<*>.configureHttpClient(
    timeoutSeconds: Long
) {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = false
            }
        )
    }
    
    install(HttpTimeout) {
        requestTimeoutMillis = timeoutSeconds * 1000
        connectTimeoutMillis = timeoutSeconds * 1000
        socketTimeoutMillis = timeoutSeconds * 1000
    }
}

