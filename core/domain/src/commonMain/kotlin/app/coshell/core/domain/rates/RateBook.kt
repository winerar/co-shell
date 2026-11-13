package app.coshell.core.domain.rates

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import kotlin.concurrent.Volatile
import kotlinx.datetime.LocalDate

/** A rate picked for a conversion, with where it came from (for "≈" hints in the UI). */
data class FoundRate(
    /** `1 from = rate to`. */
    val rate: Rate,
    /** Date of the (oldest) rate used; may differ from the requested date. */
    val date: LocalDate,
    /** [RateOrigin.MANUAL] if any leg was typed by a person. */
    val origin: RateOrigin,
    /** True when computed through [RateBook.pivot] (cross rate). */
    val viaPivot: Boolean,
)

/**
 * In-memory snapshot of all known rates with the lookup rules (ARCHITECTURE §6):
 * 1. a **manual** rate of the pair on or before the date — it stays in force until the next
 *    manual rate (so a hand-set rate, e.g. "blue" ARS, is not overwritten by downloads);
 * 2. a downloaded rate on the date, else the nearest earlier date;
 * 3. a cross rate through [pivot] (USD) with the same rules for both legs;
 * 4. only if nothing earlier exists: the nearest later rate (direct, then cross; a cross leg that has an
 *    earlier rate keeps it, only the leg without one takes the later rate).
 * Each stored rate also serves the opposite direction via [Rate.inverse].
 */
class RateBook(rates: List<ExchangeRate>, val pivot: CurrencyCode = CurrencyCode("USD")) {

    companion object {
        @Volatile private var last: Pair<List<ExchangeRate>, RateBook>? = null

        /**
         * The book of [rates], reused while screens get the very same list (the shared rates flow, #260):
         * «Обзор», «Счета», «Бюджеты» and the entry form then index thousands of rates once, not each.
         */
        fun of(rates: List<ExchangeRate>): RateBook {
            last?.let { (list, book) -> if (list === rates) return book }
            return RateBook(rates).also { last = rates to it }
        }

        private fun key(a: CurrencyCode, b: CurrencyCode) = if (a.value <= b.value) a to b else b to a
    }

    private val byPair: Map<Pair<CurrencyCode, CurrencyCode>, PairRates> =
        rates.groupBy { key(it.from, it.to) }.mapValues { (_, list) -> PairRates(list) }

    fun find(from: CurrencyCode, to: CurrencyCode, date: LocalDate): FoundRate? {
        if (from == to) return FoundRate(Rate.ONE, date, RateOrigin.DOWNLOADED, viaPivot = false)
        return direct(from, to, date, allowLater = false)
            ?: cross(from, to, date, allowLater = false)
            ?: direct(from, to, date, allowLater = true)
            ?: cross(from, to, date, allowLater = true)
    }

    private fun direct(from: CurrencyCode, to: CurrencyCode, date: LocalDate, allowLater: Boolean): FoundRate? {
        val pair = byPair[key(from, to)] ?: return null
        val entry = (if (allowLater) pair.earliestAfter(date) else pair.onOrBefore(date)) ?: return null
        val rate = if (entry.from == from) entry.rate else entry.rate.inverse()
        return FoundRate(rate, entry.date, entry.origin, viaPivot = false)
    }

    private fun cross(from: CurrencyCode, to: CurrencyCode, date: LocalDate, allowLater: Boolean): FoundRate? {
        if (from == pivot || to == pivot) return null
        val first = leg(from, pivot, date, allowLater) ?: return null
        val second = leg(pivot, to, date, allowLater) ?: return null
        return FoundRate(
            rate = first.rate * second.rate,
            date = minOf(first.date, second.date),
            origin = if (RateOrigin.MANUAL in listOf(first.origin, second.origin)) RateOrigin.MANUAL else RateOrigin.DOWNLOADED,
            viaPivot = true,
        )
    }

    /** One leg of a cross rate: an earlier rate if there is one; a later one only when [allowLater] (each leg on its own). */
    private fun leg(from: CurrencyCode, to: CurrencyCode, date: LocalDate, allowLater: Boolean): FoundRate? =
        direct(from, to, date, allowLater = false) ?: if (allowLater) direct(from, to, date, allowLater = true) else null

    /** Rates of one unordered pair, sorted by date; lookups are binary searches. */
    private class PairRates(all: List<ExchangeRate>) {
        private val manual = all.filter { it.origin == RateOrigin.MANUAL }.sortedBy { it.date }
        private val downloaded = all.filter { it.origin == RateOrigin.DOWNLOADED }.sortedBy { it.date }

        fun onOrBefore(date: LocalDate): ExchangeRate? =
            manual.getOrNull(lastOnOrBefore(manual, date)) ?: downloaded.getOrNull(lastOnOrBefore(downloaded, date))

        /** Nearest later rate; a manual one wins on the same date. */
        fun earliestAfter(date: LocalDate): ExchangeRate? {
            val m = manual.getOrNull(lastOnOrBefore(manual, date) + 1)
            val d = downloaded.getOrNull(lastOnOrBefore(downloaded, date) + 1)
            return when {
                m == null -> d
                d == null || m.date <= d.date -> m
                else -> d
            }
        }

        /** Index of the last rate dated on or before [date], or -1. */
        private fun lastOnOrBefore(list: List<ExchangeRate>, date: LocalDate): Int {
            var low = 0
            var high = list.size - 1
            var found = -1
            while (low <= high) {
                val mid = (low + high) ushr 1
                if (list[mid].date <= date) { found = mid; low = mid + 1 } else high = mid - 1
            }
            return found
        }
    }
}
