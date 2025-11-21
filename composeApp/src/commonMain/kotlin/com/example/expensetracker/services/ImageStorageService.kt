package com.example.expensetracker.services

expect class ImageStorageService {
    suspend fun saveImageToGallery(imageBytes: ByteArray, filename: String? = null): Result<String>
}

// Factory function to get the service instance
expect fun getImageStorageService(): ImageStorageService