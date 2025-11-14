package com.example.expensetracker.services

// commonMain/CameraManager.kt
// expect class CameraManager {
//    fun startCamera(onPreviewReady: () -> Unit)
//    fun stopCamera()
//    fun takePicture(onImageCaptured: (ByteArray) -> Unit)
// }

enum class CameraState {
    IDLE, // Camera not initialized
    INITIALIZING, // Camera is being initialized
    READY, // Camera is ready to take photos
    CAPTURING, // Currently capturing a photo
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
    suspend fun stopCamera()
}

expect fun getCameraService(): CameraService
