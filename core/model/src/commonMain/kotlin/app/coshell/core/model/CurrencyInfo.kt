package app.coshell.core.model

/**
 * Static facts about a currency needed to store and display amounts.
 *
 * Amounts are always displayed with the letter code («1 234,50 USD», owner's choice, issue #61),
 * so there is no currency symbol here.
 *
 * @property fractionDigits number of minor-unit digits (JPY = 0, EUR = 2, KWD = 3).
 */
data class CurrencyInfo(
    val code: CurrencyCode,
    val fractionDigits: Int,
) {
    init {
        require(fractionDigits in 0..MAX_FRACTION_DIGITS) {
            "fractionDigits must be in 0..$MAX_FRACTION_DIGITS, was $fractionDigits"
        }
    }

    /** 10^fractionDigits: how many minor units make one major unit. */
    val minorPerMajor: Long
        get() {
            var result = 1L
            repeat(fractionDigits) { result *= 10 }
            return result
        }

    companion object {
        /** Enough for ISO 4217 (max 4) and common crypto assets (BTC = 8). */
        const val MAX_FRACTION_DIGITS = 8
    }
}
