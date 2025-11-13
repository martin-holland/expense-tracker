package com.example.expensetracker.service

import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import com.example.expensetracker.MainActivity
import com.example.expensetracker.service.MicrophoneService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class AndroidMicrophoneService(private val context: Context) : MicrophoneService {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFile: File? = null
    @Volatile private var isRecording = false
    @Volatile private var isPlaying = false

    override suspend fun startRecording(): Boolean =
        withContext(Dispatchers.IO) {
            try {
                if (isRecording) {
                    println("⚠️ Already recording!")
                    return@withContext false
                }

                audioFile =
                    File(context.cacheDir, "recording_${System.currentTimeMillis()}.3gp")
                println("🎤 Starting recording to: ${audioFile!!.absolutePath}")

                mediaRecorder =
                    MediaRecorder().apply {
                        setAudioSource(MediaRecorder.AudioSource.MIC)
                        setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                        setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                        setOutputFile(audioFile!!.absolutePath)
                        prepare()
                        start()
                    }

                kotlinx.coroutines.delay(100)
                isRecording = true
                println("✅ Recording started successfully (isRecording = $isRecording)")
                true
            } catch (e: Exception) {
                println("❌ Error starting recording: ${e.message}")
                e.printStackTrace()
                false
            }
        }

    override suspend fun stopRecording(): ByteArray? =
        withContext(Dispatchers.IO) {
            try {
                println("🔍 stopRecording() called. isRecording = $isRecording")
                if (!isRecording) {
                    println("⚠️ Not recording, cannot stop (isRecording = false)")
                    println("   mediaRecorder = $mediaRecorder")
                    println("   audioFile = $audioFile")
                    return@withContext null
                }

                println("⏹️ Stopping recording (isRecording = true)...")
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
                isRecording = false

                // Read the recorded file
                audioFile?.let { file ->
                    if (file.exists()) {
                        val fileSize = file.length()
                        println("📁 Audio file exists: ${file.absolutePath}")
                        println("📊 File size: $fileSize bytes")

                        val inputStream = FileInputStream(file)
                        val outputStream = ByteArrayOutputStream()
                        val buffer = ByteArray(1024)
                        var length: Int
                        while (inputStream.read(buffer).also { length = it } != -1) {
                            outputStream.write(buffer, 0, length)
                        }
                        inputStream.close()
                        outputStream.close()
                        val audioBytes = outputStream.toByteArray()
                        println("✅ Audio data loaded: ${audioBytes.size} bytes")
                        audioBytes
                    } else {
                        println("❌ Audio file does not exist!")
                        null
                    }
                }
            } catch (e: Exception) {
                println("❌ Error stopping recording: ${e.message}")
                e.printStackTrace()
                isRecording = false
                null
            }
        }


    override suspend fun playAudio(audioData: ByteArray): Boolean =
        withContext(Dispatchers.IO) {
            try {
                println("▶️ Playing audio: ${audioData.size} bytes")

                mediaPlayer?.release()
                mediaPlayer = null

                val tempFile =
                    File(context.cacheDir, "playback_${System.currentTimeMillis()}.3gp")
                FileOutputStream(tempFile).use { output -> output.write(audioData) }
                println(
                    "📁 Playback file created: ${tempFile.absolutePath} (${tempFile.length()} bytes)"
                )

                isPlaying = true

                // Create and configure MediaPlayer
                mediaPlayer =
                    MediaPlayer().apply {
                        setDataSource(tempFile.absolutePath)
                        prepare()
                        setOnCompletionListener {
                            println("✅ Playback completed")
                            this@AndroidMicrophoneService.isPlaying = false
                            tempFile.delete()
                        }
                        setOnErrorListener { _, what, extra ->
                            println("❌ Playback error: what=$what, extra=$extra")
                            this@AndroidMicrophoneService.isPlaying = false
                            tempFile.delete()
                            true
                        }
                        start()
                        println("🔊 Playback started (duration: ${duration}ms)")
                    }

                true
            } catch (e: Exception) {
                println("❌ Error playing audio: ${e.message}")
                e.printStackTrace()
                isPlaying = false
                false
            }
        }

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

    override fun isRecording(): Boolean = isRecording

    override fun isPlaying(): Boolean = isPlaying


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

