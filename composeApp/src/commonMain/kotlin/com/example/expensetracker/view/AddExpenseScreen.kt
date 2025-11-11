package com.example.expensetracker.view

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
            Text("Cancel", color = AppColors.foreground, fontWeight = FontWeight.Medium)
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
            OutlinedTextField(
                value = amount,
                onValueChange = { viewModel.onAmountChanged(it) },
                placeholder = { Text("$ 0.00", color = AppColors.mutedForeground) },
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

        Spacer(Modifier.height(40.dp))
    }
}



// ---------- REUSABLE COMPOSABLES ----------

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
private fun QuickInputItem(label: String, subtext: String, icon: ImageVector, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
