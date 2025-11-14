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
import androidx.compose.runtime.DisposableEffect
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.expensetracker.services.CameraState
import com.example.expensetracker.services.decodeByteArrayToImageBitmap
import com.example.expensetracker.services.getCameraService
import kotlinx.coroutines.launch

@Composable
fun CameraScreen() {
    val cameraService = getCameraService()
    var photoData by remember { mutableStateOf<ByteArray?>(null) }
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var cameraState by remember { mutableStateOf(cameraService.getCameraState()) }
    val scope = rememberCoroutineScope()

    // Get lifecycle owner for camera binding
    val lifecycleOwner = LocalLifecycleOwner.current

    val hasPermission = cameraService.hasCameraPermission()

    // Start camera when screen is displayed (only if permission is granted)
    // This LaunchedEffect runs when the composable is displayed or when permission changes
    LaunchedEffect(hasPermission) {
        if (hasPermission && cameraService.getCameraState() == CameraState.IDLE) {
            println("📷 CameraScreen: Starting camera initialization...")
            cameraState = CameraState.INITIALIZING
            val started = cameraService.startCamera(lifecycleOwner)
            cameraState = cameraService.getCameraState()
            if (started) {
                println("✅ CameraScreen: Camera started successfully")
            } else {
                println("❌ CameraScreen: Failed to start camera")
            }
        }
    }

    // Stop camera when screen is disposed (when user navigates away or closes camera)
    DisposableEffect(Unit) {
        onDispose {
            println("🛑 CameraScreen: Disposing, stopping camera...")
            scope.launch { 
                cameraService.stopCamera()
                cameraState = cameraService.getCameraState()
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
    
    // Stop camera after photo is captured
    LaunchedEffect(photoData) {
        if (photoData != null) {
            println("📸 CameraScreen: Photo captured, stopping camera to save resources...")
            cameraService.stopCamera()
            cameraState = cameraService.getCameraState()
        }
    }

    Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Camera Preview/Status Card
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
                    cameraState == CameraState.INITIALIZING -> {
                        Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                    "Initializing camera...",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                    "This may take a few seconds",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    cameraState == CameraState.READY -> {
                        Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                        ) {
                            Text("📸", style = MaterialTheme.typography.displayLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                    "Camera Ready",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                    "Tap 'Take Photo' to capture",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    cameraState == CameraState.ERROR -> {
                        Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                        ) {
                            Text("❌", style = MaterialTheme.typography.displayMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                    "Camera Error",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                    "Try restarting the camera",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            Text(
                                    "Camera Idle",
                                    style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                    "Waiting to initialize...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Take Photo button (only shown when camera is ready)
            if (cameraState == CameraState.READY) {
                Button(
                        onClick = {
                            scope.launch {
                                try {
                                    isProcessing = true
                                    cameraState = CameraState.CAPTURING

                                    photoData = cameraService.takePhoto()
                                    cameraState = cameraService.getCameraState()
                                    
                                    if (photoData == null) {
                                        println("⚠️ Camera: Photo capture returned null")
                                    }
                                } catch (e: Exception) {
                                    println("❌ Camera: Error taking photo: ${e.message}")
                                    e.printStackTrace()
                                    photoData = null
                                    cameraState = cameraService.getCameraState()
                                } finally {
                                    isProcessing = false
                                }
                            }
                        },
                        enabled = !isProcessing && hasPermission
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White
                        )
                    } else {
                        Text("📷")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Take Photo")
                }
            }
            
            // Restart Camera button (shown when camera is stopped after photo)
            if (cameraState == CameraState.IDLE && photoData != null) {
                Button(
                        onClick = {
                            scope.launch {
                                println("🔄 CameraScreen: Restarting camera...")
                                cameraState = CameraState.INITIALIZING
                                val started = cameraService.startCamera(lifecycleOwner)
                                cameraState = cameraService.getCameraState()
                                if (started) {
                                    println("✅ CameraScreen: Camera restarted successfully")
                                } else {
                                    println("❌ CameraScreen: Failed to restart camera")
                                }
                            }
                        },
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                )
                ) {
                    Text("🔄")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restart Camera")
                }
            }

            // Clear button (shown when photo exists)
            if (photoData != null) {
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
                    Text("Clear")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        //        Text(
        //            text =
        //                "Camera Permission: ${if (cameraService.hasCameraPermission()) "Granted"
        // else "Not Granted"}",
        //            style = MaterialTheme.typography.bodySmall,
        //            color =
        //                if (cameraService.hasCameraPermission()) MaterialTheme.colorScheme.primary
        //                else MaterialTheme.colorScheme.error
        //        )
    }
}
