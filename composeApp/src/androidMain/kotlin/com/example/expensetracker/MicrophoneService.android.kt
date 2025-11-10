package com.example.expensetracker

import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import com.example.expensetracker.Service.MicrophoneService
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidMicrophoneService(private val context: Context) : MicrophoneService {

    override fun hasMicrophonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun requestMicrophonePermission(): Boolean {
        // In a real app, you'd use ActivityResultContracts.RequestPermission
        return true
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
        com.example.testkotlinmultiplatform.MainActivity.appContext
    )
}

