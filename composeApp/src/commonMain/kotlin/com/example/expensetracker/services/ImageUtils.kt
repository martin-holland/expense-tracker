package com.example.expensetracker.services

import androidx.compose.ui.graphics.ImageBitmap

expect fun decodeByteArrayToImageBitmap(bytes: ByteArray): ImageBitmap
