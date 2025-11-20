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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
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
import kotlinx.coroutines.launch
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
    
    // Scroll state for auto-scrolling to voice section
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

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
                            action = { 
                                voiceViewModel.toggleVoiceSection()
                                // Scroll to voice section when opened
                                if (!showVoiceSection) {
                                    coroutineScope.launch {
                                        kotlinx.coroutines.delay(100) // Small delay for UI to render
                                        scrollState.animateScrollTo(scrollState.maxValue)
                                    }
                                }
                            }
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
                    settingsViewModel = settingsViewModel,
                    scrollState = scrollState,
                    coroutineScope = coroutineScope
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
        settingsViewModel: com.example.expensetracker.viewmodel.SettingsViewModel = viewModel(),
        scrollState: ScrollState? = null,
        coroutineScope: kotlinx.coroutines.CoroutineScope? = null
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
        // Auto-scroll to voice section when it opens
        LaunchedEffect(showVoiceSection) {
            if (showVoiceSection) {
                kotlinx.coroutines.delay(200) // Wait for UI to render
                scrollState?.animateScrollTo(scrollState.maxValue)
            }
        }
        
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

            // Speech Recognition Section (Live Transcription)
            SpeechRecognitionSection(
                voiceViewModel = voiceViewModel, 
                accentGreen = accentGreen,
                hasMicrophonePermission = hasMicrophonePermission
            )
        }
    }
}

