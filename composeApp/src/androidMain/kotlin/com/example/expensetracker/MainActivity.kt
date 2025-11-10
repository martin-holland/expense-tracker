package com.example.expensetracker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.theme.com.example.expensetracker.ThemeProvider
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        //create a logger instance
//        AndroidLogcatLogger.installOnDebuggableApp(this, minPriority = VERBOSE)

        requestNecessaryPermissions()

        initializeCamera()


        setContent {
            ThemeProvider(useDarkTheme = false) {
                 AppContent()
            }
        }
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                permissions ->
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

    companion object {
        // TODO: Consider using dependency injection or Application class instead of global context
        // This pattern works but is not ideal for testability and separation of concerns
        lateinit var appContext: Context
    }

    private fun initializeCamera() {
        try {
//            val cameraService = AndroidCameraService.getInstance(this)
            print("start Camera service")
            // Start camera in coroutine
//            lifecycleScope.launch { cameraService.startCamera(this@MainActivity) }
        } catch (e: Exception) {
            println("❌ Error initializing camera: ${e.message}")
        }
    }

}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}