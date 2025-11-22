package com.example.expensetracker.services

data class SavedImageResult(
    val uri: String,
    val filePath: String
)

expect class ImageStorageService {
    suspend fun saveImageToGallery(imageBytes: ByteArray, filename: String? = null): Result<SavedImageResult>
}

expect fun getImageStorageService(): ImageStorageService
