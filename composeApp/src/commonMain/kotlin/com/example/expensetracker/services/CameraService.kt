package com.example.expensetracker.services

// commonMain/CameraManager.kt
//expect class CameraManager {
//    fun startCamera(onPreviewReady: () -> Unit)
//    fun stopCamera()
//    fun takePicture(onImageCaptured: (ByteArray) -> Unit)
//}

interface CameraService {
    suspend fun takePhoto(): ByteArray?
    fun hasCameraPermission(): Boolean
    suspend fun requestCameraPermission(): Boolean
    fun isCameraReady(): Boolean
    suspend fun ensureCameraInitialized(): Boolean
}

expect fun getCameraService(): CameraService
