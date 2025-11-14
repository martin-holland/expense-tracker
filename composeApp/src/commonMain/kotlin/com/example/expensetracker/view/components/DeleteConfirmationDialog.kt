package com.example.expensetracker.view.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.example.expensetracker.model.Expense
import com.example.theme.com.example.expensetracker.LocalAppColors

/**
 * Dialog component for confirming expense deletion
 * @param expense The expense to be deleted
 * @param onConfirm Callback when deletion is confirmed
 * @param onDismiss Callback when dialog is dismissed
 */
@Composable
fun DeleteConfirmationDialog(
    expense: Expense,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val appColors = LocalAppColors.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Expense?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"${expense.description}\" for ${expense.getFormattedAmount()}? This action cannot be undone.",
                style = MaterialTheme.typography.bodyMedium,
                color = appColors.mutedForeground
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = appColors.destructive,
                    contentColor = appColors.destructiveForeground
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = appColors.foreground)
            }
        },
        containerColor = appColors.card
    )
}

