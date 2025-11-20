package com.example.expensetracker.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensetracker.viewmodel.AndroidVoiceInputHelper
import com.example.expensetracker.viewmodel.VoiceInputViewModel

// Shared helper instance to persist across button recompositions
private val helperMap = mutableMapOf<VoiceInputViewModel, AndroidVoiceInputHelper>()

/**
 * Manages the helper lifecycle at the section level (not button level)
 * This prevents disposal when button moves position in composition tree
 */
@Composable
actual fun SpeechRecognitionHelperManager(voiceViewModel: VoiceInputViewModel) {
    val context = LocalContext.current
    val readyForCleanup by voiceViewModel.recognizerReadyForCleanup.collectAsState()
    
    // Create helper at section level
    DisposableEffect(voiceViewModel) {
        val helper = helperMap.getOrPut(voiceViewModel) {
            AndroidVoiceInputHelper(context, voiceViewModel)
        }
        
        onDispose {
            helper.cleanup()
            helperMap.remove(voiceViewModel)
        }
    }
    
    // Clean up service after success/error to prevent ERROR_RECOGNIZER_BUSY
    LaunchedEffect(readyForCleanup) {
        if (readyForCleanup) {
            val helper = helperMap[voiceViewModel]
            helper?.cleanup()
            voiceViewModel.acknowledgeCleanup()
        }
    }
}

@Composable
actual fun SpeechRecognitionButton(
    voiceViewModel: VoiceInputViewModel,
    speechState: VoiceInputViewModel.SpeechRecognitionState,
    accentGreen: Color
) {
    val context = LocalContext.current
    
    // Get existing helper (created at section level)
    val helper = remember(voiceViewModel) {
        helperMap.getOrPut(voiceViewModel) {
            AndroidVoiceInputHelper(context, voiceViewModel)
        }
    }

    Button(
        onClick = {
            when (speechState) {
                is VoiceInputViewModel.SpeechRecognitionState.Idle,
                is VoiceInputViewModel.SpeechRecognitionState.Processing,
                is VoiceInputViewModel.SpeechRecognitionState.Success,
                is VoiceInputViewModel.SpeechRecognitionState.Error -> {
                    helper.startSpeechRecognition()
                }
                is VoiceInputViewModel.SpeechRecognitionState.Listening -> {
                    helper.stopSpeechRecognition()
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = speechState !is VoiceInputViewModel.SpeechRecognitionState.Processing,
        colors = ButtonDefaults.buttonColors(
            containerColor = when (speechState) {
                is VoiceInputViewModel.SpeechRecognitionState.Listening ->
                    Color(0xFFFF6B6B)
                else -> accentGreen
            }
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(
            when (speechState) {
                is VoiceInputViewModel.SpeechRecognitionState.Listening ->
                    Icons.Default.Stop
                else -> Icons.Default.Mic
            },
            contentDescription = null,
            tint = Color.White
        )
        Spacer(Modifier.width(8.dp))
        Text(
            when (speechState) {
                is VoiceInputViewModel.SpeechRecognitionState.Listening ->
                    "Stop Transcription"
                is VoiceInputViewModel.SpeechRecognitionState.Processing ->
                    "Processing..."
                else -> "Start Voice Input"
            },
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

