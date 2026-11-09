package app.coshell.core.model

/**
 * Built-in currency table. Pure Kotlin on purpose (no `java.util.Currency`) so it works on every
 * Kotlin Multiplatform target.
 *
 * ISO 4217 currencies not listed in [fractionDigitsOverrides] use 2 fraction digits.
 */
object Currencies {

    private const val DEFAULT_FRACTION_DIGITS = 2

    /** ISO 4217 currencies whose minor unit is not 2 digits. */
    private val fractionDigitsOverrides: Map<String, Int> = buildMap {
        listOf(
            "BIF", "CLP", "DJF", "GNF", "ISK", "JPY", "KMF", "KRW", "PYG",
            "RWF", "UGX", "UYI", "VND", "VUV", "XAF", "XOF", "XPF",
        ).forEach { put(it, 0) }
        listOf("BHD", "IQD", "JOD", "KWD", "LYD", "OMR", "TND").forEach { put(it, 3) }
        listOf("CLF", "UYW").forEach { put(it, 4) }
    }

    /** Custom (non-ISO) assets the app knows about out of the box. */
    private val customAssets: Map<String, CurrencyInfo> = listOf(
        CurrencyInfo(CurrencyCode("USDT"), fractionDigits = 2),
        CurrencyInfo(CurrencyCode("BTC"), fractionDigits = 8),
    ).associateBy { it.code.value }

    fun info(code: CurrencyCode): CurrencyInfo =
        customAssets[code.value] ?: CurrencyInfo(
            code = code,
            fractionDigits = fractionDigitsOverrides[code.value] ?: DEFAULT_FRACTION_DIGITS,
        )

    fun info(code: String): CurrencyInfo = info(CurrencyCode(code))
}
