package com.example.expensetracker.service

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.expensetracker.MainActivity
import com.example.expensetracker.service.MicrophoneService

class AndroidMicrophoneService(private val context: Context) : MicrophoneService {

    override fun hasMicrophonePermission(): Boolean {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        println("🎤 Microphone permission check: $hasPermission")
        return hasPermission
    }

    override fun requestMicrophonePermission() {
        (context as? MainActivity)?.requestMicrophonePermission()
    }

    companion object {
        @Volatile private var instance: AndroidMicrophoneService? = null

        fun getInstance(context: Context): AndroidMicrophoneService {
            return instance
                ?: synchronized(this) {
                    instance ?: AndroidMicrophoneService(context).also { instance = it }
                }
        }
    }
}

actual fun getMicrophoneService(): MicrophoneService {
    return AndroidMicrophoneService.getInstance(
        com.example.expensetracker.MainActivity.appContext
    )
}

