package com.example.expensetracker.service

/**
 * Result from speech recognition
 */
sealed class SpeechRecognitionResult {
    data class Success(
        val text: String,
        val confidence: Float,
        val alternatives: List<SpeechAlternative>
    ) : SpeechRecognitionResult()

    data class Error(val message: String) : SpeechRecognitionResult()
}

/**
 * Alternative speech recognition result
 */
data class SpeechAlternative(
    val text: String,
    val confidence: Float
)

