package com.example.expensetracker.view

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.theme.com.example.expensetracker.LocalAppColors

@Composable
fun DashboardScreen(){

    val appColors = LocalAppColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.background)
            .statusBarsPadding() // Add padding for system status bar
    ){
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Header()
            Text("This is overview")
            Text("This is pie chart")
            Text("This is graph chart")
        }
    }
}

@Composable
private fun Header(){
    val appColors = LocalAppColors.current
    val headerPadding by animateDpAsState(
        targetValue =  16.dp
    )
    val titleSize by animateFloatAsState(
        targetValue = 1f
    )
    Surface (
        modifier = Modifier.fillMaxWidth(),
        color = appColors.background,
        shadowElevation = 0.dp
    ){
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = headerPadding, bottom = headerPadding)
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ){
                Box(
                    modifier = Modifier
                        .size(48.dp * titleSize)
                        .clip(CircleShape)
                        .background(Color(0xFF00BCD4)), // Teal color
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = "App Icon",
                        tint = appColors.primaryForeground,
                        modifier = Modifier.size(24.dp * titleSize)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Expense Dashboard",
                        style =
                            MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = appColors.foreground
                    )

                    Text(
                        text = "Expense Tracker",
                        style = MaterialTheme.typography.bodySmall,
                        color = appColors.mutedForeground
                    )

                }
            }
        }

    }
}