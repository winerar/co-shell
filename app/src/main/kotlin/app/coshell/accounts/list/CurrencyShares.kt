package app.coshell.accounts.list

/** Whole percents of [weights] that add up to exactly 100 (largest remainder); non-positive weights get 0. */
internal fun percentsOf(weights: List<Long>): List<Int> {
    val positive = weights.map { it.coerceAtLeast(0) }
    val total = positive.sum()
    if (total == 0L) return positive.map { 0 }
    val floors = positive.map { (it * 100 / total).toInt() }
    var left = 100 - floors.sum()
    val byRemainder = positive.indices.sortedByDescending { (positive[it] * 100) % total }
    val out = floors.toMutableList()
    for (i in byRemainder) {
        if (left == 0) break
        if (positive[i] > 0) {
            out[i]++
            left--
        }
    }
    return out
}

/**
 * Shares of the total balance by currency (#192, board `Idea5A`): [parts] are the positive balances already counted in
 * the main currency, biggest first. At most [MAX_SHARES] pieces — the tail becomes one «Другие» (`currency == null`).
 * Fewer than two currencies need no bar.
 */
internal fun currencyShares(parts: List<Pair<String, Long>>): List<AccountsUiState.Share> {
    val sorted = parts.filter { it.second > 0 }.sortedByDescending { it.second }
    if (sorted.size < 2) return emptyList()
    val shown = if (sorted.size > MAX_SHARES) {
        sorted.take(MAX_SHARES - 1).map { it.first to it.second } + (null to sorted.drop(MAX_SHARES - 1).sumOf { it.second })
    } else {
        sorted.map { it.first to it.second }
    }
    val percents = percentsOf(shown.map { it.second })
    return shown.mapIndexed { i, (currency, _) -> AccountsUiState.Share(currency, percents[i]) }.filter { it.percent > 0 }
}

internal const val MAX_SHARES = 5
