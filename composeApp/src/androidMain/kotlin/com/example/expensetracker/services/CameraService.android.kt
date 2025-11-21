package com.example.expensetracker.services

import android.content.Context
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.expensetracker.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AndroidCameraService(private val context: Context) : CameraService {

    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    @Volatile
    private var cameraState: CameraState = CameraState.NOT_INITIALIZED

    // For 30-second timeout in IDLE state
    private var idleTimeoutJob: kotlinx.coroutines.Job? = null

    // Store the preview view for live preview
    private var currentPreviewView: PreviewView? = null

    override suspend fun takePhoto(): ByteArray? =
        withContext(Dispatchers.Main) {
            try {
                println("📷 Android: Taking photo...")

                // Check camera state
                if (cameraState != CameraState.READY) {
                    println("❌ Android: Camera not ready. Current state: $cameraState")
                    return@withContext null
                }

                // Update state to capturing
                cameraState = CameraState.CAPTURING

                // Check if imageCapture is initialized
                if (imageCapture == null) {
                    println("❌ Android: Camera not initialized. imageCapture is null")
                    cameraState = CameraState.ERROR
                    return@withContext null
                }

                // Create output file
                val photoFile =
                    File(
                        context.cacheDir,
                        SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                            .format(System.currentTimeMillis()) + ".jpg"
                    )

                println("📁 Android: Photo will be saved to: ${photoFile.absolutePath}")

                val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

                // Capture photo
                val result =
                    suspendCancellableCoroutine<ImageCapture.OutputFileResults> {
                            continuation ->
                        imageCapture?.takePicture(
                            outputOptions,
                            cameraExecutor,
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(
                                    output: ImageCapture.OutputFileResults
                                ) {
                                    println("✅ Android: Photo saved successfully")
                                    continuation.resume(output)
                                }

                                override fun onError(exc: ImageCaptureException) {
                                    println(
                                        "❌ Android: Photo capture failed: ${exc.message}"
                                    )
                                    continuation.resumeWithException(exc)
                                }
                            }
                        ) ?: run {
                            // If imageCapture is null, resume with exception
                            println("❌ Android: imageCapture is null when trying to take picture")
                            continuation.resumeWithException(
                                IllegalStateException("Camera not initialized")
                            )
                        }
                    }

                // Read the file and return as ByteArray
                val photoBytes = withContext(Dispatchers.IO) {
                    if (photoFile.exists()) {
                        val bytes = photoFile.readBytes()
                        println("📊 Android: Photo size: ${bytes.size} bytes")
                        photoFile.delete() // Clean up
                        bytes
                    } else {
                        println("❌ Android: Photo file doesn't exist")
                        null
                    }
                }

                // Photo captured successfully, return to IDLE state
                // Camera will be stopped by the UI layer after photo is taken
                cameraState = CameraState.READY

                photoBytes
            } catch (e: Exception) {
                println("❌ Android: Error taking photo: ${e.message}")
                e.printStackTrace()
                cameraState = CameraState.ERROR
                null
            }
        }

    override suspend fun startCamera(lifecycleOwner: Any): Boolean =
        withContext(Dispatchers.Main) {
            try {
                if (lifecycleOwner !is LifecycleOwner) {
                    println("❌ Android: Invalid LifecycleOwner type")
                    return@withContext false
                }

                // Cancel any pending idle timeout
                idleTimeoutJob?.cancel()
                idleTimeoutJob = null

                // Check if camera is already starting or ready
                if (cameraState == CameraState.INITIALIZING) {
                    println("⚠️ Android: Camera already initializing, skipping...")
                    return@withContext false
                }

                if (cameraState == CameraState.READY) {
                    println("✅ Android: Camera already ready")
                    return@withContext true
                }

                // INSTANT RESUME: If in IDLE state, camera provider is warm - just rebind!
                if (cameraState == CameraState.IDLE && cameraProvider != null && imageCapture != null) {
                    println("🔄 Android: Quick resume from IDLE state (instant, no reinitialization)")

                    val preview = Preview.Builder().build()
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    // Rebind use cases (camera provider already initialized)
                    cameraProvider?.unbindAll()
                    cameraProvider?.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )

                    cameraState = CameraState.READY
                    println("✅ Android: Camera resumed instantly from warm state")
                    return@withContext true
                }

                // COLD START: Full initialization needed
                println("🎥 Android: Starting camera initialization (cold start: 1-2s)...")
                cameraState = CameraState.INITIALIZING

                // Use suspendCancellableCoroutine to convert ListenableFuture to coroutine
                val provider = suspendCancellableCoroutine<ProcessCameraProvider> { continuation ->
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener({
                        try {
                            val provider = cameraProviderFuture.get()
                            continuation.resume(provider)
                        } catch (e: Exception) {
                            continuation.resumeWithException(e)
                        }
                    }, ContextCompat.getMainExecutor(context))
                }

                cameraProvider = provider

                // Preview
                preview = Preview.Builder().build()

                // Image capture
                imageCapture =
                    ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                // Select back camera
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // Unbind all use cases before rebinding
                cameraProvider?.unbindAll()

                // Bind use cases to camera
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

                cameraState = CameraState.READY
                println("✅ Android: Camera started successfully and ready")
                true
            } catch (e: Exception) {
                println("❌ Android: Error starting camera: ${e.message}")
                e.printStackTrace()
                cameraState = CameraState.ERROR
                false
            }
        }

    // NEW METHOD: Set up preview view for live camera feed
    fun setupPreviewView(previewView: PreviewView, lifecycleOwner: LifecycleOwner): Boolean {
        return try {
            currentPreviewView = previewView

            // Configure preview view
            previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
            previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE

            // Set the surface provider for the preview
            preview?.setSurfaceProvider(previewView.surfaceProvider)

            println("✅ Android: Preview view setup successfully")
            true
        } catch (e: Exception) {
            println("❌ Android: Error setting up preview view: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    override suspend fun pauseCamera() = withContext(Dispatchers.Main) {
        try {
            println("⏸️ Android: Pausing camera (entering warm IDLE state)...")

            // Cancel any existing timeout
            idleTimeoutJob?.cancel()

            // Unbind use cases but keep camera provider and imageCapture (warm state)
            cameraProvider?.unbindAll()

            // Clear preview view
            currentPreviewView = null

            cameraState = CameraState.IDLE
            println("✅ Android: Camera paused (IDLE - 5% battery, 30s timeout starting)")

            // Start 30-second timeout
            idleTimeoutJob = CoroutineScope(Dispatchers.Main).launch {
                delay(30_000) // 30 seconds
                if (cameraState == CameraState.IDLE) {
                    println("⏱️ Android: 30-second timeout reached, releasing camera...")
                    releaseCamera()
                }
            }
        } catch (e: Exception) {
            println("❌ Android: Error pausing camera: ${e.message}")
            e.printStackTrace()
            cameraState = CameraState.ERROR
        }
    }

    private suspend fun releaseCamera() = withContext(Dispatchers.Main) {
        try {
            println("🔓 Android: Releasing camera (RELEASED state)...")
            idleTimeoutJob?.cancel()
            idleTimeoutJob = null
            cameraProvider?.unbindAll()
            cameraProvider = null
            imageCapture = null
            preview = null
            currentPreviewView = null
            cameraState = CameraState.RELEASED
            println("✅ Android: Camera released, transitioning to NOT_INITIALIZED")
            // Immediately transition to NOT_INITIALIZED (RELEASED is transient)
            cameraState = CameraState.NOT_INITIALIZED
        } catch (e: Exception) {
            println("❌ Android: Error releasing camera: ${e.message}")
            e.printStackTrace()
            cameraState = CameraState.ERROR
        }
    }

    override suspend fun stopCamera() = withContext(Dispatchers.Main) {
        try {
            println("🛑 Android: Stopping camera (full shutdown)...")
            idleTimeoutJob?.cancel()
            idleTimeoutJob = null
            cameraProvider?.unbindAll()
            cameraProvider = null
            imageCapture = null
            preview = null
            currentPreviewView = null
            cameraState = CameraState.NOT_INITIALIZED
            println("✅ Android: Camera stopped successfully")
        } catch (e: Exception) {
            println("❌ Android: Error stopping camera: ${e.message}")
            e.printStackTrace()
            cameraState = CameraState.ERROR
        }
    }

    override fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
    }

    override suspend fun requestCameraPermission(): Boolean {
        // Permission is requested in MainActivity
        return true
    }

    override fun isCameraReady(): Boolean {
        return cameraState == CameraState.READY
    }

    override fun getCameraState(): CameraState {
        return cameraState
    }

    override suspend fun ensureCameraInitialized(): Boolean {
        // If camera is already ready, return true
        if (imageCapture != null) {
            return true
        }

        // Try to initialize camera if we have permission
        if (hasCameraPermission()) {
            // We need a LifecycleOwner, but we can't get it here
            // The camera should be initialized in MainActivity
            // This is a fallback that returns false if not ready
            println("⚠️ Android: Camera not initialized. Please ensure camera is initialized in MainActivity.")
            return false
        }

        return false
    }

    fun shutdown() {
        cameraExecutor.shutdown()
    }

    companion object {
        @Volatile private var instance: AndroidCameraService? = null

        fun getInstance(context: Context): AndroidCameraService {
            return instance
                ?: synchronized(this) {
                    instance ?: AndroidCameraService(context).also { instance = it }
                }
        }
    }
}

actual fun getCameraService(): CameraService {
    return AndroidCameraService.getInstance(MainActivity.appContext)
}