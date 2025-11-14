package com.example.expensetracker.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Currency Exchange Banner Component
 * 
 * A reusable banner component for the currency exchange feature.
 * Features a gradient background, exchange icon, text section, and "Open" button.
 * 
 * @param onOpenClick Callback when "Open" button is clicked
 * @param modifier Modifier for layout customization
 */
@Composable
fun CurrencyExchangeBanner(
    onOpenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Gradient colors: Light blue-green (left) to medium blue (right)
    val gradientColors = listOf(
        Color(0xFF4FC3F7), // Light blue-green
        Color(0xFF29B6F6), // Medium blue
        Color(0xFF0288D1)  // Medium blue (darker)
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    colors = gradientColors
                )
            )
            .clickable { onOpenClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left section: Icon and Text
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Exchange icon with circular background
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.SwapHoriz,
                        contentDescription = "Currency Exchange",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Text section
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Currency Exchange",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Convert & track rates",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            
            // Right section: "Open" button
            Button(
                onClick = onOpenClick,
                modifier = Modifier.padding(start = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF0288D1) // Blue text
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Open",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

