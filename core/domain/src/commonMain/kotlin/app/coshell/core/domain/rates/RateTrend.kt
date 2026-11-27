package app.coshell.core.domain.rates

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/**
 * How a currency's rate moved over the last days (#195, board `Idea10A`): [values] oldest first — what a fixed amount of
 * the base currency was worth in it, on the same scale (only their ratio matters, for a sparkline); [changeBp] — the
 * change from the first to the last in basis points (−610 = −6,1 %), whole numbers: no floating point near money.
 */
data class RateTrend(val values: List<Long>, val changeBp: Int)

object RateTrends {
    private const val PROBE_MINOR = 100_000_000L

    /**
     * The trend of `1 base = X currency` over [days] days up to [today], one point every [step] days. `null` — no
     * rate at some point, or nothing known from before the start (a later rate would draw a made-up line).
     */
    fun of(
        book: RateBook,
        base: CurrencyCode,
        currency: CurrencyCode,
        today: LocalDate,
        days: Int = 30,
        step: Int = 2,
    ): RateTrend? {
        if (base == currency) return null
        val values = mutableListOf<Long>()
        for (offset in days downTo 0 step step) {
            val day = today.minus(offset, DateTimeUnit.DAY)
            val found = book.find(base, currency, day) ?: return null
            if (offset == days && found.date > day) return null
            values += found.rate.convert(Money(PROBE_MINOR, base), currency).amountMinor
        }
        val first = values.first()
        if (first <= 0L) return null
        return RateTrend(values, ((values.last() * 10_000) / first - 10_000).toInt())
    }
}
