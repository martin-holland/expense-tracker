package com.example.expensetracker.services

import android.graphics.Bitmap
import android.media.Image
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class TextRecognitionAnalyzer(
    private val onDetectedTextUpdated: (String) -> Unit
) : ImageAnalysis.Analyzer {

    companion object {
        const val THROTTLE_TIMEOUT_MS = 1_000L
    }

    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val textRecognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @ExperimentalGetImage
    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        scope.launch {
            val mediaImage: Image = imageProxy.image ?: run { imageProxy.close(); return@launch }
            val inputImage: InputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            suspendCoroutine { continuation ->
                textRecognizer.process(inputImage)
                    .addOnSuccessListener { visionText: Text ->
                        val detectedText: String = visionText.text
                        if (detectedText.isNotBlank()) {
                            onDetectedTextUpdated(detectedText)
                        }
                    }
                    .addOnCompleteListener {
                        continuation.resume(Unit)
                    }
            }

            delay(THROTTLE_TIMEOUT_MS)
        }.invokeOnCompletion { exception ->
            exception?.printStackTrace()
            imageProxy.close()
        }
    }
    // NEW METHOD: Analyze static bitmap (for captured photos)
    fun analyzeBitmap(bitmap: Bitmap, rotationDegrees: Int = 0) {
        scope.launch {
            try {
                val inputImage = InputImage.fromBitmap(bitmap, rotationDegrees)

                suspendCoroutine { continuation ->
                    textRecognizer.process(inputImage)
                        .addOnSuccessListener { visionText: Text ->
                            val detectedText: String = visionText.text
                            if (detectedText.isNotBlank()) {
                                onDetectedTextUpdated(detectedText)
                            } else {
                                onDetectedTextUpdated("No text detected")
                            }
                        }
                        .addOnFailureListener { exception ->
                            exception.printStackTrace()
                            onDetectedTextUpdated("Recognition error: ${exception.message}")
                        }
                        .addOnCompleteListener {
                            continuation.resume(Unit)
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onDetectedTextUpdated("Analysis error: ${e.message}")
            }
        }
    }
}