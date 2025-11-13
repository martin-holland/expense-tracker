package com.example.expensetracker.viewmodel

import androidx.compose.runtime.mutableStateOf

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.service.getMicrophoneService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VoiceInputViewModel : ViewModel() {
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



}