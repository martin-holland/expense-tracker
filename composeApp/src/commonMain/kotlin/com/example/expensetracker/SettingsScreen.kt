package com.example.expensetracker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview


import expensetracker.composeapp.generated.resources.Res
import expensetracker.composeapp.generated.resources.compose_multiplatform

@Composable
@Preview
fun SettingsScreen() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Non-functional settings items
            SettingsItemVoiceInput(title = "Voice Input")
            SettingsItemCurrency(title = "Currency")
            SettingsItemAppearance(title = "Appearance")
            SettingsItemAccessibility(title = "Accessibility Features")

        }
    }
}



@Composable
fun SettingsItemVoiceInput(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
    ) {
        Row {
            Text("**IMG**")
            Column {
                Text(title) // heading text bold
                Text("Enable voice-based expense entry") // subheading text
            }
        }
        /// spaces

        // not sure if below theme and boxes should be in the same section?
        Text( // bold text, smaller than subheading, little bigger than normal i believe
            text = "Enable voice-based input",
        )
        Text("Use your voice to quickly add expenses")
        Text("**Toggle Icon**")


    }
}

@Composable
fun SettingsItemCurrency(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
    ) {
        Row {
            Text("**IMG**")
            Column {
                Text(title) // heading text bold
                Text("Select your prefered currency") // subheading text
            }
        }
        /// spaces

        // not sure if below theme and boxes should be in the same section?
        Text( // bold text, smaller than subheading, little bigger than normal i believe
            text = "Default currency",
        )
        TestDropdown()

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestDropdown() {
    var expanded by remember { mutableStateOf(false) }      // menu visibility
    var selectedOption by remember { mutableStateOf("Choose") } // selected item
    val options = listOf("Euro", "Dollar", "Yen")

    Column(modifier = Modifier.padding(16.dp)) {
        // TextField that opens dropdown
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedOption = option
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}



@Composable
fun SettingsItemAppearance(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
    ) {
        Row {
            Text("**IMG**")
            Column {
                Text(title) // heading text bold
                Text("Customize the app theme") // subheading text
            }
        }
         /// spaces

        // not sure if below theme and boxes should be in the same section?
        Text( // bold text, smaller than subheading, little bigger than normal i believe
            text = "Theme",
        )
        Row {
            Text("Light")
            Text("Dark")
            Text("System")
        }

    }
}

@Composable
fun SettingsItemAccessibility(title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "This app includes the following accessibility features:\n" +
                    "\n" +
                    "•  Voice input for hands-free expense entry\n" +
                    "•  High contrast colors meeting WCAG 2.1 standards\n" +
                    "•  Large touch targets for easier interaction\n" +
                    "•  Clear visual hierarchy and spacing\n" +
                    "•  Dark mode support to reduce eye strain\n" +
                    "•  Semantic labels for screen readers"
        )
    }
}