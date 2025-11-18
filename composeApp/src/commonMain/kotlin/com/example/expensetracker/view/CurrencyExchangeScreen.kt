package com.example.expensetracker.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.Expense
import com.example.expensetracker.model.ExpenseWithConversion
import com.example.expensetracker.viewmodel.CurrencyExchangeViewModel
import com.example.theme.com.example.expensetracker.LocalAppColors

/**
 * Currency Exchange Screen (Full Screen)
 * 
 * Displays currency exchange information, converted expenses, and exchange rates.
 * Uses CurrencyExchangeViewModel for real data from repositories.
 * 
 * @param onNavigateBack Callback when back button is clicked
 * @param viewModel The CurrencyExchangeViewModel instance (defaults to new instance)
 * @param modifier Modifier for layout customization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyExchangeScreen(
    onNavigateBack: () -> Unit,
    viewModel: CurrencyExchangeViewModel = viewModel { CurrencyExchangeViewModel() },
    modifier: Modifier = Modifier
) {
    // Navigation state for Currency Settings screen
    var showCurrencySettingsScreen by remember { mutableStateOf(false) }
    
    // Show Currency Settings screen if requested
    if (showCurrencySettingsScreen) {
        CurrencySettingsScreen(
            onNavigateBack = { showCurrencySettingsScreen = false }
        )
        return
    }
    val appColors = LocalAppColors.current
    
    // Observe ViewModel state
    val baseCurrency by viewModel.baseCurrency.collectAsState()
    val expensesWithConversion by viewModel.expensesWithConversion.collectAsState()
    val exchangeRates by viewModel.exchangeRates.collectAsState()
    val lastUpdateTime by viewModel.lastUpdateTime.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = appColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Currency Exchange",
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
                actions = {
                    IconButton(onClick = { showCurrencySettingsScreen = true }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Currency Settings",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            // Base Currency Section
            item {
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                            Text(
                                text = "Base Currency",
                                style = MaterialTheme.typography.labelMedium,
                                color = appColors.mutedForeground
                            )
                            Text(
                                text = "${baseCurrency.symbol} ${baseCurrency.displayName} (${baseCurrency.code})",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = appColors.foreground
                            )
                            Text(
                                text = "All expenses are converted to this currency",
                                style = MaterialTheme.typography.bodySmall,
                                color = appColors.mutedForeground
                            )
                    }
                }
            }
            
            // Exchange Rates Section
            item {
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Exchange Rates",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = appColors.foreground
                                )
                                Text(
                                    text = lastUpdateTime ?: "Never",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = appColors.mutedForeground
                                )
                            }
                            
                            // Rate list
                            if (exchangeRates.isEmpty()) {
                                Text(
                                    text = "No exchange rates available. Please refresh rates.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = appColors.mutedForeground
                                )
                            } else {
                                exchangeRates.forEach { (currency, rate) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${currency.code} (${currency.symbol})",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = appColors.foreground
                                        )
                                        Text(
                                            text = formatRate(rate),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = appColors.foreground
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Error message display
                            errorMessage?.let { error ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = error,
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            
                            Button(
                                onClick = { viewModel.refreshExchangeRates() },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = appColors.primary,
                                    contentColor = appColors.primaryForeground
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = appColors.primaryForeground,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Refresh Rates")
                            }
                    }
                }
            }
                
            // Converted Expenses Section
            item {
                Text(
                    text = "Converted Expenses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = appColors.foreground,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
                
            if (expensesWithConversion.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = appColors.card
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "No expenses to convert",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = appColors.mutedForeground
                        )
                    }
                }
            } else {
                items(expensesWithConversion) { expenseWithConversion ->
                    val expense = expenseWithConversion.expense
                    val convertedAmount = expenseWithConversion.convertedAmount
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = appColors.card
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
                                    text = expense.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = appColors.foreground
                                )
                                Text(
                                    text = expense.category.displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = appColors.mutedForeground
                                )
                            }
                            
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = expense.getFormattedAmount(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = appColors.mutedForeground
                                )
                                if (convertedAmount != null) {
                                    Text(
                                        text = baseCurrency.format(convertedAmount),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = appColors.foreground
                                    )
                                } else {
                                    Text(
                                        text = "N/A",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = appColors.mutedForeground,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }
                        }
                    }
                }
            }
                
            // Info Section
            item {
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
                            text = "• Exchange rates are updated automatically every 24 hours",
                            style = MaterialTheme.typography.bodySmall,
                            color = appColors.mutedForeground
                        )
                        Text(
                            text = "• Offline mode uses cached rates",
                            style = MaterialTheme.typography.bodySmall,
                            color = appColors.mutedForeground
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• Configure API key in ",
                                style = MaterialTheme.typography.bodySmall,
                                color = appColors.mutedForeground
                            )
                            TextButton(
                                onClick = { showCurrencySettingsScreen = true },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(20.dp)
                            ) {
                                Text(
                                    text = "Settings",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = appColors.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Formats a rate to 4 decimal places
 */
private fun formatRate(rate: Double): String {
    val roundedRate = kotlin.math.round(rate * 10000) / 10000
    return roundedRate.toString()
}

