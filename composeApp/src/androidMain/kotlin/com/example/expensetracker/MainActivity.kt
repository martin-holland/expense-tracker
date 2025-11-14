// androidMain/MainActivity.kt
package com.example.expensetracker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.expensetracker.data.database.AndroidDatabaseContext
import com.example.expensetracker.data.worker.ExchangeRateRefreshWorker
import com.example.expensetracker.services.AndroidCameraService
import com.example.expensetracker.services.initializeNapier
import com.example.theme.com.example.expensetracker.ThemeProvider
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize database early to ensure migrations run
        // This triggers database creation and applies migrations if needed
        android.util.Log.d("MainActivity", "Initializing database...")
        try {
            val database = com.example.expensetracker.data.database.getRoomDatabase()
            // Access a DAO to ensure database is fully initialized
            database.settingsDao()
            android.util.Log.d("MainActivity", "Database initialized successfully")
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error initializing database", e)
        }

        // Initialize and schedule background exchange rate refresh
        try {
            ExchangeRateRefreshWorker.initialize(this)
            ExchangeRateRefreshWorker.scheduleExchangeRateRefresh()
            android.util.Log.d("MainActivity", "Exchange rate refresh worker scheduled")
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error scheduling exchange rate refresh", e)
        }

        appContext = this

        requestNecessaryPermissions()

        initializeNapier()
        Napier.d("App initialized", tag = "DDD")

        // Initialize database context for Android
        AndroidDatabaseContext.init(this)

        // Camera will be initialized only when CameraScreen is displayed, not at app startup
        // This prevents unnecessary battery usage and privacy concerns

        setContent { ThemeProvider { AppContent() } }
    }

    // Handles what happens after the permission is Granted / Denied
    private val permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                    permissions ->
                permissions.entries.forEach { entry ->
                    val permission = entry.key
                    val isGranted = entry.value
                    if (isGranted) {
                        println("✅ Permission granted: $permission")
                        // Camera will be initialized when CameraScreen is displayed, not here
                        // This prevents unnecessary battery usage
                    } else {
                        println("❌ Permission denied: $permission")
                    }
                }
            }

    // permission request popup that should happen at the start of app
    // should also implement to popup if microphone disabled and click in stgs
    private fun requestNecessaryPermissions() {
        // Only request permissions on Android 6.0 (API 23) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionsToRequest = mutableListOf<String>()

            // Check Camera permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
                            PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.CAMERA)
            }

            // Check Microphone (RECORD_AUDIO) permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
                            PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
            }

            // Request all needed permissions at once
            if (permissionsToRequest.isNotEmpty()) {
                permissionLauncher.launch(permissionsToRequest.toTypedArray())
            } else {
                Napier.d("All permission granted", tag = "DDD")
            }
        }
    }

    fun requestMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) !=
                        PackageManager.PERMISSION_GRANTED
        ) {
            println("🎤 Requesting microphone permission via Settings")
            permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
        } else {
            println("🎤 Microphone permission already granted")
        }
    }

    // Camera initialization moved to CameraScreen to only activate when needed
    // This method is kept for potential future use but not called at startup
    fun getLifecycleOwner(): LifecycleOwner = this
    companion object {
        // TODO: Consider using dependency injection or Application class instead of global context
        // This pattern works but is not ideal for testability and separation of concerns
        lateinit var appContext: Context
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
