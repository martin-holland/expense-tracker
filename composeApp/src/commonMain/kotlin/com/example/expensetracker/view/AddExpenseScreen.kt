// ===============================================
// ADD EXPENSE SCREEN — FIXED FOR DARK MODE
// (NO LOGIC CHANGED)
// ===============================================

package com.example.expensetracker.view

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.ExpenseCategory
import com.example.expensetracker.service.getMicrophoneService
import com.example.expensetracker.view.components.SnackbarHost
import com.example.expensetracker.view.components.camera.CameraScreen
import com.example.expensetracker.viewmodel.AddExpenseViewModel
import com.example.expensetracker.viewmodel.VoiceInputViewModel
import com.example.theme.com.example.expensetracker.LocalAppColors
import kotlin.time.ExperimentalTime
import kotlinx.datetime.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalTime::class)
@Composable
fun AddExpenseScreen(viewModel: AddExpenseViewModel = viewModel()) {
    val voiceViewModel: VoiceInputViewModel = viewModel<VoiceInputViewModel>()
    val settingsViewModel: com.example.expensetracker.viewmodel.SettingsViewModel = viewModel()

    val appColors = LocalAppColors.current
    val accentGreen = appColors.chart2
    val sectionShape = RoundedCornerShape(12.dp)

    val currency = viewModel.currency
    val amount = viewModel.amount
    val category = viewModel.category
    val note = viewModel.note
    val date = viewModel.date
    val showVoiceSection by voiceViewModel.showVoiceSection.collectAsState()
    val isSaving = viewModel.isSaving
    val snackbarMessage = viewModel.snackbarMessage

    var showDialog by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Check microphone permission when screen loads
    LaunchedEffect(Unit) { settingsViewModel.checkMicrophonePermission() }

    // Re-check permission when screen resumes (e.g., after granting permission in Android settings)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                settingsViewModel.checkMicrophonePermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize().background(appColors.background)) {
        Column(
                modifier =
                        Modifier.verticalScroll(rememberScrollState()).fillMaxSize().padding(20.dp)
        ) {
            // === Header ===
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                            modifier =
                                    Modifier.size(42.dp)
                                            .clip(CircleShape)
                                            .background(accentGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                    ) {
                        Icon(
                                Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = accentGreen
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                                "Add Expense",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = appColors.foreground
                        )
                        Text("Expense Tracker", fontSize = 14.sp, color = appColors.mutedForeground)
                    }
                }

                Text(
                        text = "Cancel",
                        color = appColors.foreground,
                        fontWeight = FontWeight.Medium,
                        modifier =
                                Modifier.clickable {
                                    // Reset form state
                                    viewModel.resetForm()
                                }
                )
            }

            Spacer(Modifier.height(24.dp))

            // === Currency Section ===
            SectionCard(title = "Currency") {
                // Display all currencies in a scrollable grid
                val allCurrencies = Currency.values().toList()
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    allCurrencies.chunked(3).forEach { rowCurrencies ->
                        Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                        ) {
                            rowCurrencies.forEach { curr ->
                                Button(
                                        onClick = { viewModel.onCurrencySelected(curr) },
                                        colors =
                                                ButtonDefaults.buttonColors(
                                                        containerColor =
                                                                if (currency == curr)
                                                                        appColors.primary
                                                                else appColors.card,
                                                        contentColor =
                                                                if (currency == curr)
                                                                        appColors.primaryForeground
                                                                else appColors.foreground
                                                ),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, appColors.border),
                                        modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                            "${curr.symbol} ${curr.code}",
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp
                                    )
                                }
                            }
                            // Fill remaining space if row has less than 3 items
                            repeat(3 - rowCurrencies.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // === Amount Section ===
            SectionCard(title = "Amount") {
                OutlinedTextField(
                        value = amount,
                        onValueChange = { viewModel.onAmountChanged(it) },
                        placeholder = {
                            Text("${currency.symbol} 0.00", color = appColors.mutedForeground)
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                                OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = appColors.inputBackground,
                                        unfocusedContainerColor = appColors.inputBackground,
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                        cursorColor = appColors.primary
                                )
                )
            }
            // === Category Section ===
            SectionCard(title = "Category") {
                val categories =
                        listOf(
                                Triple(
                                        ExpenseCategory.FOOD,
                                        Icons.Default.Fastfood,
                                        Color(0xFFFFEAEA)
                                ),
                                Triple(
                                        ExpenseCategory.TRAVEL,
                                        Icons.Default.DirectionsCar,
                                        Color(0xFFE5F8FA)
                                ),
                                Triple(
                                        ExpenseCategory.UTILITIES,
                                        Icons.Default.ElectricBolt,
                                        Color(0xFFEAF9EE)
                                ),
                                Triple(
                                        ExpenseCategory.OTHER,
                                        Icons.Default.MoreHoriz,
                                        Color(0xFFFFF8E8)
                                )
                        )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.chunked(2).forEach { row ->
                        Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { (cat, icon, bgColor) ->
                                Card(
                                        modifier =
                                                Modifier.weight(1f).clickable {
                                                    viewModel.onCategorySelected(cat)
                                                },
                                        shape = RoundedCornerShape(12.dp),
                                        border =
                                                BorderStroke(
                                                        1.dp,
                                                        if (category == cat) accentGreen
                                                        else appColors.border
                                                ),
                                        colors =
                                                CardDefaults.cardColors(
                                                        containerColor = appColors.card
                                                )
                                ) {
                                    Column(
                                            modifier = Modifier.padding(vertical = 14.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                                modifier =
                                                        Modifier.size(46.dp)
                                                                .clip(CircleShape)
                                                                .background(bgColor),
                                                contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                    icon,
                                                    contentDescription = cat.displayName,
                                                    tint = accentGreen,
                                                    modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Spacer(Modifier.height(6.dp))
                                        Text(
                                                cat.displayName,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = appColors.foreground
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // === Date Section ===
            SectionCard(title = "Date") {
                Row(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .border(1.dp, appColors.border, sectionShape)
                                        .padding(12.dp)
                                        .clickable { showDialog = true },
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = accentGreen)
                    Spacer(Modifier.width(8.dp))
                    Text(date, color = appColors.foreground, fontSize = 15.sp)
                }

                if (showDialog) {
                    DatePickerDialog(
                            onDismissRequest = { showDialog = false },
                            confirmButton = {
                                TextButton(
                                        onClick = {
                                            datePickerState.selectedDateMillis?.let { millis ->
                                                val instant = Instant.fromEpochMilliseconds(millis)
                                                val localDate =
                                                        instant.toLocalDateTime(
                                                                        TimeZone.currentSystemDefault()
                                                                )
                                                                .date
                                                val monthName =
                                                        localDate.month.name.lowercase()
                                                                .replaceFirstChar { it.uppercase() }
                                                val formatted =
                                                        "$monthName ${localDate.dayOfMonth}, ${localDate.year}"
                                                viewModel.onDateSelected(formatted)
                                            }
                                            showDialog = false
                                        }
                                ) { Text("OK", color = accentGreen) }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDialog = false }) {
                                    Text("Cancel", color = accentGreen)
                                }
                            }
                    ) { DatePicker(state = datePickerState) }
                }
            }

            Spacer(Modifier.height(30.dp))

            // === Note Section ===
            SectionCard(title = "Note (Optional)") {
                OutlinedTextField(
                        value = note,
                        onValueChange = { viewModel.onNoteChanged(it) },
                        placeholder = {
                            Text(
                                    "Add a note about this expense...",
                                    color = appColors.mutedForeground
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        colors =
                                OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = appColors.inputBackground,
                                        unfocusedContainerColor = appColors.inputBackground,
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                        cursorColor = appColors.primary
                                )
                )
            }

            // Camera Section
            if (showCamera) {
                CameraScreen()
            }

            // === Quick Input Section ===
            Text("Quick Input", color = appColors.foreground, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            DashedCard(accentGreen) {
                Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                ) {
                    QuickInputItem(
                            label = "Voice Input",
                            subtext = if (showVoiceSection) "Tap to close" else "Tap to speak",
                            icon = Icons.Default.Mic,
                            accent = accentGreen,
                            action = { voiceViewModel.toggleVoiceSection() }
                    )
                    if (!showCamera) {
                        QuickInputItem(
                                label = "Receipt",
                                subtext = "Tap to capture",
                                icon = Icons.Default.CameraAlt,
                                accent = accentGreen,
                                action = { showCamera = true }
                        )
                    } else {
                        QuickInputItem(
                                label = "Receipt",
                                subtext = "Tap to close",
                                icon = Icons.Default.CameraAlt,
                                accent = accentGreen,
                                action = { showCamera = false }
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            VoiceInputSection(
                    voiceViewModel = voiceViewModel,
                    settingsViewModel = settingsViewModel
            )

            Spacer(Modifier.height(40.dp))
            Button(
                    onClick = { viewModel.saveExpense() },
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = accentGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(55.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                            "Saving...",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                            "Save Expense",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Snackbar at the top - placed last so it appears above scrollable content
        SnackbarHost(
                message = snackbarMessage,
                onDismiss = { viewModel.dismissSnackbar() },
                modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

// ========================================================================
// REUSABLE SECTION CARD
// ========================================================================
@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    val appColors = LocalAppColors.current

    Card(
            colors = CardDefaults.cardColors(containerColor = appColors.card),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, appColors.border),
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = appColors.foreground)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

// ========================================================================
// VOICE INPUT SECTION (THEME FIX ONLY)
// ========================================================================
@Composable
private fun VoiceInputSection(
        voiceViewModel: VoiceInputViewModel = viewModel(),
        settingsViewModel: com.example.expensetracker.viewmodel.SettingsViewModel = viewModel()
) {
    val isRecording by voiceViewModel.isRecording.collectAsState()
    val audioData by voiceViewModel.audioData.collectAsState()
    val isProcessing by voiceViewModel.isProcessing.collectAsState()
    val errorMessage by voiceViewModel.errorMessage.collectAsState()
    val showVoiceSection by voiceViewModel.showVoiceSection.collectAsState()
    val isVoiceInputEnabled by settingsViewModel.isVoiceInputEnabled.collectAsState()
    val hasMicrophonePermission by settingsViewModel.hasMicrophonePermission.collectAsState()

    val appColors = LocalAppColors.current
    val accentGreen = appColors.chart2

    if (showVoiceSection) {
        SectionCard(title = "Voice Input") {
            // Show enable voice input message if disabled
            if (!isVoiceInputEnabled) {
                Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E6)),
                        border = BorderStroke(1.dp, Color(0xFFFFA726))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                                "⚠️ Voice input is disabled",
                                color = Color(0xFFE65100),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                                "Enable voice input in Settings to use this feature",
                                color = Color(0xFFE65100),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Button(
                                onClick = { settingsViewModel.toggleVoiceInput(true) },
                                colors =
                                        ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFFFA726)
                                        ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                        ) { Text("Enable Voice Input", color = Color.White, fontSize = 14.sp) }
                    }
                }
            }

            // Show permission message if no permission
            if (isVoiceInputEnabled && !hasMicrophonePermission) {
                Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        border = BorderStroke(1.dp, Color(0xFFEF5350))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                                "🎤 Microphone permission required",
                                color = Color(0xFFC62828),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                                "Grant microphone permission to record audio",
                                color = Color(0xFFC62828),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Button(
                                onClick = { getMicrophoneService().requestMicrophonePermission() },
                                colors =
                                        ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFFEF5350)
                                        ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                        ) { Text("Grant Permission", color = Color.White, fontSize = 14.sp) }
                    }
                }
            }

            errorMessage?.let { error ->
                Text(
                        error,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Card(
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    colors = CardDefaults.cardColors(containerColor = appColors.inputBackground),
                    border = BorderStroke(1.dp, appColors.border)
            ) {
                Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                            text =
                                    when {
                                        isRecording -> "🎤 Recording..."
                                        audioData != null ->
                                                "✅ Audio recorded (${audioData!!.size} bytes)"
                                        else -> "Tap record to start voice input"
                                    },
                            style = MaterialTheme.typography.bodyLarge,
                            color = accentGreen,
                            fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                    onClick = {
                        if (isRecording) {
                            voiceViewModel.stopRecording()
                        } else {
                            voiceViewModel.startRecording()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                            ButtonDefaults.buttonColors(
                                    containerColor =
                                            if (isRecording) Color(0xFFFF6B6B) else accentGreen
                            ),
                    shape = RoundedCornerShape(10.dp),
                    enabled = !isProcessing && isVoiceInputEnabled && hasMicrophonePermission
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                            if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color.White
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                        if (isRecording) "Stop Recording" else "Start Recording",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                        onClick = {
                            // Non-functional for now
                        },
                        enabled = false, // Disabled since not implemented
                        modifier = Modifier.weight(1f),
                        colors =
                                ButtonDefaults.buttonColors(
                                        containerColor =
                                                appColors.mutedForeground.copy(alpha = 0.5f)
                                ),
                        shape = RoundedCornerShape(10.dp)
                ) {
                    Text("📝")
                    Spacer(Modifier.width(8.dp))
                    Text("Transcribe", color = Color.White)
                }

                Button(
                        onClick = { voiceViewModel.playAudio() },
                        enabled = audioData != null && !isRecording && !isProcessing,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = appColors.chart1),
                        shape = RoundedCornerShape(10.dp)
                ) {
                    Text("▶️")
                    Spacer(Modifier.width(8.dp))
                    Text("Play", color = Color.White)
                }
            }

            // Permission Status
            Spacer(Modifier.height(8.dp))
            Text(
                    text =
                            "Microphone Permission: ${if (getMicrophoneService().hasMicrophonePermission()) "Granted" else "Not Granted"}",
                    fontSize = 12.sp,
                    color =
                            if (getMicrophoneService().hasMicrophonePermission()) accentGreen
                            else Color(0xFFFF6B6B)
            )

            // NEW: Speech Recognition Section
            Spacer(Modifier.height(24.dp))
            SpeechRecognitionSection(voiceViewModel = voiceViewModel, accentGreen = accentGreen)
        }
    }
}

@Composable
private fun SpeechRecognitionSection(voiceViewModel: VoiceInputViewModel, accentGreen: Color) {
    val speechState by voiceViewModel.speechRecognitionState.collectAsState()
    val partialTranscription by voiceViewModel.partialTranscription.collectAsState()
    val appColors = LocalAppColors.current

    Column {
        Text(
                "Live Transcription (POC)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = appColors.foreground
        )

        Spacer(Modifier.height(12.dp))

        // Transcription display
        Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors =
                        CardDefaults.cardColors(containerColor = appColors.muted.copy(alpha = 0.2f))
        ) {
            Box(modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp).padding(16.dp)) {
                when (val state = speechState) {
                    is VoiceInputViewModel.SpeechRecognitionState.Idle -> {
                        Text(
                                "Tap 'Start Live Transcription' to begin",
                                color = appColors.mutedForeground,
                                style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    is VoiceInputViewModel.SpeechRecognitionState.Listening -> {
                        Column {
                            Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = accentGreen
                                )
                                Text(
                                        "Listening...",
                                        color = accentGreen,
                                        fontWeight = FontWeight.Medium
                                )
                            }

                            if (partialTranscription.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                        partialTranscription,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                    is VoiceInputViewModel.SpeechRecognitionState.Success -> {
                        Column {
                            Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                        "✓ Transcribed",
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.Medium
                                )
                                Text(
                                        "Confidence: ${(state.confidence * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = appColors.mutedForeground
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                    state.transcription,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                            )

                            // Show alternatives if available
                            if (state.alternatives.size > 1) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                        "Alternatives:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = appColors.mutedForeground
                                )
                                state.alternatives.drop(1).take(2).forEach { alt ->
                                    Text(
                                            "• $alt",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = appColors.mutedForeground,
                                            fontStyle =
                                                    androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }
                        }
                    }
                    is VoiceInputViewModel.SpeechRecognitionState.Error -> {
                        Column {
                            Text(
                                    "❌ Error",
                                    color = Color(0xFFEF4444),
                                    fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                    state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Control buttons - Android-specific implementation will be handled in MainActivity
        SpeechRecognitionButton(
                voiceViewModel = voiceViewModel,
                speechState = speechState,
                accentGreen = accentGreen
        )

        // Parse button and results
        val parsedData by voiceViewModel.parsedExpenseData.collectAsState()

        // Show parse button when we have a successful transcription
        if (speechState is VoiceInputViewModel.SpeechRecognitionState.Success) {
            Spacer(Modifier.height(12.dp))
            Button(
                    onClick = {
                        val state =
                                speechState as VoiceInputViewModel.SpeechRecognitionState.Success
                        voiceViewModel.parseTranscription(state.transcription)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.primary),
                    shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Extract Expense Data", color = Color.White)
            }
        }

        // Show parsed results
        parsedData?.let { data ->
            Spacer(Modifier.height(12.dp))
            ParsedDataCard(data = data, appColors = appColors)
        }
    }
}

@Composable
expect fun SpeechRecognitionButton(
        voiceViewModel: VoiceInputViewModel,
        speechState: VoiceInputViewModel.SpeechRecognitionState,
        accentGreen: Color
)

@Composable
private fun ParsedDataCard(
        data: com.example.expensetracker.service.ParsedExpenseData,
        appColors: com.example.theme.com.example.expensetracker.AppColorScheme
) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors =
                    CardDefaults.cardColors(
                            containerColor =
                                    when {
                                        data.completeness >= 0.8f ->
                                                Color(0xFF10B981).copy(alpha = 0.1f)
                                        data.completeness >= 0.5f ->
                                                Color(0xFFF59E0B).copy(alpha = 0.1f)
                                        else -> Color(0xFFEF4444).copy(alpha = 0.1f)
                                    }
                    )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                        "Extracted Data",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                )
                Text(
                        "Completeness: ${(data.completeness * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = appColors.mutedForeground
                )
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            // Show each field
            ParsedField("Amount", data.amount?.toString() ?: "Not found", appColors)
            ParsedField("Currency", data.currency?.code ?: "Not found", appColors)
            ParsedField("Category", data.category?.displayName ?: "Not found", appColors)
            ParsedField("Description", data.description.ifBlank { "Not found" }, appColors)

            // If usable, show status
            if (data.isUsable) {
                Spacer(Modifier.height(12.dp))
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                            "✓ Ready to use",
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun ParsedField(
        label: String,
        value: String,
        appColors: com.example.theme.com.example.expensetracker.AppColorScheme
) {
    Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = appColors.mutedForeground)
        Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (value != "Not found") FontWeight.Medium else FontWeight.Normal,
                color =
                        if (value != "Not found") appColors.foreground
                        else appColors.mutedForeground
        )
    }
}

// ========================================================================
// QUICK INPUT ITEM
// ========================================================================
@Composable
private fun QuickInputItem(
        label: String,
        subtext: String,
        icon: ImageVector,
        accent: Color,
        action: () -> Unit
) {
    val appColors = LocalAppColors.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
                modifier =
                        Modifier.size(60.dp).clip(CircleShape).background(accent).clickable {
                            action()
                        },
                contentAlignment = Alignment.Center
        ) {
            Icon(
                    icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = appColors.foreground, fontSize = 14.sp)
        Text(subtext, color = appColors.mutedForeground, fontSize = 12.sp)
    }
}

// ========================================================================
// DASHED CARD
// ========================================================================
@Composable
private fun DashedCard(accent: Color, content: @Composable BoxScope.() -> Unit) {
    Box(
            modifier =
                    Modifier.fillMaxWidth()
                            .padding(bottom = 20.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(accent.copy(alpha = 0.05f))
                            .border(
                                    BorderStroke(
                                            1.dp,
                                            Brush.horizontalGradient(listOf(accent, accent))
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRoundRect(
                    color = accent.copy(alpha = 0.4f),
                    style =
                            Stroke(
                                    width = 2f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                            ),
                    cornerRadius = CornerRadius(12.dp.toPx())
            )
        }
        content()
    }
}
