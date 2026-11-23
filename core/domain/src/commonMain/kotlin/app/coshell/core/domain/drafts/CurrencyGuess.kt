package app.coshell.core.domain.drafts

import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Currencies
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType

/**
 * The currency of a note that names none, by the order of its number (#635): «кофе 3500» is pesos, «кофе 5» is dollars. Each currency of the
 * person's live accounts has a typical size of an everyday expense — the median digit count of its past expenses (from [MIN_SAMPLES] of them),
 * else a rough [PRIOR] for well-known currencies; the currency whose typical size is nearest to the number's digit count wins. A tie, or a single
 * candidate, gives `null`: the caller keeps its usual choice. Whole numbers only (digit counts), no floating point.
 */
internal object CurrencyGuess {
    private const val MIN_SAMPLES = 3

    /** Digits of an everyday expense in whole units; rough on purpose — inflation moves a number, rarely a digit count. */
    private val PRIOR = mapOf(
        "USD" to 2, "EUR" to 2, "GBP" to 2, "CHF" to 2, "CAD" to 2, "AUD" to 2, "BRL" to 2, "USDT" to 2,
        "RUB" to 3, "UAH" to 3, "TRY" to 3, "CNY" to 2, "JPY" to 3,
        "ARS" to 4, "AMD" to 4, "CLP" to 4, "COP" to 4, "KZT" to 4, "UZS" to 5,
    )

    /** [typed] — the number as the person wrote it (whole units, `0` for «0,99»). */
    fun byMagnitude(typed: Long, accounts: List<Account>, history: List<Transaction>): CurrencyCode? {
        val currencies = accounts.filter { !it.archived }.map { it.currency }.distinct()
        if (currencies.size < 2) return null
        val digits = digits(typed)
        val centres = currencies.mapNotNull { code -> centre(code, history)?.let { code to it } }
        if (centres.size < 2) return null
        val distances = centres.map { (code, c) -> code to kotlin.math.abs(c - digits) }
        val best = distances.minOf { it.second }
        return distances.filter { it.second == best }.singleOrNull()?.first
    }

    /** Whole units of [amountMinor] in [code]: «350000» ARS → 3500. */
    fun wholeUnits(amountMinor: Long, code: CurrencyCode): Long {
        var divisor = 1L
        repeat(Currencies.info(code).fractionDigits) { divisor *= 10 }
        return amountMinor / divisor
    }

    private fun centre(code: CurrencyCode, history: List<Transaction>): Int? {
        val sizes = history.filter { it.type == TransactionType.EXPENSE && it.amount.currency == code }
            .map { digits(wholeUnits(it.amount.amountMinor, code)) }.sorted()
        return if (sizes.size >= MIN_SAMPLES) sizes[(sizes.size - 1) / 2] else PRIOR[code.value]
    }

    private fun digits(whole: Long): Int {
        var n = kotlin.math.abs(whole)
        var count = 1
        while (n >= 10) { n /= 10; count++ }
        return count
    }
}
