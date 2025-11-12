package com.example.expensetracker.view.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensetracker.model.Currency
import com.example.expensetracker.model.Expense
import com.example.theme.com.example.expensetracker.LocalAppColors
import kotlinx.coroutines.launch

/**
 * Swipeable expense item component
 * Swipe left to reveal edit button, swipe right to reveal delete button
 * Swipe far enough to trigger action directly
 * 
 * @param expense The expense to display
 * @param onEdit Callback when edit is triggered
 * @param onDelete Callback when delete is triggered
 * @param convertedAmount Optional converted amount in base currency
 * @param baseCurrency Optional base currency for conversion display
 * @param showConvertedAmount Whether to show the converted amount
 * @param modifier Modifier for layout customization
 */
@Composable
fun SwipeableExpenseItem(
    expense: Expense,
    onEdit: (Expense) -> Unit,
    onDelete: (Expense) -> Unit,
    convertedAmount: Double? = null,
    baseCurrency: Currency? = null,
    showConvertedAmount: Boolean = true,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    
    // Convert dp to pixels for proper swipe distance calculation
    val buttonWidthPx = with(density) { 140.dp.toPx() }
    
    // State for swipe offset
    val offsetX = remember { Animatable(0f) }
    val maxSwipeDistance = buttonWidthPx // Full reveal of button (in pixels)
    val triggerThreshold = buttonWidthPx * 1.6f // Swipe 1.6x button width to trigger directly
    
    // Track if item is being dragged
    var isDragging by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        // Background actions (Delete on right, Edit on left)
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Delete button (shown on right swipe)
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .fillMaxHeight()
                    .clickable {
                        scope.launch {
                            offsetX.animateTo(0f, animationSpec = tween(300))
                        }
                        onDelete(expense)
                    }
                    .background(appColors.destructive),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = appColors.destructiveForeground,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Delete",
                        style = MaterialTheme.typography.labelSmall,
                        color = appColors.destructiveForeground
                    )
                }
            }
            
            // Edit button (shown on left swipe)
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .fillMaxHeight()
                    .clickable {
                        scope.launch {
                            offsetX.animateTo(0f, animationSpec = tween(300))
                        }
                        onEdit(expense)
                    }
                    .background(Color(0xFFFF8C00)), // Orange color for edit
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = appColors.primaryForeground,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Edit",
                        style = MaterialTheme.typography.labelSmall,
                        color = appColors.primaryForeground
                    )
                }
            }
        }
        
        // Foreground expense card
        Card(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = offsetX.value
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            isDragging = true
                        },
                        onDragEnd = {
                            isDragging = false
                            scope.launch {
                                when {
                                    // Swiped left past trigger threshold - open edit directly
                                    offsetX.value < -triggerThreshold -> {
                                        offsetX.animateTo(0f, animationSpec = tween(300))
                                        onEdit(expense)
                                    }
                                    // Swiped left to reveal - stay revealed
                                    offsetX.value < -maxSwipeDistance / 3 -> {
                                        offsetX.animateTo(-maxSwipeDistance, animationSpec = tween(300))
                                    }
                                    // Swiped right past trigger threshold - trigger delete directly
                                    offsetX.value > triggerThreshold -> {
                                        offsetX.animateTo(0f, animationSpec = tween(300))
                                        onDelete(expense)
                                    }
                                    // Swiped right to reveal - stay revealed
                                    offsetX.value > maxSwipeDistance / 3 -> {
                                        offsetX.animateTo(maxSwipeDistance, animationSpec = tween(300))
                                    }
                                    // Not swiped enough - snap back
                                    else -> {
                                        offsetX.animateTo(0f, animationSpec = tween(300))
                                    }
                                }
                            }
                        },
                        onDragCancel = {
                            isDragging = false
                            scope.launch {
                                offsetX.animateTo(0f, animationSpec = tween(300))
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                // Allow swiping beyond max for trigger threshold
                                val newOffset = (offsetX.value + dragAmount)
                                    .coerceIn(-triggerThreshold, triggerThreshold)
                                offsetX.snapTo(newOffset)
                            }
                        }
                    )
                }
                .clickable(enabled = !isDragging) {
                    // Close revealed actions on tap
                    if (offsetX.value != 0f) {
                        scope.launch {
                            offsetX.animateTo(0f, animationSpec = tween(300))
                        }
                    }
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = appColors.card
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category icon and details
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Category icon with background
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(expense.category.backgroundColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = expense.category.icon,
                            contentDescription = expense.category.displayName,
                            tint = appColors.foreground,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Expense details
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = expense.category.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            color = appColors.foreground,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = expense.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = appColors.mutedForeground
                        )
                    }
                }
                
                // Amount with currency and optional conversion
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = expense.getFormattedAmount(),
                        style = MaterialTheme.typography.titleMedium,
                        color = appColors.foreground,
                        fontWeight = FontWeight.SemiBold
                    )
                    // Show converted amount if available and enabled
                    if (showConvertedAmount && convertedAmount != null && baseCurrency != null && expense.currency != baseCurrency) {
                        Text(
                            text = "≈ ${baseCurrency.format(convertedAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = appColors.mutedForeground
                        )
                    }
                }
            }
        }
    }
}
