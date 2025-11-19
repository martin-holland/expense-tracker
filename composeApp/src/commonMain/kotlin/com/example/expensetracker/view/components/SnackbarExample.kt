package com.example.expensetracker.view.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Example showing how to use the reusable Snackbar component
 * 
 * This file demonstrates the usage pattern - you can delete this file
 * or keep it as a reference for other developers.
 */
@Composable
fun SnackbarUsageExample() {
    // Step 1: Create a state to hold the snackbar message
    var snackbarMessage by remember { mutableStateOf<SnackbarMessage?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Step 2: Place SnackbarHost at the top of your screen
        SnackbarHost(
            message = snackbarMessage,
            onDismiss = { snackbarMessage = null },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // Your screen content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Snackbar Examples", style = MaterialTheme.typography.headlineSmall)

            // Step 3: Show snackbars by setting the message state
            Button(
                onClick = {
                    snackbarMessage = SnackbarMessage(
                        message = "Operation completed successfully!",
                        type = SnackbarType.SUCCESS
                    )
                }
            ) {
                Text("Show Success")
            }

            Button(
                onClick = {
                    snackbarMessage = SnackbarMessage(
                        message = "An error occurred",
                        type = SnackbarType.ERROR
                    )
                }
            ) {
                Text("Show Error")
            }

            Button(
                onClick = {
                    snackbarMessage = SnackbarMessage(
                        message = "Please review this information",
                        type = SnackbarType.WARNING
                    )
                }
            ) {
                Text("Show Warning")
            }

            Button(
                onClick = {
                    snackbarMessage = SnackbarMessage(
                        message = "Here's some helpful information",
                        type = SnackbarType.INFO,
                        duration = 5000L // Custom duration (5 seconds)
                    )
                }
            ) {
                Text("Show Info (5s)")
            }
        }
    }
}

/**
 * Usage in ViewModels:
 * 
 * ```kotlin
 * class MyViewModel : ViewModel() {
 *     var snackbarMessage by mutableStateOf<SnackbarMessage?>(null)
 *         private set
 *     
 *     fun showSuccess() {
 *         snackbarMessage = SnackbarMessage(
 *             message = "Success!",
 *             type = SnackbarType.SUCCESS
 *         )
 *     }
 *     
 *     fun dismissSnackbar() {
 *         snackbarMessage = null
 *     }
 * }
 * ```
 * 
 * Usage in Composables:
 * 
 * ```kotlin
 * @Composable
 * fun MyScreen(viewModel: MyViewModel = viewModel()) {
 *     Box(modifier = Modifier.fillMaxSize()) {
 *         SnackbarHost(
 *             message = viewModel.snackbarMessage,
 *             onDismiss = { viewModel.dismissSnackbar() },
 *             modifier = Modifier.align(Alignment.TopCenter)
 *         )
 *         
 *         // Your screen content
 *     }
 * }
 * ```
 */

