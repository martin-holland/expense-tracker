package com.example.expensetracker.view.components.camera

import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.expensetracker.services.AndroidCameraService
import com.example.expensetracker.services.CameraState
import com.example.expensetracker.services.TextRecognitionAnalyzer
import com.example.expensetracker.services.decodeByteArrayToImageBitmap
import com.example.expensetracker.services.getCameraService
import com.example.expensetracker.services.getImageStorageService
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
actual fun CameraScreen() {
    val cameraService = getCameraService()
    val imageStorageService = remember { getImageStorageService() }  // Add this

    var photoData by remember { mutableStateOf<ByteArray?>(null) }
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var cameraState by remember { mutableStateOf(cameraService.getCameraState()) }
    var saveStatus by remember { mutableStateOf<String?>(null) }  // Add this
    var savedImagePath by remember { mutableStateOf<String?>(null)}

    val scope = rememberCoroutineScope()

    var detectedText: String by remember { mutableStateOf("No text detected yet..") }

    // Create text analyzer
    val textAnalyzer = remember {
        TextRecognitionAnalyzer { updatedText ->
            detectedText = updatedText
        }
    }

    // Get lifecycle owner for camera binding
    val lifecycleOwner = LocalLifecycleOwner.current

    val hasPermission = cameraService.hasCameraPermission()

    // Start camera when screen is displayed (only if permission is granted)
    // Supports instant resume from IDLE state!
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            val currentState = cameraService.getCameraState()
            if (currentState == CameraState.NOT_INITIALIZED || currentState == CameraState.RELEASED) {
                println("📷 CameraScreen: Starting camera initialization (cold start)...")
                cameraState = CameraState.INITIALIZING
                val started = cameraService.startCamera(lifecycleOwner)
                cameraState = cameraService.getCameraState()
                if (started) {
                    println("✅ CameraScreen: Camera started successfully")
                } else {
                    println("❌ CameraScreen: Failed to start camera")
                }
            } else if (currentState == CameraState.IDLE) {
                println("🔄 CameraScreen: Resuming from IDLE (instant resume)...")
                cameraState = CameraState.IDLE // Show we're resuming
                val started = cameraService.startCamera(lifecycleOwner)
                cameraState = cameraService.getCameraState()
                if (started) {
                    println("✅ CameraScreen: Camera resumed instantly")
                }
            }
        }
    }

    // Pause camera when screen is disposed (moves to IDLE with 30s timeout)
    DisposableEffect(Unit) {
        onDispose {
            println("⏸️ CameraScreen: Disposing, pausing camera (IDLE state with 30s timeout)...")
            scope.launch {
                cameraService.pauseCamera()
                cameraState = cameraService.getCameraState()
                textAnalyzer.cleanup()
            }
        }
    }

    // Convert byte array to ImageBitmap when photo data changes
    LaunchedEffect(photoData) {
        photoData?.let { bytes ->
            try {
                isProcessing = true
                imageBitmap = decodeByteArrayToImageBitmap(bytes)

                //convert ImageBitmap to Bitmap for analysys
                val bitmap = imageBitmap?.asAndroidBitmap()
                if (bitmap != null){
                    textAnalyzer.analyzeBitmap(bitmap)

                } else{
                    detectedText = "Failed to process image"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                detectedText = "Error processing image: ${e.message}"
            } finally {
                isProcessing = false
            }
        }
            photoData
            ?: run {
                imageBitmap = null
                detectedText = "No text detected yet"
            }
    }

    // NOTE: Camera stays READY after photo capture (diagram requirement)
    // User must explicitly close camera to enter IDLE state

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
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Camera still ready for more photos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    cameraState == CameraState.READY && photoData == null -> {
                        // Live camera preview using AndroidCameraService
                        AndroidView(
                            factory = { context ->
                                PreviewView(context).apply {
                                    layoutParams = android.widget.LinearLayout.LayoutParams(
                                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT
                                    )
                                    scaleType = PreviewView.ScaleType.FILL_CENTER
                                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                }.also { previewView ->
                                    // Set up the preview view with camera service
                                    (cameraService as? AndroidCameraService)?.setupPreviewView(
                                        previewView,
                                        lifecycleOwner
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
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
                                "Cold start: 1-2 seconds",
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
                                "100% battery - Camera active",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    cameraState == CameraState.IDLE -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("⏸️", style = MaterialTheme.typography.displayLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Camera Idle (Warm)",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                "5% battery - Quick resume available",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Auto-release in 30s",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary
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
                                "Try restarting the app",
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
                            Text("🌙", style = MaterialTheme.typography.displayLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Camera Not Initialized",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                "0% battery - Ready to start",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

// Processing indicator
        if (isProcessing) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Yellow.copy(alpha = 0.3f))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Analyzing text...",
                    color = Color.Black
                )
            }
        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            text = detectedText,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Take Photo button (only shown when camera is ready and no photo taken)
            if (cameraState == CameraState.READY && photoData == null) {
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
                                } else {
                                    println("✅ Camera: Photo captured, camera stays READY for more photos")
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

            // Retake Photo button (shown when photo exists and camera is ready)
            if (photoData != null && cameraState == CameraState.READY) {
                Button(
                    onClick = {
                        photoData = null
                        imageBitmap = null
                        println("🔄 Camera: Photo cleared, ready to take another (camera stays READY)")
                    },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                ) {
                    Text("🔄")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retake Photo")
                }
                Button(
                    onClick = {
                        scope.launch {
                            saveStatus = "Saving..."
                            val result = imageStorageService.saveImageToGallery(photoData!!)
                            Napier.d("result ${result}", tag = "DDD")
                            saveStatus = result.fold(
                                onSuccess = { savedResult ->
                                    println("📁 URI: ${savedResult.uri}")
                                    println("📁 File path: ${savedResult.filePath}")
                                    savedImagePath = savedResult.filePath
                                    saveStatus = "✅ Saved!"
                                    Napier.d("URI: ${savedResult.uri}", tag = "DDD")
                                    Napier.d("filepath: ${savedResult.filePath}", tag = "DDD")

                                    "Saved to storage"
                                },
                                onFailure = { "❌ Failed: ${it.message}" }
                            )
                            // Clear status after 3 seconds
                            delay(3000)
                            saveStatus = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text("💾")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save")
                }
            }
        }
        saveStatus?.let { status ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    status.startsWith("✅") -> MaterialTheme.colorScheme.primary
                    status.startsWith("❌") -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }

            // Close Camera button (pauses to IDLE state with 30s timeout)
//            if (cameraState == CameraState.READY || cameraState == CameraState.CAPTURING) {
//                Button(
//                    onClick = {
//                        scope.launch {
//                            println("⏸️ CameraScreen: User closing camera, entering IDLE state...")
//                            cameraService.pauseCamera()
//                            cameraState = cameraService.getCameraState()
//                        }
//                    },
//                    enabled = !isProcessing,
//                    colors =
//                        ButtonDefaults.buttonColors(
//                            containerColor = MaterialTheme.colorScheme.tertiary
//                        )
//                ) {
//                    Text("⏸️")
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text("Close Camera")
//                }
//            }

            // Clear Photo button (alternative action when photo exists)
            if (photoData != null && cameraState == CameraState.READY) {
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

    }

