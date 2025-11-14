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
        if (speechRecognizerService == null) {
            speechRecognizerService = AndroidSpeechRecognizerService(context)

            // Collect partial results
            viewModel.viewModelScope.launch {
                speechRecognizerService?.partialResults?.collect { partial ->
                    viewModel.onPartialTranscription(partial)
                }
            }

            // Collect final results
            viewModel.viewModelScope.launch {
                speechRecognizerService?.finalResult?.collect { result ->
                    viewModel.onSpeechResult(result)
                }
            }
        }

        viewModel.startSpeechRecognition()
        speechRecognizerService?.startListening()
    }

    fun stopSpeechRecognition() {
        speechRecognizerService?.stopListening()
    }

    fun cleanup() {
        speechRecognizerService?.destroy()
        speechRecognizerService = null
    }
}

