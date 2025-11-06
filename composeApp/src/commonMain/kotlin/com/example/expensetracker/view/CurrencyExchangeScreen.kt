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
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.Expense
import com.example.theme.com.example.expensetracker.LocalAppColors
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Currency Exchange Screen (Full Screen)
 * 
 * Displays currency exchange information, converted expenses, and exchange rates.
 * Initially uses mock data, will be wired to real services later.
 * 
 * @param onNavigateBack Callback when back button is clicked
 * @param modifier Modifier for layout customization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyExchangeScreen(
    onNavigateBack: () -> Unit,
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
    
    // Mock data - will be replaced with real ViewModel later
    val baseCurrency = Currency.USD
    val mockExpenses = listOf(
        Expense(
            id = "1",
            category = com.example.expensetracker.model.ExpenseCategory.FOOD,
            description = "Lunch at restaurant",
            amount = 45.50,
            currency = Currency.EUR,
            date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        ),
        Expense(
            id = "2",
            category = com.example.expensetracker.model.ExpenseCategory.TRAVEL,
            description = "Gas station",
            amount = 120.00,
            currency = Currency.GBP,
            date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        )
    )
    
    // Mock converted amounts (will be calculated later)
    val mockConvertedAmounts = mapOf(
        "1" to 50.05, // EUR to USD
        "2" to 164.40 // GBP to USD
    )
    
    // Mock exchange rates (will be fetched from API later)
    val mockExchangeRates = mapOf(
        Currency.EUR to 1.10,
        Currency.GBP to 1.37,
        Currency.JPY to 0.0067,
        Currency.CHF to 1.12
    )
    
    // Mock last update time (2 hours ago)
    val lastUpdateTime = "Updated 2 hours ago"
    
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
                                    text = lastUpdateTime,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = appColors.mutedForeground
                                )
                            }
                            
                            // Rate list
                            mockExchangeRates.forEach { (currency, rate) ->
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
                                        text = String.format("%.4f", rate),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = appColors.foreground
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
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
                
            items(mockExpenses) { expense ->
                val convertedAmount = mockConvertedAmounts[expense.id]
                
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

