package app.coshell.core.domain.rates

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.core.model.isBalanceAdjustment
import kotlin.time.Instant
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
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
 * Balance adjustments ([isBalanceAdjustment]) are not spending and are left out.
 */
fun RateBook.expenseTotals(
    transactions: List<Transaction>,
    currencies: List<CurrencyCode>,
    timeZone: TimeZone,
): List<CurrencyConverter.Total> = typeTotals(transactions, TransactionType.EXPENSE, currencies, timeZone)

/** Incomes among [transactions] in each of [currencies], by the same rules as [expenseTotals]. */
fun RateBook.incomeTotals(
    transactions: List<Transaction>,
    currencies: List<CurrencyCode>,
    timeZone: TimeZone,
): List<CurrencyConverter.Total> = typeTotals(transactions, TransactionType.INCOME, currencies, timeZone)

private fun RateBook.typeTotals(
    transactions: List<Transaction>,
    type: TransactionType,
    currencies: List<CurrencyCode>,
    timeZone: TimeZone,
): List<CurrencyConverter.Total> {
    val items = transactions
        .filter { it.type == type && !it.isBalanceAdjustment }
        .map { it.amount to it.date.toLocalDateTime(timeZone).date }
    val converter = CurrencyConverter(this)
    return currencies.map { converter.total(items, it) }
}

/**
 * Expenses of each day of the month that contains [month], in [currency] (each at the rate of its day):
 * element 0 is the 1st, the list is as long as the month. Days without expenses are zero; expenses
 * without any known rate are left out (the month total lists them), adjustments too. For the mini bars on «Обзор».
 */
fun RateBook.dailyExpenses(
    transactions: List<Transaction>,
    currency: CurrencyCode,
    month: LocalDate,
    timeZone: TimeZone,
): List<Money> {
    val first = LocalDate(month.year, month.month, 1)
    val days = first.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1)).day
    val converter = CurrencyConverter(this)
    val byDay = MutableList(days) { Money.zero(currency) }
    for (t in transactions) {
        if (t.type != TransactionType.EXPENSE || t.isBalanceAdjustment) continue
        val date = t.date.toLocalDateTime(timeZone).date
        if (date.year != first.year || date.month != first.month) continue
        val converted = converter.convert(t.amount, currency, date) ?: continue
        byDay[date.day - 1] += converted.money
    }
    return byDay
}
