package com.example.expensetracker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.theme.com.example.expensetracker.LocalAppColors
import com.example.theme.com.example.expensetracker.ThemeProvider
import expensetracker.composeapp.generated.resources.Res
import expensetracker.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    ThemeProvider { AppContent() }
}

@Composable
fun AppContent() {
    val appColors = LocalAppColors.current // Access custom colors like mutedForeground
    var showContent by remember { mutableStateOf(false) }
    Column(
        // Example of colour usage from theme:
            modifier =
                    Modifier.background(LocalAppColors.current.testChart)
                            .safeContentPadding()
                            .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(onClick = { showContent = !showContent }) { Text("Click me!") }
        AnimatedVisibility(showContent) {
            val greeting = remember { Greeting().greet() }
            Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(painterResource(Res.drawable.compose_multiplatform), null)
                // Example: Using mutedForeground from custom theme colors
                Text("Compose: $greeting", color = appColors.mutedForeground)
            }
        }
    }
}
