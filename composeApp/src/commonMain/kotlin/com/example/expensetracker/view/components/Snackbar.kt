package com.example.expensetracker.view.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.theme.com.example.expensetracker.LocalAppColors
import kotlinx.coroutines.delay

/**
 * Types of snackbar messages
 */
enum class SnackbarType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO
}

/**
 * Data class representing a snackbar message
 * 
 * @param message The message text to display
 * @param type The type of message (determines styling and icon)
 * @param duration How long to show the message in milliseconds (default 3000ms)
 */
data class SnackbarMessage(
    val message: String,
    val type: SnackbarType = SnackbarType.INFO,
    val duration: Long = 3000L
)

/**
 * Reusable Snackbar Host component
 * Displays animated snackbar messages at the top of the screen
 * 
 * Usage:
 * ```kotlin
 * var snackbarMessage by remember { mutableStateOf<SnackbarMessage?>(null) }
 * 
 * SnackbarHost(
 *     message = snackbarMessage,
 *     onDismiss = { snackbarMessage = null }
 * )
 * 
 * // To show a message:
 * snackbarMessage = SnackbarMessage("Success!", SnackbarType.SUCCESS)
 * ```
 * 
 * @param message The current message to display (null to hide)
 * @param onDismiss Callback when the snackbar is dismissed
 * @param modifier Optional modifier for the host container
 */
@Composable
fun SnackbarHost(
    message: SnackbarMessage?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Auto-dismiss after duration
    LaunchedEffect(message) {
        if (message != null) {
            delay(message.duration)
            onDismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = message != null,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh
                )
            ) + fadeIn(
                animationSpec = tween(durationMillis = 200)
            ),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(durationMillis = 200)
            ) + fadeOut(
                animationSpec = tween(durationMillis = 200)
            )
        ) {
            message?.let {
                SnackbarContent(message = it)
            }
        }
    }
}

/**
 * Internal composable for rendering the snackbar content
 */
@Composable
private fun SnackbarContent(message: SnackbarMessage) {
    val appColors = LocalAppColors.current
    
    val (backgroundColor, contentColor, icon) = when (message.type) {
        SnackbarType.SUCCESS -> Triple(
            Color(0xFF10B981), // Green
            Color.White,
            Icons.Filled.CheckCircle
        )
        SnackbarType.ERROR -> Triple(
            Color(0xFFEF4444), // Red
            Color.White,
            Icons.Filled.Error
        )
        SnackbarType.WARNING -> Triple(
            Color(0xFFF59E0B), // Amber
            Color.White,
            Icons.Filled.Warning
        )
        SnackbarType.INFO -> Triple(
            appColors.primary,
            appColors.primaryForeground,
            Icons.Filled.Info
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = contentColor
            )
            
            Text(
                text = message.message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

