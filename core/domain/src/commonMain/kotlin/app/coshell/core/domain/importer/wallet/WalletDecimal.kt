package app.coshell.core.domain.importer.wallet

import app.coshell.core.model.Currencies
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money

/**
 * An exact decimal number as Wallet writes it: `value = unscaled / 10^scale`.
 * Never a `Double`. Used for `ref_currency_amount`, whose currency the file does not name.
 */
data class WalletDecimal(val unscaled: Long, val scale: Int) {

    val isNegative: Boolean get() = unscaled < 0

    /** Same number without the sign. */
    fun abs(): WalletDecimal = if (unscaled < 0) WalletDecimal(-unscaled, scale) else this

    /** Equal value, ignoring how many fraction zeros were written ("10.0" == "10.00"). */
    fun sameValueAs(other: WalletDecimal): Boolean = normalized() == other.normalized()

    /**
     * Converts to [currency]'s minor units. Extra fraction digits are allowed only if they are
     * zeros (Wallet writes "-500000.00" even for 0-digit currencies); otherwise `null` —
     * never silent rounding.
     */
    fun toMoney(currency: CurrencyCode): Money? {
        val digits = Currencies.info(currency).fractionDigits
        var value = unscaled
        var s = scale
        while (s > digits) {
            if (value % 10 != 0L) return null
            value /= 10
            s--
        }
        while (s < digits) {
            if (value > Long.MAX_VALUE / 10 || value < Long.MIN_VALUE / 10) return null
            value *= 10
            s++
        }
        return Money(value, currency)
    }

    private fun normalized(): WalletDecimal {
        var value = unscaled
        var s = scale
        while (s > 0 && value % 10 == 0L) {
            value /= 10
            s--
        }
        return WalletDecimal(value, s)
    }

    companion object {
        private val PATTERN = Regex("""-?\d+(\.\d+)?""")
        private const val MAX_DIGITS = 18

        /** Parses "-13.00", "1500", "0.5" ('.' only, no grouping); `null` if not such a number. */
        fun parse(raw: String): WalletDecimal? {
            val text = raw.trim()
            if (!PATTERN.matches(text)) return null
            val negative = text.startsWith('-')
            val body = if (negative) text.substring(1) else text
            val point = body.indexOf('.')
            val digits = if (point < 0) body else body.removeRange(point, point + 1)
            val significant = digits.trimStart('0')
            if (significant.length > MAX_DIGITS) return null
            val value = significant.ifEmpty { "0" }.toLong()
            val scale = if (point < 0) 0 else body.length - point - 1
            return WalletDecimal(if (negative) -value else value, scale)
        }
    }
}
