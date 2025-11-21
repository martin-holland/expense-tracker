package com.example.expensetracker.viewmodel

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.service.AndroidSpeechRecognizerService
import kotlinx.coroutines.launch

/**
 * Android-specific extension for VoiceInputViewModel
 * Manages the speech recognizer lifecycle
 */
class AndroidVoiceInputHelper(
    private val context: Context,
    private val viewModel: VoiceInputViewModel
) {
    private var speechRecognizerService: AndroidSpeechRecognizerService? = null

    fun startSpeechRecognition() {
        // Always clean up any existing service first to prevent ERROR_RECOGNIZER_BUSY (error 11)
        if (speechRecognizerService != null) {
            try {
                speechRecognizerService?.destroy()
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
            speechRecognizerService = null
        }
        
        // Small delay to ensure cleanup completes
        Thread.sleep(100)
        
        speechRecognizerService = AndroidSpeechRecognizerService(context)

        // Collect partial results
        viewModel.viewModelScope.launch {
            try {
                speechRecognizerService?.partialResults?.collect { partial ->
                    viewModel.onPartialTranscription(partial)
                }
            } catch (e: Exception) {
                // Collection ended
            }
        }

        // Collect final results
        viewModel.viewModelScope.launch {
            try {
                speechRecognizerService?.finalResult?.collect { result ->
                    viewModel.onSpeechResult(result)
                }
            } catch (e: Exception) {
                // Collection ended
            }
        }

        viewModel.startSpeechRecognition()
        speechRecognizerService?.startListening()
    }

    fun stopSpeechRecognition() {
        speechRecognizerService?.stopListening()
        viewModel.stopSpeechRecognition()
    }

    fun cleanup() {
        speechRecognizerService?.destroy()
        speechRecognizerService = null
    }
}

