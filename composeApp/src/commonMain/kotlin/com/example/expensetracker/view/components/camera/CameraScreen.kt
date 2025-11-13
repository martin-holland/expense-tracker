package com.example.expensetracker.view.components.camera

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.expensetracker.services.decodeByteArrayToImageBitmap
import com.example.expensetracker.services.getCameraService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun CameraScreen() {
    val cameraService = getCameraService()
    var photoData by remember { mutableStateOf<ByteArray?>(null) }
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var isCameraOn by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Add this state to trigger recomposition when permissions change
    var permissionCheck by remember { mutableStateOf(0) }
    var cameraInitAttempted by remember { mutableStateOf(false) }

    // Force recomposition when permissions might have changed
    LaunchedEffect(Unit) {
        // This will make the composable check permission state again
        permissionCheck++
    }

    // Your existing code, but update the permission check:
    val hasPermission = cameraService.hasCameraPermission()
    val isCameraReady = cameraService.isCameraReady()

    // Try to ensure camera is initialized when screen is shown
    LaunchedEffect(hasPermission, cameraInitAttempted) {
        if (hasPermission && !isCameraReady && !cameraInitAttempted) {
            cameraInitAttempted = true
            println("📷 CameraScreen: Camera not ready, attempting to ensure initialization...")
            // Give a moment for MainActivity to initialize if it's in progress
            delay(500)
            val initialized = cameraService.ensureCameraInitialized()
            if (!initialized) {
                println("⚠️ CameraScreen: Camera initialization check failed. Camera may need to be initialized in MainActivity.")
            }
        }
    }

    // Convert byte array to ImageBitmap when photo data changes

    LaunchedEffect(photoData) {
        photoData?.let { bytes ->
            try {
                imageBitmap = decodeByteArrayToImageBitmap(bytes)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
            ?: run { imageBitmap = null }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        if (isCameraOn  && hasPermission) {
            Card(
                modifier = Modifier.fillMaxWidth().height(300.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
            ) {

                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    when {
                        imageBitmap != null -> {
                            Image(
                                bitmap = imageBitmap!!,
                                contentDescription = "Captured photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                        photoData != null -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("✅", style = MaterialTheme.typography.displayMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Photo captured!",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "(${photoData!!.size} bytes)",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        else -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("📸", style = MaterialTheme.typography.displayLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No photo taken yet", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "Tap 'Take Photo' to start",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }


        // Show warning if camera is not ready
        if (!isCameraReady && hasPermission) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = "⚠️ Camera not ready. Please wait a moment and try again.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    scope.launch {
                        try {
                            isProcessing = true
                            isCameraOn = true
                            
                            // Check if camera is ready before taking photo
                            if (!cameraService.isCameraReady()) {
                                println("⚠️ Camera: Camera not ready, cannot take photo")
                                photoData = null
                                return@launch
                            }
                            
                            photoData = cameraService.takePhoto()
                            // Reset processing state after photo is taken (or fails)
                            if (photoData == null) {
                                println("⚠️ Camera: Photo capture returned null")
                            }
                        } catch (e: Exception) {
                            println("❌ Camera: Error taking photo: ${e.message}")
                            e.printStackTrace()
                            photoData = null
                        } finally {
                            isProcessing = false
                        }
                    }
                },
                enabled = !isProcessing && hasPermission && isCameraReady
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("📷")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Take Photo")
            }

            Button(
                onClick = { 
                    photoData = null
                    imageBitmap = null
                },
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
            ) {
                Text("🗑️")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear", color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

//        Text(
//            text =
//                "Camera Permission: ${if (cameraService.hasCameraPermission()) "Granted" else "Not Granted"}",
//            style = MaterialTheme.typography.bodySmall,
//            color =
//                if (cameraService.hasCameraPermission()) MaterialTheme.colorScheme.primary
//                else MaterialTheme.colorScheme.error
//        )
    }
}

