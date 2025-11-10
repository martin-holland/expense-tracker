package com.example.expensetracker.data.worker

/**
 * Platform-agnostic interface for scheduling background exchange rate refresh
 * 
 * This uses expect/actual declarations to provide platform-specific implementations:
 * - Android: Uses WorkManager for periodic background work
 * - iOS: Uses background tasks (BGTaskScheduler)
 * 
 * The worker refreshes exchange rates every 24 hours automatically.
 */
expect object ExchangeRateRefreshWorker {
    /**
     * Schedules periodic exchange rate refresh
     * 
     * On Android: Uses WorkManager to schedule periodic work every 24 hours
     * On iOS: Registers a background task to run every 24 hours
     * 
     * This should be called once during app initialization (e.g., in MainActivity.onCreate)
     * The worker will automatically refresh exchange rates in the background.
     */
    fun scheduleExchangeRateRefresh()
    
    /**
     * Cancels the scheduled exchange rate refresh
     * 
     * This can be used if the user disables automatic refresh or for testing purposes.
     */
    fun cancelExchangeRateRefresh()
}

