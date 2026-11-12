package app.coshell.core.domain.reports

/**
 * Whole percents of [parts] in their sum that add up to exactly 100 (largest remainder): 33 + 33 + 34,
 * not 33 + 33 + 33. Non-positive parts get 0 and are not in the sum; all zero — all 0.
 */
fun percentShares(parts: List<Long>): List<Int> {
    var total = parts.filter { it > 0 }.sum()
    if (total == 0L) return parts.map { 0 }
    // Keep amount × 100 inside Long; percents of amounts this large lose nothing that shows.
    var divisor = 1L
    while (total > Long.MAX_VALUE / 100) { divisor *= 1000; total = parts.filter { it > 0 }.sumOf { it / divisor } }
    val scaled = parts.map { if (it > 0) it / divisor else 0L }
    if (total == 0L) return parts.map { 0 }
    val floors = scaled.map { (it * 100 / total).toInt() }
    val remainders = scaled.map { it * 100 % total }
    var left = 100 - floors.sum()
    val result = floors.toMutableList()
    for (i in scaled.indices.sortedByDescending { remainders[it] }) {
        if (left == 0) break
        if (scaled[i] <= 0) continue
        result[i]++
        left--
    }
    return result
}