@Composable
private fun SpeechRecognitionSection(
    voiceViewModel: VoiceInputViewModel, 
    accentGreen: Color,
    hasMicrophonePermission: Boolean,
    addExpenseViewModel: AddExpenseViewModel = viewModel()
) {
    val speechState by voiceViewModel.speechRecognitionState.collectAsState()
    val partialTranscription by voiceViewModel.partialTranscription.collectAsState()
    val showManualEntry by voiceViewModel.showManualEntry.collectAsState()
    val manualEntryText by voiceViewModel.manualEntryText.collectAsState()
    val appColors = LocalAppColors.current

    // Initialize helper at section level so it doesn't get disposed when button moves
    SpeechRecognitionHelperManager(voiceViewModel)

    Column {
        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                    "Voice Input",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = appColors.foreground
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Show cancel button when there's active content (transcription, parsed data, or manual entry)
                val hasActiveContent = speechState !is VoiceInputViewModel.SpeechRecognitionState.Idle || showManualEntry
                if (hasActiveContent) {
                    TextButton(
                            onClick = { voiceViewModel.cancelVoiceInput() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                                Icons.Default.Close,
                                contentDescription = "Cancel",
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFFEF4444)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                                "Cancel",
                                fontSize = 12.sp,
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // "Type instead" option - always visible for quick access
                TextButton(
                        onClick = { voiceViewModel.enableManualEntry() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                            Icons.Default.Edit,
                            contentDescription = "Type instead",
                            modifier = Modifier.size(14.dp),
                            tint = appColors.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                            "Type instead",
                            fontSize = 12.sp,
                            color = appColors.primary,
                            fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        // Show permission status only when NOT granted
        if (!hasMicrophonePermission) {
            Text(
                    text = "🎤 Microphone permission not granted",
                    fontSize = 11.sp,
                    color = Color(0xFFFF6B6B),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        // Hide voice transcription when manual entry is active
        if (!showManualEntry) {
            // Show button ABOVE card in Idle or Processing state for better visual hierarchy
            if (speechState is VoiceInputViewModel.SpeechRecognitionState.Idle || 
                speechState is VoiceInputViewModel.SpeechRecognitionState.Processing) {
                SpeechRecognitionButton(
                        voiceViewModel = voiceViewModel,
                        speechState = speechState,
                        accentGreen = accentGreen
                )
                Spacer(Modifier.height(12.dp))
            }

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
                    is VoiceInputViewModel.SpeechRecognitionState.Processing -> {
                        Column {
                            Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = appColors.primary
                                )
                                Text(
                                        "Processing...",
                                        color = appColors.primary,
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
                                        if (partialTranscription.isBlank()) "Listening..." else "Processing...",
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
                            Text(
                                    "✓ Transcribed",
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                    state.transcription,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                            )
                            // Subtle confidence score at bottom
                            Spacer(Modifier.height(6.dp))
                            Text(
                                    "${(state.confidence * 100).toInt()}% confidence",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = appColors.mutedForeground.copy(alpha = 0.7f),
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                    is VoiceInputViewModel.SpeechRecognitionState.Error -> {
                        Column {
                            Text(
                                    "❌ ${state.message}",
                                    color = Color(0xFFEF4444),
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(12.dp))
                            // Show both retry and manual entry options
                            Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                        onClick = { 
                                            voiceViewModel.resetSpeechRecognition()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, accentGreen)
                                ) {
                                    Icon(
                                            Icons.Default.Mic, 
                                            contentDescription = null, 
                                            tint = accentGreen,
                                            modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Try Again", color = accentGreen)
                                }
                                Button(
                                        onClick = { voiceViewModel.enableManualEntry() },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = appColors.primary
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(
                                            Icons.Default.Edit, 
                                            contentDescription = null, 
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Type Instead", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
            }

            Spacer(Modifier.height(12.dp))

            // Show button BELOW card only when actively Listening (not Idle, Processing, Success, or Error)
            if (speechState is VoiceInputViewModel.SpeechRecognitionState.Listening) {
                SpeechRecognitionButton(
                        voiceViewModel = voiceViewModel,
                        speechState = speechState,
                        accentGreen = accentGreen
                )
            }
        }

        // Manual text entry section (fallback)
        if (showManualEntry) {
            ManualTextEntryCard(
                text = manualEntryText,
                onTextChange = { voiceViewModel.onManualEntryTextChanged(it) },
                onParse = { voiceViewModel.parseManualEntry() },
                onCancel = { voiceViewModel.cancelManualEntry() },
                appColors = appColors
            )
            Spacer(Modifier.height(12.dp))
        }

        // Show parsed results (auto-parsed after successful transcription)
        val parsedData by voiceViewModel.parsedExpenseData.collectAsState()
        parsedData?.let { data ->
            Spacer(Modifier.height(12.dp))
            ParsedDataCard(
                data = data,
                voiceViewModel = voiceViewModel,
                appColors = appColors,
                onSaveExpense = { finalData ->
                    // Populate form with final (possibly edited) data
                    addExpenseViewModel.populateFromParsedData(finalData)
                    // Save the expense directly
                    addExpenseViewModel.saveExpense()
                    // Clear voice input state
                    voiceViewModel.clearParsedData()
                    voiceViewModel.resetSpeechRecognition()
                }
            )
        }
    }
}

@Composable
expect fun SpeechRecognitionHelperManager(voiceViewModel: VoiceInputViewModel)

@Composable
expect fun SpeechRecognitionButton(
        voiceViewModel: VoiceInputViewModel,
        speechState: VoiceInputViewModel.SpeechRecognitionState,
        accentGreen: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParsedDataCard(
        data: com.example.expensetracker.service.ParsedExpenseData,
        voiceViewModel: VoiceInputViewModel,
        appColors: com.example.theme.com.example.expensetracker.AppColorScheme,
        onSaveExpense: (com.example.expensetracker.service.ParsedExpenseData) -> Unit
) {
    // Editable state for each field
    var editedAmount by remember(data) { mutableStateOf(data.amount?.toString() ?: "") }
    var editedDescription by remember(data) { mutableStateOf(data.description) }
    var editedCurrency by remember(data) { mutableStateOf(data.currency) }
    // Default category to OTHER if not detected
    var editedCategory by remember(data) { mutableStateOf(data.category ?: ExpenseCategory.OTHER) }
    
    // Dropdown expanded state
    var currencyExpanded by remember { mutableStateOf(false) }
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

            // Editable fields
            val missingFields = mutableListOf<String>()
            
            // Amount field (editable)
            Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Amount", style = MaterialTheme.typography.bodyMedium, color = appColors.mutedForeground)
                OutlinedTextField(
                        value = editedAmount,
                        onValueChange = { editedAmount = it },
                        modifier = Modifier.width(120.dp),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        singleLine = true,
                        placeholder = { Text("50.0", style = MaterialTheme.typography.bodySmall) }
                )
            }
            if (data.amount == null) missingFields.add("Amount")
            
            // Currency selector (editable if missing)
            if (editedCurrency != null) {
                ParsedField("Currency", editedCurrency!!.code, appColors)
            } else {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("Currency", style = MaterialTheme.typography.bodyMedium, color = appColors.mutedForeground)
                    Spacer(Modifier.height(4.dp))
                    
                    // Currency Dropdown
                    ExposedDropdownMenuBox(
                        expanded = currencyExpanded,
                        onExpandedChange = { currencyExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = if (editedCurrency != null) 
                                "${editedCurrency!!.symbol} ${editedCurrency!!.displayName} (${editedCurrency!!.code})"
                            else "Select Currency",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = appColors.foreground,
                                unfocusedTextColor = appColors.foreground,
                                focusedBorderColor = appColors.border,
                                unfocusedBorderColor = appColors.border
                            ),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        
                        ExposedDropdownMenu(
                            expanded = currencyExpanded,
                            onDismissRequest = { currencyExpanded = false },
                            modifier = Modifier.heightIn(max = 250.dp)
                        ) {
                            Currency.entries.forEach { currency ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = currency.symbol,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Column {
                                                Text(
                                                    text = currency.code,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                Text(
                                                    text = currency.displayName,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = appColors.mutedForeground
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        editedCurrency = currency
                                        currencyExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                missingFields.add("Currency")
            }
            
            // Category selector (always show as editable since we default to OTHER)
            if (data.category != null) {
                // Show as read-only if it was successfully detected
                ParsedField("Category", editedCategory!!.displayName, appColors)
            } else {
                // Show selector with OTHER as default
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text("Category", style = MaterialTheme.typography.bodyMedium, color = appColors.mutedForeground)
                    Spacer(Modifier.height(4.dp))
                    
                    // Compact category selector - 2 per row
                    val categories = listOf(
                        ExpenseCategory.FOOD to Icons.Default.Fastfood,
                        ExpenseCategory.TRAVEL to Icons.Default.DirectionsCar,
                        ExpenseCategory.UTILITIES to Icons.Default.ElectricBolt,
                        ExpenseCategory.OTHER to Icons.Default.MoreHoriz
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        categories.chunked(2).forEach { rowCategories ->
                            Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                            ) {
                                rowCategories.forEach { (cat, icon) ->
                                    Box(
                                            modifier =
                                                    Modifier.weight(1f)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .border(
                                                                    1.dp,
                                                                    if (editedCategory == cat) Color(0xFF10B981)
                                                                    else appColors.border,
                                                                    RoundedCornerShape(6.dp)
                                                            )
                                                            .background(
                                                                    if (editedCategory == cat)
                                                                            Color(0xFF10B981).copy(alpha = 0.1f)
                                                                    else appColors.card
                                                            )
                                                            .clickable { editedCategory = cat }
                                                            .padding(10.dp)
                                    ) {
                                        Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center,
                                                modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                    icon,
                                                    contentDescription = cat.displayName,
                                                    tint = if (editedCategory == cat) Color(0xFF10B981) else appColors.mutedForeground,
                                                    modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                    cat.displayName,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 11.sp,
                                                    color = if (editedCategory == cat) Color(0xFF10B981) else appColors.foreground
                                            )
                                        }
                                    }
                                }
                                // Fill remaining space if row has less than 2 items
                                repeat(2 - rowCategories.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
                // Don't add to missing fields since we default to OTHER
            }
            
            // Description field (editable)
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text("Description", style = MaterialTheme.typography.bodyMedium, color = appColors.mutedForeground)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                        value = editedDescription,
                        onValueChange = { editedDescription = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        minLines = 2,
                        maxLines = 3,
                        placeholder = { Text("Enter description", style = MaterialTheme.typography.bodySmall) }
                )
            }
            if (data.description.isBlank()) missingFields.add("Description")
            
            // Show missing fields summary if any
            if (missingFields.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Missing: ${missingFields.joinToString(", ")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = appColors.mutedForeground,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            // Show "Save Expense" and "Cancel" buttons
            Spacer(Modifier.height(12.dp))
            
            // Validation: all required fields must be filled
            val isValid = editedAmount.toDoubleOrNull() != null && 
                         editedDescription.isNotBlank() &&
                         editedCurrency != null &&
                         editedCategory != null
            
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Cancel button
                OutlinedButton(
                        onClick = { 
                            voiceViewModel.cancelVoiceInput()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFEF4444)
                        )
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Cancel", fontWeight = FontWeight.Medium)
                }
                
                // Save button
                Button(
                        onClick = {
                            // Create updated data with all edited values
                            val updatedData = data.copy(
                                amount = editedAmount.toDoubleOrNull() ?: data.amount,
                                description = editedDescription.ifBlank { data.description },
                                currency = editedCurrency ?: data.currency,
                                category = editedCategory ?: data.category
                            )
                            onSaveExpense(updatedData)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = isValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981)
                        ),
                        shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Save",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Show hint for required fields
            if (!isValid) {
                Spacer(Modifier.height(4.dp))
                val missing = mutableListOf<String>()
                if (editedAmount.toDoubleOrNull() == null) missing.add("Amount")
                if (editedDescription.isBlank()) missing.add("Description")
                if (editedCurrency == null) missing.add("Currency")
                if (editedCategory == null) missing.add("Category")
                
                Text(
                    "⚠️ Required: ${missing.joinToString(", ")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
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

@Composable
private fun ManualTextEntryCard(
        text: String,
        onTextChange: (String) -> Unit,
        onParse: () -> Unit,
        onCancel: () -> Unit,
        appColors: com.example.theme.com.example.expensetracker.AppColorScheme
) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF3F4F6).copy(alpha = 0.5f)
            ),
            border = BorderStroke(1.dp, appColors.primary.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                        "✍️ Manual Entry",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = appColors.foreground
                )
                IconButton(
                        onClick = onCancel,
                        modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                            Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = appColors.mutedForeground,
                            modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                    "Enter your expense details in natural language",
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.mutedForeground
            )

            Spacer(Modifier.height(12.dp))

            // Text input field
            OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                    placeholder = {
                        Text(
                                "E.g., \"50 euros for lunch at restaurant\" or \"20 dollars for gas\"",
                                style = MaterialTheme.typography.bodySmall
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = appColors.primary,
                            unfocusedBorderColor = appColors.border,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    textStyle = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(12.dp))

            // Parse button
            Button(
                    onClick = onParse,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = text.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = appColors.primary
                    ),
                    shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Extract Expense Data", color = Color.White, fontWeight = FontWeight.Medium)
            }
        }
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
