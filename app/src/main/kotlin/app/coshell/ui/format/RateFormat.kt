package app.coshell.ui.format

import app.coshell.core.model.Rate

/**
 * Ru-style rate text: "1 049,69", "1,0846", "0,0007", "0,000009876".
 * 2 fraction digits from 100, 4 from 1; below 1 — 4 significant digits, so a tiny rate
 * (1 USD = 0,00001 BTC) is never rounded to zero. Used by the receipt hint and the "Rates" screen.
 */
fun formatRate(rate: Rate): String {
    val plain = rate.rounded(displayScale(rate)).toPlainString()
    val integer = plain.substringBefore('.')
    val fraction = plain.substringAfter('.', "")
    val grouped = integer.reversed().chunked(3).joinToString(" ").reversed()
    return if (fraction.isEmpty()) grouped else "$grouped,$fraction"
}

/** Fraction digits to show: 2 from 100, 4 from 1, else enough for 4 significant digits. */
private fun displayScale(rate: Rate): Int = when {
    rate >= HUNDRED -> 2
    rate >= Rate.ONE -> 4
    else -> {
        val zeros = rate.toPlainString().substringAfter('.').takeWhile { it == '0' }.length
        minOf(zeros + SIGNIFICANT_BELOW_ONE, Rate.MAX_SCALE)
    }
}

private val HUNDRED = Rate.parse("100")
private const val SIGNIFICANT_BELOW_ONE = 4
