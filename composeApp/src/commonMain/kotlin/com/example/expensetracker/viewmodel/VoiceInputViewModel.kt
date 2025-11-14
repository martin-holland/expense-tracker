package com.example.expensetracker.viewmodel

import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.service.ExpenseParser
import com.example.expensetracker.service.ParsedExpenseData
import com.example.expensetracker.service.SpeechRecognitionResult
import com.example.expensetracker.service.getMicrophoneService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VoiceInputViewModel : ViewModel() {
    // Existing audio recording state
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _audioData = MutableStateFlow<ByteArray?>(null)
    val audioData: StateFlow<ByteArray?> = _audioData.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showVoiceSection = MutableStateFlow(false)
    val showVoiceSection: StateFlow<Boolean> = _showVoiceSection.asStateFlow()

    // NEW: Speech recognition state
    private val _speechRecognitionState = MutableStateFlow<SpeechRecognitionState>(
        SpeechRecognitionState.Idle
    )
    val speechRecognitionState: StateFlow<SpeechRecognitionState> =
        _speechRecognitionState.asStateFlow()

    private val _partialTranscription = MutableStateFlow("")
    val partialTranscription: StateFlow<String> = _partialTranscription.asStateFlow()

    sealed class SpeechRecognitionState {
        object Idle : SpeechRecognitionState()
        object Listening : SpeechRecognitionState()
        data class Success(
            val transcription: String,
            val confidence: Float,
            val alternatives: List<String>
        ) : SpeechRecognitionState()
        data class Error(val message: String) : SpeechRecognitionState()
    }

    fun toggleVoiceSection() {
        _showVoiceSection.value = !_showVoiceSection.value
    }

    fun startRecording() {
        viewModelScope.launch {
            _isProcessing.value = true
            _errorMessage.value = null
            val success = getMicrophoneService().startRecording()
            _isRecording.value = success
            _isProcessing.value = false
            if (!success) _errorMessage.value = "Failed to start recording"
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            _isProcessing.value = true
            val data = getMicrophoneService().stopRecording()
            _isRecording.value = false
            _audioData.value = data
            _isProcessing.value = false
        }
    }

    fun playAudio() {
        viewModelScope.launch {
            _audioData.value?.let { data ->
                getMicrophoneService().playAudio(data)
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // NEW: Speech recognition functions

    /**
     * Start speech recognition (will be called from platform-specific helper)
     */
    fun startSpeechRecognition() {
        viewModelScope.launch {
            _speechRecognitionState.value = SpeechRecognitionState.Listening
            _partialTranscription.value = ""
        }
    }

    /**
     * Stop speech recognition (will be called from platform-specific helper)
     */
    fun stopSpeechRecognition() {
        // Platform-specific implementation will handle this
    }

    /**
     * Handle partial transcription updates
     */
    fun onPartialTranscription(text: String) {
        _partialTranscription.value = text
    }

    /**
     * Handle final transcription result
     */
    fun onSpeechResult(result: SpeechRecognitionResult) {
        when (result) {
            is SpeechRecognitionResult.Success -> {
                _speechRecognitionState.value = SpeechRecognitionState.Success(
                    transcription = result.text,
                    confidence = result.confidence,
                    alternatives = result.alternatives.map { it.text }
                )
            }
            is SpeechRecognitionResult.Error -> {
                _speechRecognitionState.value = SpeechRecognitionState.Error(result.message)
            }
        }
    }

    /**
     * Reset speech recognition state
     */
    fun resetSpeechRecognition() {
        _speechRecognitionState.value = SpeechRecognitionState.Idle
        _partialTranscription.value = ""
    }

    // NEW: Parsed expense data state
    private val _parsedExpenseData = MutableStateFlow<ParsedExpenseData?>(null)
    val parsedExpenseData: StateFlow<ParsedExpenseData?> = _parsedExpenseData.asStateFlow()

    /**
     * Parse the transcription into expense data
     */
    fun parseTranscription(transcription: String) {
        val parsed = ExpenseParser.parse(transcription)
        _parsedExpenseData.value = parsed

        println("""
            📊 Parsing Results:
            Raw: "${parsed.rawText}"
            Amount: ${parsed.amount}
            Currency: ${parsed.currency}
            Category: ${parsed.category}
            Description: "${parsed.description}"
            Completeness: ${(parsed.completeness * 100).toInt()}%
            Usable: ${parsed.isUsable}
        """.trimIndent())
    }

    /**
     * Clear parsed expense data
     */
    fun clearParsedData() {
        _parsedExpenseData.value = null
    }
}