package app.coshell.core.domain.rates

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.sum
import kotlinx.datetime.LocalDate

/**
 * Sum of balances in one report currency.
 * @property missing currencies without any known rate — left out of [sum] ("без учёта: …").
 * @property rateDate the oldest rate date used, `null` when nothing had to be converted.
 */
data class BalanceTotal(val sum: Money, val missing: Set<CurrencyCode>, val rateDate: LocalDate?)

/**
 * Totals of [balances] in each of [currencies] at the rates in force on [date] (balances use
 * today's rate, ARCHITECTURE §6). Balances are summed per currency first and converted once, so
 * rounding happens once per currency, not per account.
 */
fun RateBook.balanceTotals(balances: List<Money>, currencies: List<CurrencyCode>, date: LocalDate): List<BalanceTotal> {
    val perCurrency = balances.groupBy { it.currency }.map { (currency, items) -> items.sum(currency) }
    return currencies.map { target ->
        var sum = Money.zero(target)
        var rateDate: LocalDate? = null
        val missing = linkedSetOf<CurrencyCode>()
        for (money in perCurrency) {
            if (money.currency == target) {
                sum += money
                continue
            }
            val found = find(money.currency, target, date)
            if (found == null) {
                missing += money.currency
                continue
            }
            sum += found.rate.convert(money, target)
            rateDate = rateDate?.let { minOf(it, found.date) } ?: found.date
        }
        BalanceTotal(sum, missing, rateDate)
    }
}
