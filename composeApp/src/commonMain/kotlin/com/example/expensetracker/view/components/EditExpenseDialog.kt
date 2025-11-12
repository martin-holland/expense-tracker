package com.example.expensetracker.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.expensetracker.domain.CurrencyConverter
import com.example.expensetracker.data.repository.SettingsRepository
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.ExpenseCategory
import com.example.theme.com.example.expensetracker.LocalAppColors
import kotlinx.datetime.LocalDateTime

/**
 * Dialog for editing expense details
 * Includes fields for category, description, amount, currency, and date
 * 
 * @param expense The expense to edit
 * @param onSave Callback when save is clicked with updated expense
 * @param onDismiss Callback when dialog is dismissed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseDialog(
    expense: Expense,
    onSave: (Expense) -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = LocalAppColors.current
    
    // Local state for form fields
    var selectedCategory by remember { mutableStateOf(expense.category) }
    var description by remember { mutableStateOf(expense.description) }
    var amountText by remember { mutableStateOf(expense.amount.toString()) }
    var selectedCurrency by remember { mutableStateOf(expense.currency) }
    var selectedDate by remember { mutableStateOf(expense.date) }
    
    // Dropdown states
    var categoryExpanded by remember { mutableStateOf(false) }
    var currencyExpanded by remember { mutableStateOf(false) }
    
    // Validation
    val amountError = amountText.toDoubleOrNull() == null && amountText.isNotEmpty()
    val isValid = description.isNotBlank() && amountText.toDoubleOrNull() != null
    
    // Currency conversion preview
    val currencyConverter = remember { CurrencyConverter.getInstance() }
    val settingsRepository = remember { SettingsRepository.getInstance() }
    var baseCurrency by remember { mutableStateOf<Currency?>(null) }
    var convertedAmount by remember { mutableStateOf<Double?>(null) }
    var isConverting by remember { mutableStateOf(false) }
    
    // Get base currency
    LaunchedEffect(Unit) {
        baseCurrency = settingsRepository.getBaseCurrencySync()
    }
    
    // Calculate converted amount when amount, currency, or base currency changes
    LaunchedEffect(amountText, selectedCurrency, baseCurrency) {
        val amount = amountText.toDoubleOrNull()
        val base = baseCurrency
        
        if (amount != null && base != null && selectedCurrency != base) {
            isConverting = true
            try {
                convertedAmount = currencyConverter.convertAmountSync(
                    amount = amount,
                    fromCurrency = selectedCurrency,
                    toCurrency = base,
                    date = selectedDate
                )
            } catch (e: Exception) {
                convertedAmount = null
            } finally {
                isConverting = false
            }
        } else {
            convertedAmount = null
        }
    }
    
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = appColors.card
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Title
                Text(
                    text = "Edit Expense",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = appColors.foreground
                )
                
                // Category Selector
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = appColors.foreground
                    )
                    
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory.displayName,
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(selectedCategory.backgroundColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = selectedCategory.icon,
                                        contentDescription = null,
                                        tint = appColors.foreground,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = appColors.primary,
                                unfocusedBorderColor = appColors.border
                            )
                        )
                        
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            ExpenseCategory.values().forEach { category ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(category.backgroundColor),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = category.icon,
                                                    contentDescription = null,
                                                    tint = appColors.foreground,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Text(category.displayName)
                                        }
                                    },
                                    onClick = {
                                        selectedCategory = category
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Description
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = appColors.foreground
                    )
                    
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("e.g., Lunch at restaurant") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = appColors.primary,
                            unfocusedBorderColor = appColors.border
                        ),
                        singleLine = true
                    )
                }
                
                // Amount and Currency Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Amount
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Amount",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = appColors.foreground
                        )
                        
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            placeholder = { Text("0.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = amountError,
                            supportingText = if (amountError) {
                                { Text("Invalid amount", color = appColors.destructive) }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = appColors.primary,
                                unfocusedBorderColor = appColors.border,
                                errorBorderColor = appColors.destructive
                            ),
                            singleLine = true
                        )
                    }
                    
                    // Currency
                    Column(
                        modifier = Modifier.width(120.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Currency",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = appColors.foreground
                        )
                        
                        ExposedDropdownMenuBox(
                            expanded = currencyExpanded,
                            onExpandedChange = { currencyExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = "${selectedCurrency.symbol} ${selectedCurrency.code}",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { 
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) 
                                },
                                modifier = Modifier
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                    .fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = appColors.primary,
                                    unfocusedBorderColor = appColors.border
                                ),
                                textStyle = MaterialTheme.typography.bodyMedium
                            )
                            
                            ExposedDropdownMenu(
                                expanded = currencyExpanded,
                                onDismissRequest = { currencyExpanded = false },
                                modifier = Modifier.heightIn(max = 200.dp)
                            ) {
                                Currency.values().forEach { currency ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = currency.symbol,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Column {
                                                    Text(
                                                        text = currency.code,
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                    Text(
                                                        text = currency.displayName,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = appColors.mutedForeground
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            selectedCurrency = currency
                                            currencyExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Currency Conversion Preview
                if (baseCurrency != null && selectedCurrency != baseCurrency && amountText.toDoubleOrNull() != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = appColors.secondary.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Converted to base currency",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = appColors.mutedForeground
                                )
                                if (isConverting) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = appColors.primary
                                        )
                                        Text(
                                            text = "Calculating...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = appColors.mutedForeground
                                        )
                                    }
                                } else if (convertedAmount != null) {
                                    Text(
                                        text = "≈ ${baseCurrency!!.format(convertedAmount!!)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = appColors.foreground
                                    )
                                } else {
                                    Text(
                                        text = "Conversion unavailable",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = appColors.mutedForeground,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Date Picker
                // TODO: Implement date picker in future version
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Date",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = appColors.foreground
                    )
                    
                    OutlinedTextField(
                        value = formatDateTimeForDisplay(selectedDate),
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Select date") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = appColors.primary,
                            unfocusedBorderColor = appColors.border,
                            disabledBorderColor = appColors.border,
                            disabledTextColor = appColors.foreground
                        ),
                        enabled = false // TODO: Enable when date picker is implemented
                    )
                    
                    Text(
                        text = "Date editing will be available in a future update",
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.mutedForeground
                    )
                }
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = appColors.foreground
                        )
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull()
                            if (isValid && amount != null) {
                                val updatedExpense = expense.copy(
                                    category = selectedCategory,
                                    description = description,
                                    amount = amount,
                                    currency = selectedCurrency,
                                    date = selectedDate
                                )
                                onSave(updatedExpense)
                                onDismiss()
                            }
                        },
                        enabled = isValid,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appColors.primary,
                            contentColor = appColors.primaryForeground
                        )
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

/**
 * Formats LocalDateTime for display
 * TODO: Use localized formatting
 */
private fun formatDateTimeForDisplay(dateTime: LocalDateTime): String {
    val monthNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    return "${monthNames[dateTime.monthNumber - 1]} ${dateTime.dayOfMonth}, ${dateTime.year}"
}

