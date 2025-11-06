package com.example.expensetracker.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.expensetracker.model.Currency
import com.example.theme.com.example.expensetracker.LocalAppColors

/**
 * Currency Settings Screen
 * 
 * Displays currency exchange settings including base currency, API configuration, and exchange rate management.
 * Initially uses mock data, will be wired to SettingsViewModel later.
 * 
 * @param onNavigateBack Callback when back button is clicked
 * @param modifier Modifier for layout customization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    
    // Mock state - will be replaced with ViewModel later
    var selectedCurrency by remember { mutableStateOf(Currency.USD) }
    var currencyExpanded by remember { mutableStateOf(false) }
    var apiKey by remember { mutableStateOf("") }
    var apiBaseUrl by remember { mutableStateOf("https://v6.exchangerate-api.com/v6") }
    var isApiConfigured by remember { mutableStateOf(false) }
    var lastUpdateTime by remember { mutableStateOf<String?>(null) }
    var showApiKey by remember { mutableStateOf(false) }
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = appColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Currency Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = appColors.foreground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = appColors.foreground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = appColors.background,
                    titleContentColor = appColors.foreground,
                    navigationIconContentColor = appColors.foreground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Base Currency Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = appColors.card
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Base Currency",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = appColors.foreground
                    )
                    
                    // Currency Dropdown
                    ExposedDropdownMenuBox(
                        expanded = currencyExpanded,
                        onExpandedChange = { currencyExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = "${selectedCurrency.symbol} ${selectedCurrency.displayName} (${selectedCurrency.code})",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Base Currency") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = appColors.foreground,
                                unfocusedTextColor = appColors.foreground,
                                focusedLabelColor = appColors.mutedForeground,
                                unfocusedLabelColor = appColors.mutedForeground
                            )
                        )
                        
                        ExposedDropdownMenu(
                            expanded = currencyExpanded,
                            onDismissRequest = { currencyExpanded = false },
                            modifier = Modifier.heightIn(max = 300.dp)
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
                                    },
                                    leadingIcon = if (currency == selectedCurrency) {
                                        {
                                            Icon(
                                                imageVector = Icons.Filled.CheckCircle,
                                                contentDescription = "Selected",
                                                tint = appColors.primary
                                            )
                                        }
                                    } else null
                                )
                            }
                        }
                    }
                    
                    Text(
                        text = "All expenses will be converted to this currency",
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.mutedForeground
                    )
                }
            }
            
            // Exchange Rate API Configuration Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = appColors.card
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Exchange Rate API",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = appColors.foreground
                        )
                        
                        // Status indicator
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isApiConfigured) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                                contentDescription = null,
                                tint = if (isApiConfigured) appColors.primary else appColors.mutedForeground,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isApiConfigured) "Configured" else "Not Configured",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isApiConfigured) appColors.primary else appColors.mutedForeground
                            )
                        }
                    }
                    
                    // API Key input
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API Key") },
                        placeholder = { Text("Enter your API key") },
                        visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showApiKey = !showApiKey }) {
                                Text(
                                    text = if (showApiKey) "Hide" else "Show",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = appColors.primary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = appColors.foreground,
                            unfocusedTextColor = appColors.foreground,
                            focusedLabelColor = appColors.mutedForeground,
                            unfocusedLabelColor = appColors.mutedForeground
                        )
                    )
                    
                    // API Base URL input
                    OutlinedTextField(
                        value = apiBaseUrl,
                        onValueChange = { apiBaseUrl = it },
                        label = { Text("API Base URL") },
                        placeholder = { Text("https://v6.exchangerate-api.com/v6") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = appColors.foreground,
                            unfocusedTextColor = appColors.foreground,
                            focusedLabelColor = appColors.mutedForeground,
                            unfocusedLabelColor = appColors.mutedForeground
                        )
                    )
                    
                    // Test API Connection button
                    Button(
                        onClick = {
                            // Mock implementation: Log for now
                            println("Test API connection clicked - will test connection later")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appColors.primary,
                            contentColor = appColors.primaryForeground
                        )
                    ) {
                        Text("Test API Connection")
                    }
                }
            }
            
            // Exchange Rate Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = appColors.card
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Exchange Rates",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = appColors.foreground
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Last Update",
                            style = MaterialTheme.typography.bodyMedium,
                            color = appColors.mutedForeground
                        )
                        Text(
                            text = lastUpdateTime ?: "Never",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = appColors.foreground
                        )
                    }
                    
                    Button(
                        onClick = {
                            // Mock implementation: Log for now
                            println("Refresh rates clicked - will refresh from API later")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appColors.primary,
                            contentColor = appColors.primaryForeground
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Refresh Now")
                    }
                    
                    Text(
                        text = "Rates refresh automatically every 24 hours",
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.mutedForeground
                    )
                }
            }
            
            // Info Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = appColors.secondary.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "ℹ️ Information",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = appColors.foreground
                    )
                    Text(
                        text = "• Offline mode uses cached rates when available",
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.mutedForeground
                    )
                    Text(
                        text = "• API key is required for exchange rate updates",
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.mutedForeground
                    )
                    Text(
                        text = "• Get your free API key at: exchangerate-api.com",
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "• Cross-rate calculation optimizes API usage (single call per day)",
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.mutedForeground
                    )
                }
            }
        }
    }
}

