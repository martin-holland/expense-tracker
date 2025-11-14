package com.example.expensetracker.service

interface MicrophoneService {

    suspend fun startRecording(): Boolean
    suspend fun stopRecording(): ByteArray?
    suspend fun playAudio(audioData: ByteArray): Boolean

    fun hasMicrophonePermission(): Boolean
    fun requestMicrophonePermission()

    fun isRecording(): Boolean
    fun isPlaying(): Boolean

}

expect fun getMicrophoneService(): MicrophoneService
