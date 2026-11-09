package app.coshell.ui.format

import app.coshell.core.model.Rate

/**
 * Ru-style rate text: "1 049,69", "0,0067"; 2 fraction digits from 100, otherwise 4.
 * Used by the receipt hint of the transaction editor and the "Rates" screen.
 */
fun formatRate(rate: Rate): String {
    val plain = rate.rounded(if (rate >= Rate.parse("100")) 2 else 4).toPlainString()
    val integer = plain.substringBefore('.')
    val fraction = plain.substringAfter('.', "")
    val grouped = integer.reversed().chunked(3).joinToString(" ").reversed()
    return if (fraction.isEmpty()) grouped else "$grouped,$fraction"
}
