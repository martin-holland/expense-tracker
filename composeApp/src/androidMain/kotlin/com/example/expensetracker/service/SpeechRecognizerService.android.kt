package com.example.expensetracker.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Service for Android speech recognition using SpeechRecognizer API This is a native, on-device
 * solution (no API costs)
 */
class AndroidSpeechRecognizerService(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    // Channels for streaming results
    private val _partialResults = Channel<String>(Channel.BUFFERED)
    private val _finalResult = Channel<SpeechRecognitionResult>(Channel.BUFFERED)

    val partialResults: Flow<String> = _partialResults.receiveAsFlow()
    val finalResult: Flow<SpeechRecognitionResult> = _finalResult.receiveAsFlow()

    /**
     * Start speech recognition
     * @return true if started successfully
     */
    fun startListening(): Boolean {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            println("❌ Speech recognition not available on this device")
            return false
        }

        if (isListening) {
            println("⚠️ Already listening")
            return false
        }

        try {
            speechRecognizer =
                    SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(createRecognitionListener())
                    }

            val intent =
                    Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                        // Get confidence scores
                        putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES, true)
                    }

            speechRecognizer?.startListening(intent)
            isListening = true
            println("🎤 Speech recognition started")
            return true
        } catch (e: Exception) {
            println("❌ Error starting speech recognition: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    /** Stop speech recognition */
    fun stopListening() {
        if (!isListening) {
            println("⚠️ Not currently listening")
            return
        }

        try {
            speechRecognizer?.stopListening()
            isListening = false
            println("⏹️ Speech recognition stopped")
        } catch (e: Exception) {
            println("❌ Error stopping speech recognition: ${e.message}")
        }
    }

    /** Cancel speech recognition */
    fun cancel() {
        try {
            speechRecognizer?.cancel()
            isListening = false
            println("🚫 Speech recognition cancelled")
        } catch (e: Exception) {
            println("❌ Error cancelling speech recognition: ${e.message}")
        }
    }

    /** Cleanup resources */
    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            isListening = false
            _partialResults.close()
            _finalResult.close()
            println("🗑️ Speech recognizer destroyed")
        } catch (e: Exception) {
            println("❌ Error destroying speech recognizer: ${e.message}")
        }
    }

    /** Create recognition listener for handling speech events */
    private fun createRecognitionListener() =
            object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    println("✅ Ready for speech")
                }

                override fun onBeginningOfSpeech() {
                    println("🗣️ User started speaking")
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Audio level changed - could use for visualization
                }

                override fun onBufferReceived(buffer: ByteArray?) {
                    // Raw audio buffer - not needed for our use case
                }

                override fun onEndOfSpeech() {
                    println("🔇 User stopped speaking")
                    isListening = false
                }

                override fun onError(error: Int) {
                    val errorMessage = getErrorMessage(error)
                    println("❌ Speech recognition error: $errorMessage")

                    _finalResult.trySend(SpeechRecognitionResult.Error(errorMessage))
                    isListening = false
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val confidenceScores =
                            results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)

                    if (matches != null && matches.isNotEmpty()) {
                        val bestMatch = matches[0]
                        val confidence = confidenceScores?.getOrNull(0) ?: 0f

                        println("✅ Final result: \"$bestMatch\" (confidence: $confidence)")

                        // Send all alternatives for analysis
                        val alternatives =
                                matches.mapIndexed { index, text ->
                                    SpeechAlternative(
                                            text = text,
                                            confidence = confidenceScores?.getOrNull(index) ?: 0f
                                    )
                                }

                        _finalResult.trySend(
                                SpeechRecognitionResult.Success(
                                        text = bestMatch,
                                        confidence = confidence,
                                        alternatives = alternatives
                                )
                        )
                    } else {
                        println("⚠️ No results received")
                        _finalResult.trySend(SpeechRecognitionResult.Error("No results"))
                    }
                    isListening = false
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches =
                            partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (matches != null && matches.isNotEmpty()) {
                        val partialText = matches[0]
                        println("📝 Partial result: \"$partialText\"")
                        _partialResults.trySend(partialText)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {
                    // Additional events - not needed for basic use
                }
            }

    /** Convert error codes to human-readable messages */
    private fun getErrorMessage(error: Int): String {
        return when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error ($error)"
        }
    }
}
