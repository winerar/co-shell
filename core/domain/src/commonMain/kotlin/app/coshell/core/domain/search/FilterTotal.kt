package app.coshell.core.domain.search

import app.coshell.core.domain.rates.CurrencyConverter
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * The total under the filters of «Операции» (4А, #160 → #141): how many operations matched and their net —
 * expenses minus, incomes plus, transfers left out (they move money, not spend it), as the day totals do.
 */
data class FilterTotal(
    val count: Int,
    /** Net per currency in the order the currencies first appear; zero nets are left out. */
    val nets: List<Money>,
    /** The net in the main report currency, each operation at the rate of its day; null without one. */
    val converted: CurrencyConverter.Total?,
)

/** [FilterTotal] of [transactions]; [main] — the first report currency (null — nothing converted). */
fun RateBook.filterTotal(transactions: List<Transaction>, main: CurrencyCode?, timeZone: TimeZone): FilterTotal {
    val nets = linkedMapOf<CurrencyCode, Long>()
    val signed = ArrayList<Pair<Money, kotlinx.datetime.LocalDate>>()
    for (t in transactions) {
        val money = when (t.type) {
            TransactionType.EXPENSE -> -t.amount
            TransactionType.INCOME -> t.amount
            TransactionType.TRANSFER -> continue
        }
        nets[money.currency] = (nets[money.currency] ?: 0L) + money.amountMinor
        signed += money to t.date.toLocalDateTime(timeZone).date
    }
    return FilterTotal(
        count = transactions.size,
        nets = nets.filterValues { it != 0L }.map { (currency, minor) -> Money(minor, currency) },
        converted = main?.takeIf { signed.isNotEmpty() }?.let { CurrencyConverter(this).total(signed, it) },
    )
}
