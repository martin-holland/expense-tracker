package com.example.expensetracker.services

// commonMain/CameraManager.kt
// expect class CameraManager {
//    fun startCamera(onPreviewReady: () -> Unit)
//    fun stopCamera()
//    fun takePicture(onImageCaptured: (ByteArray) -> Unit)
// }

enum class CameraState {
    NOT_INITIALIZED, // Camera not initialized (0% battery)
    INITIALIZING, // Camera is being initialized (10% battery, 1-2s cold start)
    READY, // Camera is ready to take photos (100% battery, camera active)
    CAPTURING, // Currently capturing a photo (100% battery)
    IDLE, // Camera in warm state, ready for quick resume (5% battery, 30s timeout)
    RELEASED, // Camera released after timeout (0% battery)
    ERROR // Error state
}

interface CameraService {
    suspend fun takePhoto(): ByteArray?
    fun hasCameraPermission(): Boolean
    suspend fun requestCameraPermission(): Boolean
    fun isCameraReady(): Boolean
    fun getCameraState(): CameraState
    suspend fun ensureCameraInitialized(): Boolean
    suspend fun startCamera(lifecycleOwner: Any): Boolean
    suspend fun pauseCamera() // Move to IDLE (warm state, 5% battery)
    suspend fun stopCamera() // Full release to NOT_INITIALIZED
}

expect fun getCameraService(): CameraService
