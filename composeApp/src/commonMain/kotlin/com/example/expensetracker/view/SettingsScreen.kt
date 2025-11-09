package com.example.expensetracker.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.ThemeOption
import com.example.expensetracker.viewmodel.SettingsViewModel
import com.example.theme.com.example.expensetracker.LocalAppColors

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val appColors = LocalAppColors.current
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val selectedThemeOption by viewModel.selectedThemeOption.collectAsState()
    val isVoiceInputEnabled by viewModel.isVoiceInputEnabled.collectAsState()

    Column(
        modifier = Modifier
            .background(appColors.background)
            .safeContentPadding()
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = appColors.foreground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Subheading
        Text(
            text = "Expense Tracker",
            style = MaterialTheme.typography.bodyMedium,
            color = appColors.mutedForeground
        )

        // Sections
        SettingsCardVoiceInput(
            isVoiceInputEnabled = isVoiceInputEnabled,
            onVoiceInputToggled = { viewModel.toggleVoiceInput(it) }
        )

        SettingsCardCurrency(
            selectedCurrency = selectedCurrency,
            onCurrencySelected = { viewModel.setCurrency(it) }
        )

        SettingsCardAppearance(
            selectedThemeOption = selectedThemeOption,
            onThemeOptionSelected = { viewModel.setThemeOption(it) }
        )

        SettingsAccessibilityFeatures()
        SettingsAppInfo()
    }
}

// Voice Input Card
@Composable
fun SettingsCardVoiceInput(
    isVoiceInputEnabled: Boolean,
    onVoiceInputToggled: (Boolean) -> Unit
) {
    val appColors = LocalAppColors.current

    SettingsCard(
        icon = Icons.Default.Mic,
        title = "Voice Input",
        description = "Enable voice-based expense entry",
        iconTint = Color(0xFF0D9488),
        iconBackgroundColor = Color(0xFFCCFBF1)

    ) {
        Text(
            text = "Enable voice-based input",
            fontWeight = FontWeight.SemiBold,
            color = appColors.foreground,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Use your voice to quickly add expenses",
                    color = appColors.foreground
                )
            }
            Switch(
                checked = isVoiceInputEnabled,
                onCheckedChange = { onVoiceInputToggled(it) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsCardCurrency(
    selectedCurrency: Currency,
    onCurrencySelected: (Currency) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val appColors = LocalAppColors.current

    SettingsCard(
        icon = Icons.Default.AttachMoney,
        title = "Currency",
        description = "Select your preferred currency",
        iconTint = Color(0xFF2563EB),
        iconBackgroundColor = Color(0xFFDBEAFE)
    ) {
        Text(
            "Default Currency",
            fontWeight = FontWeight.SemiBold,
            color = appColors.foreground
        )
        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = "${selectedCurrency.symbol} ${selectedCurrency.displayName} (${selectedCurrency.code})",
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                Currency.entries.forEach { currency ->
                    DropdownMenuItem(
                        text = {
                            Text("${currency.symbol} ${currency.displayName} (${currency.code})")
                        },
                        onClick = {
                            onCurrencySelected(currency)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsCardAppearance(
    selectedThemeOption: ThemeOption,
    onThemeOptionSelected: (ThemeOption) -> Unit
) {
    val appColors = LocalAppColors.current

    SettingsCard(
        icon = Icons.Default.Palette,
        title = "Appearance",
        description = "Customize the app theme",
        iconTint = Color(0xFF9333EA),
        iconBackgroundColor = Color(0xFFF3E8FF)
    ) {
        Text(
            "Theme",
            fontWeight = FontWeight.SemiBold,
            color = appColors.foreground
        )
        Spacer(Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            ThemeOptionButton(
                text = "Light",
                icon = Icons.Filled.LightMode,
                selected = selectedThemeOption == ThemeOption.LIGHT,
                onClick = { onThemeOptionSelected(ThemeOption.LIGHT) }
            )
            ThemeOptionButton(
                text = "Dark",
                icon = Icons.Filled.DarkMode,
                selected = selectedThemeOption == ThemeOption.DARK,
                onClick = { onThemeOptionSelected(ThemeOption.DARK) }
            )
            ThemeOptionButton(
                text = "System",
                icon = Icons.Filled.Computer,
                selected = selectedThemeOption == ThemeOption.SYSTEM,
                onClick = { onThemeOptionSelected(ThemeOption.SYSTEM) }
            )
        }
    }
}

@Composable
fun ThemeOptionButton(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val appColors = LocalAppColors.current
    val bg = if (selected) appColors.primary else appColors.card
    val color = if (selected) appColors.primaryForeground else appColors.foreground
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = color),
        shape = RoundedCornerShape(12.dp),


    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SettingsAccessibilityFeatures() {
    val appColors = LocalAppColors.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(
                width = 1.dp,
                color = appColors.border,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.card)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Accessibility,
                    contentDescription = null,
                    tint = appColors.primary
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        "Accessibility",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = appColors.foreground
                    )
                    Text(
                        "Built-in accessibility features",
                        color = appColors.mutedForeground
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Content
            Text(
                "This app includes the following accessibility features:",
                style = MaterialTheme.typography.bodyMedium,
                color = appColors.mutedForeground,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val features = listOf(
                "Voice input for hands-free expense entry",
                "High contrast colors meeting WCAG 2.1 standards",
                "Large touch targets for easier interaction",
                "Clear visual hierarchy and spacing",
                "Dark mode support to reduce eye strain",
                "Semantic labels for screen readers"
            )

            Column(modifier = Modifier.padding(start = 8.dp)) {
                features.forEach { feature ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Text("• ", color = appColors.mutedForeground)
                        Text(feature, color = appColors.mutedForeground)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsAppInfo() {
    val appColors = LocalAppColors.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(
                width = 1.dp,
                color = appColors.border,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.card)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
            .fillMaxWidth()
            .padding(16.dp),

        horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center

        ) {
            Icon(
                imageVector = Icons.Default.Money,
                contentDescription = null,
                tint = appColors.primary,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .size(48.dp)
            )

            Text(
                text = "Expense Tracker",
                style = MaterialTheme.typography.titleMedium,
                color = appColors.foreground
            )
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                color = appColors.mutedForeground
            )
            Text(
                text = "Material Design 3 • Jetpack Compose Ready",
                style = MaterialTheme.typography.bodySmall,
                color = appColors.mutedForeground
            )
        }
    }
}

// Composable for shared components
@Composable
fun SettingsCard(
    icon: ImageVector,
    title: String,
    description: String,
    iconTint: Color = Color.Transparent,
    iconBackgroundColor: Color = Color.Transparent,
    content: @Composable ColumnScope.() -> Unit
) {
    val appColors = LocalAppColors.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(
                width = 1.dp,
                color = appColors.border,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.card)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (iconTint != Color.Transparent) iconTint else appColors.primary,
                    modifier = Modifier
                        .background(iconBackgroundColor, RoundedCornerShape(8.dp))
                        .padding(4.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = appColors.foreground
                    )
                    Text(
                        description,
                        color = appColors.mutedForeground
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}