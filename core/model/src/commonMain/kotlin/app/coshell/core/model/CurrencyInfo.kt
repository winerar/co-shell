package app.coshell.core.model

/**
 * Static facts about a currency needed to store and display amounts.
 *
 * @property fractionDigits number of minor-unit digits (JPY = 0, EUR = 2, KWD = 3).
 * @property symbol short display symbol, or `null` to display the code itself.
 */
data class CurrencyInfo(
    val code: CurrencyCode,
    val fractionDigits: Int,
    val symbol: String? = null,
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

    val displaySymbol: String get() = symbol ?: code.value

    companion object {
        /** Enough for ISO 4217 (max 4) and common crypto assets (BTC = 8). */
        const val MAX_FRACTION_DIGITS = 8
    }
}
