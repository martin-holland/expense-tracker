package com.example.expensetracker.services



import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.expensetracker.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//data class SavedImageResult(
//    val uri: String,
//    val filePath: String
//)

actual class ImageStorageService(private val context: Context) {

    actual suspend fun saveImageToGallery(imageBytes: ByteArray, filename: String?): Result<SavedImageResult> =
        withContext(Dispatchers.IO) {
            try {
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    ?: return@withContext Result.failure(Exception("Failed to decode image"))

                val name = filename ?: generateFilename()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    saveWithMediaStore(bitmap, name)
                } else {
                    saveToExternalStorage(bitmap, name)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(e)
            }
        }

    private fun saveWithMediaStore(bitmap: Bitmap, filename: String): Result<SavedImageResult> {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ExpenseTracker")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: return Result.failure(Exception("Failed to create MediaStore entry"))

        return try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
            } ?: return Result.failure(Exception("Failed to open output stream"))

            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)

            // Get the actual file path from the URI
            val filePath = getFilePathFromUri(uri) ?: run {
                // Fallback: construct expected path
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                File(picturesDir, "ExpenseTracker/$filename").absolutePath
            }

            println("✅ Image saved to gallery: $uri")
            println("📁 File path: $filePath")

            Result.success(SavedImageResult(uri.toString(), filePath))
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            Result.failure(e)
        }
    }

    private fun saveToExternalStorage(bitmap: Bitmap, filename: String): Result<SavedImageResult> {
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val appDir = File(picturesDir, "ExpenseTracker")

        if (!appDir.exists() && !appDir.mkdirs()) {
            return Result.failure(Exception("Failed to create directory"))
        }

        val file = File(appDir, filename)
        return try {
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
            }
            val filePath = file.absolutePath
            println("✅ Image saved to: $filePath")

            Result.success(SavedImageResult(Uri.fromFile(file).toString(), filePath))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getFilePathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        var cursor: Cursor? = null

        return try {
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.let {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    it.getString(columnIndex)
                } else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            cursor?.close()
        }
    }

    private fun generateFilename(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "Receipt_$timestamp.jpg"
    }

    companion object {
        @Volatile
        private var instance: ImageStorageService? = null

        fun getInstance(context: Context): ImageStorageService {
            return instance ?: synchronized(this) {
                instance ?: ImageStorageService(context.applicationContext).also { instance = it }
            }
        }
    }
}

actual fun getImageStorageService(): ImageStorageService {
    return ImageStorageService.getInstance(MainActivity.appContext)
}