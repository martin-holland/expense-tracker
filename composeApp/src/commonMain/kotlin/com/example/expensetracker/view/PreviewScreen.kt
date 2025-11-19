package com.example.expensetracker.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier

@Composable
expect fun PreviewScreen(
    modifier: Modifier,
    onTextGenerated: (String?) -> Unit
)