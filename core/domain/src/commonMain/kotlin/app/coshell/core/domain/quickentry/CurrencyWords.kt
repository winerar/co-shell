package app.coshell.core.domain.quickentry

import app.coshell.core.model.CurrencyCode

/**
 * Currencies named by a word next to the amount (#631): «кофе 3500 песо», «15 долларов», «200 рублей», «5000 драм», «20 euros».
 * Russian words are stems (any ending: «доллар», «доллара», «долларов»); short or ambiguous ones must be the whole word.
 */
internal object CurrencyWords {
    private val EXACT = mapOf(
        "песо" to "ARS", "pesos" to "ARS", "peso" to "ARS", "арс" to "ARS",
        "бакс" to "USD", "баксов" to "USD", "бакса" to "USD", "dolar" to "USD", "dolares" to "USD", "dólar" to "USD", "dólares" to "USD",
        "usd" to "USD", "долл" to "USD", "дол" to "USD",
        "евро" to "EUR", "euro" to "EUR", "euros" to "EUR",
        "руб" to "RUB", "rub" to "RUB",
        "тенге" to "KZT", "tenge" to "KZT", "лари" to "GEL", "lari" to "GEL", "грн" to "UAH",
        "лир" to "TRY", "лира" to "TRY", "лиры" to "TRY", "лиру" to "TRY", "reales" to "BRL", "реалов" to "BRL", "реала" to "BRL",
        "юсдт" to "USDT", "тезер" to "USDT", "сум" to "UZS", "сумов" to "UZS",
    )
    private val STEMS = listOf(
        "доллар" to "USD", "dollar" to "USD", "рубл" to "RUB", "драм" to "AMD", "гривн" to "UAH",
    )

    /** The currency [raw] names, `null` when it is not a currency word. A trailing dot is ignored («руб.»). */
    fun of(raw: String): CurrencyCode? {
        val word = raw.lowercase().trimEnd('.').replace('ё', 'е')
        val code = EXACT[word] ?: STEMS.firstOrNull { (stem, _) -> word.startsWith(stem) }?.second ?: return null
        return CurrencyCode(code)
    }
}
