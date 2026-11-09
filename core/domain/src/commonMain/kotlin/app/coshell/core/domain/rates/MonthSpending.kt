package app.coshell.core.domain.rates

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlin.time.Instant
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/** Start (inclusive) and end (exclusive) of the calendar month that contains [day], in [timeZone]. */
fun monthBounds(day: LocalDate, timeZone: TimeZone): Pair<Instant, Instant> {
    val first = LocalDate(day.year, day.month, 1)
    return first.atStartOfDayIn(timeZone) to first.plus(DatePeriod(months = 1)).atStartOfDayIn(timeZone)
}

/**
 * Expenses among [transactions] summed in each of [currencies]. Each expense is converted at the
 * rate of its own day (ARCHITECTURE §6: expenses — at the operation date), so the sum does not
 * change when rates move later. The account amount (what was actually debited) is counted, not
 * the receipt amount. Expenses without any known rate are left out and listed in `missing`.
 */
fun RateBook.expenseTotals(
    transactions: List<Transaction>,
    currencies: List<CurrencyCode>,
    timeZone: TimeZone,
): List<CurrencyConverter.Total> {
    val expenses = transactions
        .filter { it.type == TransactionType.EXPENSE }
        .map { it.amount to it.date.toLocalDateTime(timeZone).date }
    val converter = CurrencyConverter(this)
    return currencies.map { converter.total(expenses, it) }
}
