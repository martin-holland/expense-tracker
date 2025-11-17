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
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.ExpenseCategory
import com.example.expensetracker.service.getMicrophoneService
import com.example.expensetracker.view.components.camera.CameraScreen
import com.example.expensetracker.viewmodel.AddExpenseViewModel
import com.example.expensetracker.viewmodel.VoiceInputViewModel
import com.example.theme.com.example.expensetracker.LocalAppColors
import kotlin.time.ExperimentalTime
import kotlinx.datetime.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalTime::class)
@Composable
fun AddExpenseScreen(viewModel: AddExpenseViewModel = viewModel()) {

    val voiceViewModel: VoiceInputViewModel = viewModel()

    val appColors = LocalAppColors.current   // <<< DARK MODE FIX
    val accentGreen = appColors.chart2
    val sectionShape = RoundedCornerShape(12.dp)

    val currency = viewModel.currency
    val amount = viewModel.amount
    val category = viewModel.category
    val note = viewModel.note
    val date = viewModel.date
    val showVoiceSection by voiceViewModel.showVoiceSection.collectAsState()
    val errorMessage = viewModel.errorMessage
    val isSaving = viewModel.isSaving
    val saveSuccess = viewModel.saveSuccess

    var showDialog by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            kotlinx.coroutines.delay(2000)
            viewModel.resetSuccessState()
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .background(appColors.background) // <<< FIX
            .padding(20.dp)
    ) {

        // -------------------------------------------------------
        // HEADER
        // -------------------------------------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(accentGreen.copy(0.15f)),
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
                    Text("Add Expense",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.foreground   // <<< FIX
                    )

                    Text("Expense Tracker",
                        fontSize = 14.sp,
                        color = appColors.mutedForeground  // <<< FIX
                    )
                }
            }

            Text(
                text = "Cancel",
                color = appColors.foreground,  // <<< FIX
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { }
            )
        }

        Spacer(Modifier.height(24.dp))

        // -------------------------------------------------------
        // CURRENCY SECTION
        // -------------------------------------------------------
        SectionCard(title = "Currency") {

            val allCurrencies = Currency.values().toList()

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                allCurrencies.chunked(3).forEach { rowCurrencies ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowCurrencies.forEach { curr ->
                            Button(
                                onClick = { viewModel.onCurrencySelected(curr) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor =
                                        if (currency == curr) appColors.primary
                                        else appColors.card,   // <<< FIX
                                    contentColor =
                                        if (currency == curr) appColors.primaryForeground
                                        else appColors.foreground  // <<< FIX
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, appColors.border), // <<< FIX
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    "${curr.symbol} ${curr.code}",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        repeat(3 - rowCurrencies.size) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------
        // AMOUNT SECTION
        // -------------------------------------------------------
        SectionCard(title = "Amount") {
            OutlinedTextField(
                value = amount,
                onValueChange = { viewModel.onAmountChanged(it) },
                placeholder = {
                    Text("${currency.symbol} 0.00", color = appColors.mutedForeground)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = appColors.inputBackground, // <<< FIX
                    unfocusedContainerColor = appColors.inputBackground,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = appColors.primary
                )
            )
        }

        // -------------------------------------------------------
        // CATEGORY SECTION
        // -------------------------------------------------------
        SectionCard(title = "Category") {

            val categories = listOf(
                Triple(ExpenseCategory.FOOD, Icons.Default.Fastfood, Color(0xFFFFEAEA)),
                Triple(ExpenseCategory.TRAVEL, Icons.Default.DirectionsCar, Color(0xFFE5F8FA)),
                Triple(ExpenseCategory.UTILITIES, Icons.Default.ElectricBolt, Color(0xFFEAF9EE)),
                Triple(ExpenseCategory.OTHER, Icons.Default.MoreHoriz, Color(0xFFFFF8E8))
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.chunked(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { (cat, icon, bgColor) ->

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.onCategorySelected(cat) },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (category == cat) accentGreen else appColors.border
                                ),
                                colors = CardDefaults.cardColors(appColors.card) // <<< FIX
                            ) {

                                Column(
                                    modifier = Modifier.padding(vertical = 14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {

                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
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
                                        color = appColors.foreground  // <<< FIX
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------
        // DATE SECTION
        // -------------------------------------------------------
        SectionCard(title = "Date") {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
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
                                    val localDate = instant
                                        .toLocalDateTime(TimeZone.currentSystemDefault())
                                        .date

                                    val monthName = localDate.month.name
                                        .lowercase()
                                        .replaceFirstChar { it.uppercase() }

                                    val formatted =
                                        "$monthName ${localDate.dayOfMonth}, ${localDate.year}"

                                    viewModel.onDateSelected(formatted)
                                }
                                showDialog = false
                            }
                        ) {
                            Text("OK", color = accentGreen)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancel", color = accentGreen)
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        }

        Spacer(Modifier.height(30.dp))

        // -------------------------------------------------------
        // NOTE SECTION
        // -------------------------------------------------------
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = appColors.inputBackground,
                    unfocusedContainerColor = appColors.inputBackground,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = appColors.primary
                )
            )
        }

        if (showCamera) CameraScreen()

        // -------------------------------------------------------
        // QUICK INPUT SECTION
        // -------------------------------------------------------
        Text(
            "Quick Input",
            color = appColors.foreground, // <<< FIX
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        DashedCard(accentGreen) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                QuickInputItem(
                    label = "Voice Input",
                    subtext = if (showVoiceSection) "Tap to close" else "Tap to speak",
                    icon = Icons.Default.Mic,
                    accent = accentGreen,
                    action = { voiceViewModel.toggleVoiceSection() }
                )

                QuickInputItem(
                    label = "Receipt",
                    subtext = if (!showCamera) "Tap to capture" else "Tap to close",
                    icon = Icons.Default.CameraAlt,
                    accent = accentGreen,
                    action = { showCamera = !showCamera }
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        VoiceInputSection(voiceViewModel)

        Spacer(Modifier.height(40.dp))

        // -------------------------------------------------------
        // SAVE BUTTON
        // -------------------------------------------------------
        Button(
            onClick = { viewModel.saveExpense() },
            enabled = !isSaving,
            colors = ButtonDefaults.buttonColors(containerColor = accentGreen),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
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

        // -------------------------------------------------------
        // SUCCESS
        // -------------------------------------------------------
        if (saveSuccess) {
            Spacer(Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "✓ Expense saved successfully!",
                    color = Color(0xFF2E7D32),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // -------------------------------------------------------
        // ERROR
        // -------------------------------------------------------
        errorMessage?.let {
            Spacer(Modifier.height(10.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    it,
                    color = Color(0xFFC62828),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

// ========================================================================
// REUSABLE SECTION CARD
// ========================================================================
@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val appColors = LocalAppColors.current

    Card(
        colors = CardDefaults.cardColors(appColors.card), // <<< FIX
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, appColors.border),   // <<< FIX
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
    ) {

        Column(Modifier.padding(16.dp)) {

            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                color = appColors.foreground  // <<< FIX
            )

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
    voiceViewModel: VoiceInputViewModel = viewModel()
) {
    val isRecording by voiceViewModel.isRecording.collectAsState()
    val audioData by voiceViewModel.audioData.collectAsState()
    val isProcessing by voiceViewModel.isProcessing.collectAsState()
    val errorMessage by voiceViewModel.errorMessage.collectAsState()
    val showVoiceSection by voiceViewModel.showVoiceSection.collectAsState()

    val appColors = LocalAppColors.current
    val accentGreen = appColors.chart2

    if (showVoiceSection) {

        SectionCard(title = "Voice Input") {

            errorMessage?.let { error ->
                Text(
                    error,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                colors = CardDefaults.cardColors(appColors.inputBackground), // <<< FIX
                border = BorderStroke(1.dp, appColors.border) // <<< FIX
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        when {
                            isRecording -> "🎤 Recording..."
                            audioData != null -> "✅ Audio recorded (${audioData!!.size} bytes)"
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
                    if (isRecording) voiceViewModel.stopRecording()
                    else voiceViewModel.startRecording()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        if (isRecording) Color(0xFFFF6B6B)
                        else accentGreen
                ),
                shape = RoundedCornerShape(10.dp),
                enabled = !isProcessing
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
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = appColors.mutedForeground.copy(alpha = 0.5f)
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
                    colors = ButtonDefaults.buttonColors(appColors.chart1),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("▶️")
                    Spacer(Modifier.width(8.dp))
                    Text("Play", color = Color.White)
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "Microphone Permission: ${
                    if (getMicrophoneService().hasMicrophonePermission()) "Granted"
                    else "Not Granted"
                }",
                fontSize = 12.sp,
                color = if (getMicrophoneService().hasMicrophonePermission())
                    accentGreen else Color(0xFFFF6B6B)
            )
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
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(accent)
                .clickable { action() },
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
private fun DashedCard(
    accent: Color,
    content: @Composable BoxScope.() -> Unit
) {
    val appColors = LocalAppColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
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
                style = Stroke(
                    width = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                ),
                cornerRadius = CornerRadius(12.dp.toPx())
            )
        }

        content()
    }
}
