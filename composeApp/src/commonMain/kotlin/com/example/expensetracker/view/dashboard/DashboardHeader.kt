package com.example.expensetracker.view.dashboard


import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardHeader() {
    val animPad by animateDpAsState(targetValue = 16.dp)
    val animScale by animateFloatAsState(targetValue = 1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(animPad),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp * animScale)
                .clip(CircleShape)
                .background(Color(0xFF00BCD4)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = null,
                tint = Color.White
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = "Expense Dashboard",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Expense Tracker",
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
    }
}
