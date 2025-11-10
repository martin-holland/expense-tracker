package com.example.expensetracker.data.network

import com.example.expensetracker.data.network.model.ErrorResponse
import com.example.expensetracker.data.network.model.ExchangeRateResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

/**
 * Service for fetching exchange rates from exchangerate-api.com
 * 
 * This service handles HTTP requests to the exchange rate API and provides
 * error handling and response validation.
 * 
 * Usage:
 * ```kotlin
 * val service = ExchangeRateApiService(httpClient)
 * val result = service.getLatestRates(apiKey, "USD", baseUrl)
 * result.onSuccess { response -> ... }
 * result.onFailure { error -> ... }
 * ```
 */
class ExchangeRateApiService(
    private val httpClient: HttpClient
) {
    /**
     * Fetches the latest exchange rates for a base currency
     * 
     * This single API call returns ALL rates for the base currency,
     * allowing cross-rate calculations without additional API calls.
     * 
     * @param apiKey The API key for exchangerate-api.com
     * @param baseCurrency The base currency code (e.g., "USD", "EUR")
     * @param baseUrl The API base URL (default: "https://v6.exchangerate-api.com/v6")
     * @return Result containing ExchangeRateResponse on success, or Exception on failure
     * 
     * Endpoint: GET {baseUrl}/{apiKey}/latest/{baseCurrency}
     * Example: GET https://v6.exchangerate-api.com/v6/YOUR-API-KEY/latest/USD
     */
    suspend fun getLatestRates(
        apiKey: String,
        baseCurrency: String,
        baseUrl: String = "https://v6.exchangerate-api.com/v6"
    ): Result<ExchangeRateResponse> {
        return try {
            // Validate inputs
            if (apiKey.isBlank()) {
                return Result.failure(IllegalArgumentException("API key cannot be empty"))
            }
            if (baseCurrency.isBlank()) {
                return Result.failure(IllegalArgumentException("Base currency cannot be empty"))
            }
            
            // Construct URL
            val url = "$baseUrl/$apiKey/latest/$baseCurrency"
            
            // Make HTTP request
            val response: HttpResponse = httpClient.get(url)
            
            // Check HTTP status code
            if (response.status != HttpStatusCode.OK) {
                return Result.failure(
                    Exception("API returned status ${response.status.value}: ${response.status.description}")
                )
            }
            
            // Parse response body
            val responseBody: ExchangeRateResponse = response.body()
            
            // Validate response
            if (!responseBody.isSuccess()) {
                // Response indicates an error
                val errorMessage = "API returned error: ${responseBody.result}"
                return Result.failure(Exception(errorMessage))
            }
            
            Result.success(responseBody)
            
        } catch (e: kotlinx.serialization.SerializationException) {
            // Log the actual serialization error for debugging
            val errorDetails = buildString {
                append("Serialization error: ${e.message}")
                e.cause?.let { append("\nCause: ${it.message}") }
                e.stackTrace.take(5).forEach { 
                    append("\n  at ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})")
                }
            }
            println(errorDetails)
            Result.failure(Exception("Failed to parse API response: Invalid JSON format. ${e.message}", e))
        } catch (e: io.ktor.client.network.sockets.ConnectTimeoutException) {
            Result.failure(Exception("Connection timeout: Unable to reach the API server", e))
        } catch (e: io.ktor.client.network.sockets.SocketTimeoutException) {
            Result.failure(Exception("Request timeout: The API server took too long to respond", e))
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}", e))
        }
    }
}

