package com.example.expensetracker.view.dashboard



/* ─────────────── MAP SORT EXTENSION ─────────────── */

fun <K, V> Map<K, V>.toSortedMap(
    comparator: Comparator<in K>
): Map<K, V> {
    return this.entries
        .sortedWith(compareBy(comparator) { it.key })
        .associate { it.toPair() }
}


