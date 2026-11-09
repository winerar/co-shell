package app.coshell.core.domain.rates

import app.coshell.core.model.CurrencyCode
import kotlinx.datetime.LocalDate

/** Rate of one currency against the base for the "Rates" screen; [found] is `null` when nothing is known. */
data class CurrentRate(val currency: CurrencyCode, val found: FoundRate?)

/**
 * Rates `1 base = X currency` in force on [date] for every currency of [currencies] except [base],
 * sorted by code. Uses the usual [RateBook] rules, so a manual rate wins over downloaded ones.
 */
fun RateBook.currentRates(currencies: Set<CurrencyCode>, base: CurrencyCode, date: LocalDate): List<CurrentRate> =
    currencies.filter { it != base }
        .sortedBy { it.value }
        .map { CurrentRate(it, find(base, it, date)) }
