package com.example.expensetracker.services

import android.content.Context
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.expensetracker.MainActivity
import kotlinx.coroutines.Dispatchers
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
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    override suspend fun takePhoto(): ByteArray? =
        withContext(Dispatchers.Main) {
            try {
                println("📷 Android: Taking photo...")

                // Check if imageCapture is initialized
                if (imageCapture == null) {
                    println("❌ Android: Camera not initialized. imageCapture is null")
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
                withContext(Dispatchers.IO) {
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
            } catch (e: Exception) {
                println("❌ Android: Error taking photo: ${e.message}")
                e.printStackTrace()
                null
            }
        }

    suspend fun startCamera(lifecycleOwner: LifecycleOwner) =
        withContext(Dispatchers.Main) {
            try {
                println("🎥 Android: Starting camera...")
                val cameraProvider = ProcessCameraProvider.getInstance(context).get()

                // Preview
                val preview = Preview.Builder().build()

                // Image capture
                imageCapture =
                    ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build()

                // Select back camera
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // Unbind all use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

                println("✅ Android: Camera started successfully")
                true
            } catch (e: Exception) {
                println("❌ Android: Error starting camera: ${e.message}")
                e.printStackTrace()
                false
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