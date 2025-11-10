package com.example.expensetracker.Service

interface MicrophoneService {

    fun hasMicrophonePermission(): Boolean
    suspend fun requestMicrophonePermission(): Boolean

}

expect fun getMicrophoneService(): MicrophoneService
