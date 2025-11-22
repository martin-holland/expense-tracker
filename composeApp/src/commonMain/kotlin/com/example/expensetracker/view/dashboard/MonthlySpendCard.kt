package com.example.expensetracker.view.dashboard


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.model.Currency

/* ─────────────── MONTHLY CARD ─────────────── */

@Composable
fun MonthlySpendCard(
    spend: Double,
    month: String,
    currency: Currency
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF00BFAE))
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("Total Monthly Spend", color = Color.White)
            Text(
                text = "${currency.symbol}${spend.formatAmount(2)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(month, color = Color.White)
        }
    }
}

/* ─────────────── CHART SELECTOR ROW ─────────────── */

@Composable
fun ChartSelectionRow(
    selected: DashboardChartType,
    onSelect: (DashboardChartType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        val items = listOf(
            DashboardChartType.Category to "Category",
            DashboardChartType.Weekly to "Weekly",
            DashboardChartType.Trend to "Trends"
        )

        items.forEach { (type, label) ->
            Button(
                onClick = { onSelect(type) },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selected == type) Color(0xFF00BFAE) else Color(0xFFF0F7F6),
                    contentColor = if (selected == type) Color.White else Color.Black
                )
            ) {
                Text(label)
            }
        }
    }
}
