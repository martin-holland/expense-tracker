package com.example.expensetracker.helpers

import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

/**
 * Extension functions for testing Flows using Turbine
 */

/**
 * Test that a flow emits expected values in order
 */
suspend fun <T> Flow<T>.testEmissions(
    vararg expectedValues: T,
    timeout: kotlin.time.Duration = 5.seconds
) {
    test(timeout = timeout) {
        expectedValues.forEach { expected ->
            val item = awaitItem()
            assertEquals(expected, item)
        }
        cancelAndIgnoreRemainingEvents()
    }
}

/**
 * Test that a flow emits a single value
 */
suspend fun <T> Flow<T>.testSingleEmission(
    expectedValue: T,
    timeout: kotlin.time.Duration = 5.seconds
) {
    test(timeout = timeout) {
        assertEquals(expectedValue, awaitItem())
        cancelAndIgnoreRemainingEvents()
    }
}

/**
 * Collect all emitted items from a flow
 */
suspend fun <T> Flow<T>.collectItems(
    count: Int,
    timeout: kotlin.time.Duration = 5.seconds
): List<T> {
    val items = mutableListOf<T>()
    test(timeout = timeout) {
        repeat(count) {
            items.add(awaitItem())
        }
        cancelAndIgnoreRemainingEvents()
    }
    return items
}

