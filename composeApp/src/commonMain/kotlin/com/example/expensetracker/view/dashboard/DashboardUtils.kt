package com.example.expensetracker.view.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.pow
import kotlin.math.round

/* ─────────────── MAP SORT EXTENSION ─────────────── */

fun <K, V> Map<K, V>.toSortedMap(
    comparator: Comparator<in K>
): Map<K, V> {
    return this.entries
        .sortedWith(compareBy(comparator) { it.key })
        .associate { it.toPair() }
}

/* ─────────────── DOUBLE FORMAT ─────────────── */

fun Double.format(n: Int): String {
    val f = 10.0.pow(n)
    return (round(this * f) / f).toString()
}

/* ─────────────── TREND PLACEHOLDER ─────────────── */

@Composable
fun TrendPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Trend Chart (Coming Soon)", color = Color.Gray)
    }
}
