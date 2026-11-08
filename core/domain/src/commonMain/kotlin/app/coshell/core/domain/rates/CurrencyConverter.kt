package app.coshell.core.domain.rates

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlinx.datetime.LocalDate

/** A converted amount plus the rate behind it. */
data class Conversion(val money: Money, val rate: FoundRate)

/**
 * Converts amounts for display. Facts are never rewritten: callers convert on the fly
 * (expenses/incomes — at the operation date, balances — at today's date).
 */
class CurrencyConverter(private val book: RateBook) {

    /** `null` when no rate for the pair is known at all (UI shows the original amount). */
    fun convert(money: Money, to: CurrencyCode, date: LocalDate): Conversion? {
        val found = book.find(money.currency, to, date) ?: return null
        return Conversion(found.rate.convert(money, to), found)
    }

    /**
     * Total of [amounts] in [to]; amounts without a known rate are skipped and listed in
     * [Total.missing], so the UI can say "без учёта: …" instead of showing a wrong sum.
     */
    fun total(amounts: List<Pair<Money, LocalDate>>, to: CurrencyCode): Total {
        var sum = Money.zero(to)
        val missing = linkedSetOf<CurrencyCode>()
        for ((money, date) in amounts) {
            val converted = convert(money, to, date)
            if (converted == null) missing += money.currency else sum += converted.money
        }
        return Total(sum, missing)
    }

    data class Total(val sum: Money, val missing: Set<CurrencyCode>)
}
