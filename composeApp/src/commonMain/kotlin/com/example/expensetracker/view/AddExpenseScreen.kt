package com.example.expensetracker.view

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.example.expensetracker.service.getMicrophoneService
import androidx.compose.material.icons.filled.AttachMoney
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
import com.example.expensetracker.viewmodel.AddExpenseViewModel
import com.example.theme.com.example.expensetracker.AppColors
import com.example.theme.com.example.expensetracker.LocalAppColors
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalFoundationApi::class, ExperimentalTime::class)
@Composable
fun AddExpenseScreen(viewModel: AddExpenseViewModel = viewModel()) {
    val appColors = LocalAppColors.current
    val accentGreen = appColors.chart2
    val sectionShape = RoundedCornerShape(12.dp)

    val currency = viewModel.currency
    val amount = viewModel.amount
    val category = viewModel.category
    val note = viewModel.note
    val date = viewModel.date
    var showVoiceSection by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .background(AppColors.background)
            .padding(20.dp)
    ) {
        // === Header ===
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
                        .background(accentGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AttachMoney, contentDescription = null, tint = accentGreen)
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Add Expense", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Expense Tracker", fontSize = 14.sp, color = AppColors.mutedForeground)
                }
            }
            // Add a clickable "Cancel" text with a TODO() placeholder
            Text(
                text = "Cancel",
                color = AppColors.foreground,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable {
                    // TODO: Implement navigation back or clear fields
                }
            )
        }

        Spacer(Modifier.height(24.dp))

        // === Currency Section ===
        SectionCard(title = "Currency") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("USD", "EUR", "GBP").forEach { curr ->
                    val symbol = when (curr) {
                        "USD" -> "$"
                        "EUR" -> "€"
                        "GBP" -> "£"
                        else -> curr
                    }

                    Button(
                        onClick = { viewModel.onCurrencySelected(curr) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currency == curr) AppColors.primary else Color.White,
                            contentColor = if (currency == curr) AppColors.primaryForeground else AppColors.foreground
                        ),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AppColors.border),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("$symbol $curr", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // === Amount Section ===
        SectionCard(title = "Amount") {
            val symbol = when (currency) {
                "USD" -> "$"
                "EUR" -> "€"
                "GBP" -> "£"
                else -> currency
            }
            OutlinedTextField(
                value = amount,
                onValueChange = { viewModel.onAmountChanged(it) },
                placeholder = { Text("$symbol 0.00", color = AppColors.mutedForeground) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AppColors.inputBackground,
                    unfocusedContainerColor = AppColors.inputBackground,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        }
        // === Category Section ===
        SectionCard(title = "Category") {
            val categories = listOf(
                Triple("Food", Icons.Default.Fastfood, Color(0xFFFFEAEA)),
                Triple("Travel", Icons.Default.DirectionsCar, Color(0xFFE5F8FA)),
                Triple("Utilities", Icons.Default.ElectricBolt, Color(0xFFEAF9EE)),
                Triple("Other", Icons.Default.MoreHoriz, Color(0xFFFFF8E8))
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { (label, icon, bgColor) ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.onCategorySelected(label) },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (category == label) accentGreen else AppColors.border
                                ),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
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
                                            contentDescription = label,
                                            tint = accentGreen,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val instant = Instant.fromEpochMilliseconds(millis)
                                val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                                val monthName = localDate.month.name.lowercase().replaceFirstChar { it.uppercase() }
                                val formatted = "$monthName ${localDate.dayOfMonth}, ${localDate.year}"
                                viewModel.onDateSelected(formatted)
                            }
                            showDialog = false
                        }) { Text("OK", color = accentGreen) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) { Text("Cancel", color = accentGreen) }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        }

        Spacer(Modifier.height(30.dp))


        // === Note Section ===
        SectionCard(title = "Note (Optional)") {
            OutlinedTextField(
                value = note,
                onValueChange = { viewModel.onNoteChanged(it) },
                placeholder = { Text("Add a note about this expense...", color = AppColors.mutedForeground) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AppColors.inputBackground,
                    unfocusedContainerColor = AppColors.inputBackground,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        }

        // === Quick Input Section ===
        Text("Quick Input", color = AppColors.foreground, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        DashedCard(accentGreen) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickInputItem(
                    label = "Voice Input",
                    subtext = "Tap to speak",
                    icon = Icons.Default.Mic,
                    accent = accentGreen,
                    onClick = { showVoiceSection = !showVoiceSection }
                )
                QuickInputItem(
                    label = "Receipt",
                    subtext = "Tap to capture",
                    icon = Icons.Default.CameraAlt,
                    accent = accentGreen
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        VoiceInputSection(showVoiceSection = showVoiceSection)

        Spacer(Modifier.height(40.dp))
        Button(
            onClick = { viewModel.saveExpense() },
            colors = ButtonDefaults.buttonColors(containerColor = accentGreen),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
        ) {
            Text("Save Expense", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        viewModel.errorMessage?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = Color.Red, fontSize = 14.sp)
        }
    }
}



// ----------  COMPOSABLES ----------

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, AppColors.border),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = AppColors.foreground)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun VoiceInputSection(showVoiceSection: Boolean) {
    val microphoneService = getMicrophoneService()
    var isRecording by remember { mutableStateOf(false) }
    var audioData by remember { mutableStateOf<ByteArray?>(null) }
    var transcription by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val appColors = LocalAppColors.current
    val accentGreen = appColors.chart2

    if (showVoiceSection) {
        SectionCard(title = "Voice Input") {
            // Recording Status Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.inputBackground),
                border = BorderStroke(1.dp, AppColors.border)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isRecording) {
                        Text(
                            "🎤 Recording...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = accentGreen,
                            fontWeight = FontWeight.Medium
                        )
                    } else if (audioData != null) {
                        Text(
                            "✅ Audio recorded (${audioData!!.size} bytes)",
                            style = MaterialTheme.typography.bodyLarge,
                            color = accentGreen,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            "Tap record to start voice input",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.mutedForeground
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Recording Button
            Button(
                onClick = {
                    scope.launch {
                        if (isRecording) {
                            val data = microphoneService.stopRecording()
                            audioData = data
                            isRecording = false
                        } else {
                            val success = microphoneService.startRecording()
                            if (success) {
                                isRecording = true
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) Color(0xFFFF6B6B) else accentGreen
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isRecording) "Stop Recording" else "Start Recording",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(12.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Transcribe Button
                Button(
                    onClick = {
                        // Non-functional for now
                    },
                    enabled = false, // Disabled since not implemented
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.mutedForeground.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("📝")
                    Spacer(Modifier.width(8.dp))
                    Text("Transcribe", color = Color.White)
                }


                // Play Button
                Button(
                    onClick = {
                        scope.launch {
                            audioData?.let { data ->
                                microphoneService.playAudio(data)
                            }
                        }
                    },
                    enabled = audioData != null && !isRecording,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.chart1
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("▶️")
                    Spacer(Modifier.width(8.dp))
                    Text("Play", color = Color.White)
                }
            }

            // Transcription Display
            if (transcription.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AppColors.inputBackground),
                    border = BorderStroke(1.dp, AppColors.border)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Transcription",
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.foreground
                            )
                            Text(
                                "${transcription.length} chars",
                                fontSize = 12.sp,
                                color = AppColors.mutedForeground
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = transcription,
                            modifier = Modifier.fillMaxWidth(),
                            color = AppColors.foreground,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Permission Status
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Microphone Permission: ${if (microphoneService.hasMicrophonePermission()) "Granted" else "Not Granted"}",
                fontSize = 12.sp,
                color = if (microphoneService.hasMicrophonePermission()) accentGreen else Color(
                    0xFFFF6B6B
                )
            )
        }
    }

}

@Composable
private fun QuickInputItem(
    label: String,
    subtext: String,
    icon: ImageVector,
    accent: Color,
    onClick: (() -> Unit)? = null
    ) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        )
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(accent),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = AppColors.foreground, fontSize = 14.sp)
        Text(subtext, color = AppColors.mutedForeground, fontSize = 12.sp)
    }
}

@Composable
private fun DashedCard(accent: Color, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(accent.copy(alpha = 0.05f))
            .border(
                BorderStroke(1.dp, Brush.horizontalGradient(listOf(accent, accent))),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRoundRect(
                color = accent.copy(alpha = 0.4f),
                style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))),
                cornerRadius = CornerRadius(12.dp.toPx())
            )
        }
        content()
    }
}
