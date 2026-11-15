package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetPeriod
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/** How one account's balance moved in one day, minor units of its currency (transfers in and out included). */
data class BalanceDay(val day: LocalDate, val accountId: String, val deltaMinor: Long)

/** Capital at the end of a month ([asOf] — its last day, or today while it runs), in the report currency. */
data class NetWorthPoint(val period: BudgetPeriod, val asOf: LocalDate, val amount: Money, val running: Boolean)

/**
 * Why capital changed in a month (#189, board `Rp189NwB`): [saved] — income minus spending of the month; [other] — the
 * rest: exchange rates moving the balances, balance adjustments, exchange spreads.
 */
data class NetWorthChange(val period: BudgetPeriod, val saved: Money, val other: Money, val running: Boolean) {
    val total: Money get() = saved + other
}

/**
 * «Капитал» (#189): the money on all accounts counted into the total (as «Обзор» counts it) at each month end, and why
 * it changed in the last months. [missing] — currencies without a rate on some point: those accounts are left out there.
 */
data class NetWorth(val points: List<NetWorthPoint>, val changes: List<NetWorthChange>, val missing: Set<CurrencyCode>) {
    val now: Money get() = points.last().amount

    /** From the first point to now; [percent] — of the first point, `null` when it was not positive. */
    val change: Money get() = now - points.first().amount
    val percent: Int? get() = percentOf(change, points.first().amount)

    /** Change in the last month and the last three. */
    fun changeOver(months: Int): Money = now - points[(points.lastIndex - months).coerceAtLeast(0)].amount

    /** The highest point is the current one. */
    val atMax: Boolean get() = points.all { it.amount <= now }
}

/**
 * Capital at the end of each of [periods] that has started by [today], each account at the rate of that day.
 * [saved] — `MonthTotals.saved` by period (the months [NetWorth.changes] can explain; at most [WHY_MONTHS], newest
 * last). `null` — no started period or no account in the total.
 */
fun netWorth(
    accounts: List<Account>,
    days: List<BalanceDay>,
    periods: List<BudgetPeriod>,
    book: RateBook,
    currency: CurrencyCode,
    today: LocalDate,
    saved: Map<BudgetPeriod, Money> = emptyMap(),
): NetWorth? {
    val counted = accounts.filter { !it.excludedFromTotal }
    val started = periods.filter { it.start <= today }
    if (counted.isEmpty() || started.isEmpty()) return null
    val byAccount = days.groupBy { it.accountId }
    val missing = linkedSetOf<CurrencyCode>()
    val points = started.map { period ->
        val running = today < period.endExclusive
        val asOf = if (running) today else period.endExclusive.minus(1, DateTimeUnit.DAY)
        var total = Money.zero(currency)
        for (account in counted) {
            val balance = balanceAt(account, byAccount[account.id].orEmpty(), asOf)
            if (balance.amountMinor == 0L) continue
            val rate = book.find(account.currency, currency, asOf)?.rate
            if (rate == null) { missing += account.currency; continue }
            total += rate.convert(balance, currency)
        }
        NetWorthPoint(period, asOf, total, running)
    }
    val changes = (1..points.lastIndex).mapNotNull { i ->
        val point = points[i]
        val s = saved[point.period] ?: return@mapNotNull null
        NetWorthChange(point.period, s, point.amount - points[i - 1].amount - s, point.running)
    }.takeLast(WHY_MONTHS)
    return NetWorth(points, changes, missing)
}

/** Months of capital on the chart: a year. */
const val NET_WORTH_MONTHS = 12

/** Rows of «Почему изменился». */
const val WHY_MONTHS = 3
