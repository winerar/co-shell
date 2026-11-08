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

    private val symbols: Map<String, String> = mapOf(
        "RUB" to "₽", "USD" to "$", "EUR" to "€", "GBP" to "£", "JPY" to "¥", "CNY" to "¥",
        "KZT" to "₸", "UAH" to "₴", "BYN" to "Br", "GEL" to "₾", "AMD" to "֏", "AZN" to "₼",
        "TRY" to "₺", "THB" to "฿", "INR" to "₹", "KRW" to "₩", "ILS" to "₪", "VND" to "₫",
        "PLN" to "zł", "CHF" to "Fr", "RSD" to "дин", "KGS" to "с", "UZS" to "сўм",
    )

    /** Custom (non-ISO) assets the app knows about out of the box. */
    private val customAssets: Map<String, CurrencyInfo> = listOf(
        CurrencyInfo(CurrencyCode("USDT"), fractionDigits = 2, symbol = "₮"),
        CurrencyInfo(CurrencyCode("BTC"), fractionDigits = 8, symbol = "₿"),
    ).associateBy { it.code.value }

    fun info(code: CurrencyCode): CurrencyInfo =
        customAssets[code.value] ?: CurrencyInfo(
            code = code,
            fractionDigits = fractionDigitsOverrides[code.value] ?: DEFAULT_FRACTION_DIGITS,
            symbol = symbols[code.value],
        )

    fun info(code: String): CurrencyInfo = info(CurrencyCode(code))
}
