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
import com.example.theme.com.example.expensetracker.ThemeProvider

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        //create a logger instance
//        AndroidLogcatLogger.installOnDebuggableApp(this, minPriority = VERBOSE)

        appContext = this


        requestNecessaryPermissions()

        setContent {
            ThemeProvider {
                AppContent()
            }
        }
    }

    // Handles what happens after the permission is Granted / Denied
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach { entry ->
                val permission = entry.key
                val isGranted = entry.value
                if (isGranted) {
                    println("✅ Permission granted: $permission")
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
                println("✅ All permissions already granted")
            }
        }
    }

    fun requestMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            println("🎤 Requesting microphone permission via Settings")
            permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
        } else {
            println("🎤 Microphone permission already granted")
        }
    }
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