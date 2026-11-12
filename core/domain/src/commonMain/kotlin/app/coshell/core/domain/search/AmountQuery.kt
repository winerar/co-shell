package app.coshell.core.domain.search

import app.coshell.core.model.Currencies
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction

/**
 * A search by amount on «Операции» (#141): `=4500`, `>10000`, `<500`, `>=`, `<=` or a range `3000-5000`
 * (inclusive). Spaces inside numbers and a decimal comma are fine (`=41,20`, `3 000-5 000`).
 *
 * Amounts are in major units of **any** currency: an operation matches when its account amount, its
 * receipt amount or the arriving amount of a transfer fits. Bounds are kept in hundredths, so the
 * comparison is exact for every currency (scaled by powers of ten, no `Double`).
 */
data class AmountQuery(
    /** Lowest amount in hundredths of a major unit; null — no lower bound. */
    val minHundredths: Long?,
    /** Highest amount in hundredths of a major unit; null — no upper bound. */
    val maxHundredths: Long?,
    val minInclusive: Boolean = true,
    val maxInclusive: Boolean = true,
) {
    fun matches(t: Transaction): Boolean =
        fits(t.amount) || t.originalAmount?.let(::fits) == true || t.toAmount?.let(::fits) == true

    fun fits(money: Money): Boolean {
        val minorPerMajor = Currencies.info(money.currency).minorPerMajor
        val amount = money.amountMinor.let { if (it < 0) -it else it }
        // Both sides in the finer unit: minorPerMajor is a power of ten, so the scaling is exact.
        val (value, boundScale) = if (minorPerMajor >= HUNDRED) amount to minorPerMajor / HUNDRED else amount * (HUNDRED / minorPerMajor) to 1L
        val lowOk = minHundredths?.let { low -> if (minInclusive) value >= low * boundScale else value > low * boundScale } ?: true
        val highOk = maxHundredths?.let { high -> if (maxInclusive) value <= high * boundScale else value < high * boundScale } ?: true
        return lowOk && highOk
    }

    /** How the query was understood, for the hint under the search field. */
    val kind: Kind
        get() = when {
            minHundredths != null && minHundredths == maxHundredths -> Kind.EXACT
            minHundredths != null && maxHundredths != null -> Kind.RANGE
            minHundredths != null -> if (minInclusive) Kind.AT_LEAST else Kind.MORE
            else -> if (maxInclusive) Kind.AT_MOST else Kind.LESS
        }

    enum class Kind { EXACT, RANGE, MORE, AT_LEAST, LESS, AT_MOST }

    companion object {
        private const val HUNDRED = 100L

        /** A number: digits with optional spaces between groups and up to two decimals after `,` or `.`. */
        private const val NUMBER = """(\d[\d\s]*(?:[.,]\d{1,2})?)"""
        private val SINGLE = Regex("""^(=|>=|<=|>|<)\s*$NUMBER$""")
        private val RANGE = Regex("""^$NUMBER\s*[-–—]\s*$NUMBER$""")

        /** The query as an amount search, or null when it is ordinary text (a plain number stays text). */
        fun parse(raw: String): AmountQuery? {
            val text = raw.trim()
            SINGLE.matchEntire(text)?.let { m ->
                val value = hundredths(m.groupValues[2]) ?: return null
                return when (m.groupValues[1]) {
                    "=" -> AmountQuery(value, value)
                    ">" -> AmountQuery(value, null, minInclusive = false)
                    ">=" -> AmountQuery(value, null)
                    "<" -> AmountQuery(null, value, maxInclusive = false)
                    else -> AmountQuery(null, value)
                }
            }
            RANGE.matchEntire(text)?.let { m ->
                val a = hundredths(m.groupValues[1]) ?: return null
                val b = hundredths(m.groupValues[2]) ?: return null
                return AmountQuery(minOf(a, b), maxOf(a, b))
            }
            return null
        }

        private fun hundredths(number: String): Long? {
            val clean = number.filterNot { it.isWhitespace() }.replace(',', '.')
            val whole = clean.substringBefore('.')
            val fraction = clean.substringAfter('.', "").padEnd(2, '0')
            // 12 digits × 10^6 (8 fraction digits less the hundredths) still fits in Long.
            if (whole.isEmpty() || whole.length > MAX_DIGITS) return null
            return whole.toLong() * HUNDRED + fraction.toLong()
        }

        private const val MAX_DIGITS = 12
    }
}
