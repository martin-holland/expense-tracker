package com.example.expensetracker.service

interface MicrophoneService {

    fun hasMicrophonePermission(): Boolean
    fun requestMicrophonePermission()

}

expect fun getMicrophoneService(): MicrophoneService
