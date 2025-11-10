package com.example.expensetracker.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.expensetracker.data.repository.ExchangeRateRepository
import com.example.expensetracker.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Android implementation of ExchangeRateRefreshWorker using WorkManager
 * 
 * Schedules periodic work to refresh exchange rates every 24 hours.
 * Requires network connection but does not require device charging.
 */
actual object ExchangeRateRefreshWorker {
    private const val WORK_NAME = "exchange_rate_refresh_work"
    private const val REPEAT_INTERVAL_HOURS = 24L
    private var applicationContext: Context? = null
    
    /**
     * Initializes the worker with application context
     * This should be called once from MainActivity.onCreate()
     */
    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }
    
    /**
     * Schedules periodic exchange rate refresh using WorkManager
     * 
     * The work will run every 24 hours, with a minimum interval of 15 minutes
     * (WorkManager's minimum periodic work interval).
     * 
     * Constraints:
     * - Requires network connection
     * - Does not require device charging
     * - Can run on any battery level
     */
    actual fun scheduleExchangeRateRefresh() {
        val context = applicationContext
            ?: throw IllegalStateException("ExchangeRateRefreshWorker not initialized. Call initialize() first.")
        scheduleExchangeRateRefreshInternal(context)
    }
    
    /**
     * Internal function to actually schedule the work
     */
    private fun scheduleExchangeRateRefreshInternal(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresCharging(false)
            .build()
        
        val workRequest = PeriodicWorkRequestBuilder<RefreshExchangeRateWorker>(
            REPEAT_INTERVAL_HOURS,
            TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing work if already scheduled
            workRequest
        )
    }
    
    /**
     * Cancels the scheduled exchange rate refresh
     */
    actual fun cancelExchangeRateRefresh() {
        val context = applicationContext
            ?: throw IllegalStateException("ExchangeRateRefreshWorker not initialized. Call initialize() first.")
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}

/**
 * WorkManager Worker that performs the actual exchange rate refresh
 */
class RefreshExchangeRateWorker(
    context: Context,
    params: androidx.work.WorkerParameters
) : androidx.work.CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val settingsRepository = SettingsRepository.getInstance()
            val exchangeRateRepository = ExchangeRateRepository.getInstance()
            
            // Get current base currency
            val baseCurrency = settingsRepository.getBaseCurrencySync()
            
            // Refresh exchange rates
            val result = exchangeRateRepository.refreshExchangeRates(baseCurrency)
            
            result.fold(
                onSuccess = {
                    Result.success()
                },
                onFailure = { error ->
                    // Log error but don't fail the work - we'll retry on next interval
                    android.util.Log.w(
                        "RefreshExchangeRateWorker",
                        "Failed to refresh exchange rates: ${error.message}",
                        error
                    )
                    Result.retry() // Retry on next interval
                }
            )
        } catch (e: Exception) {
            // Log error but don't fail the work - we'll retry on next interval
            android.util.Log.e(
                "RefreshExchangeRateWorker",
                "Error refreshing exchange rates",
                e
            )
            Result.retry() // Retry on next interval
        }
    }
}

