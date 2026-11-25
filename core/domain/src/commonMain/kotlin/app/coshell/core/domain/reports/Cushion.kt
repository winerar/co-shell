package app.coshell.core.domain.reports

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money

/**
 * «Подушка» (#241): how many months the money on the accounts counted into the total would last at the average
 * spending. [monthlySpend] — average over [months] full months; [missing] — currencies left out for lack of a rate.
 */
data class Cushion(
    val money: Money,
    val monthlySpend: Money,
    val months: Int,
    val missing: Set<CurrencyCode>,
) {
    /** Months the money lasts, in tenths (45 = «≈ 4,5 мес.»); never negative. */
    val tenths: Long get() = if (money.isPositive) money.scale(10, monthlySpend.amountMinor).amountMinor else 0L

    val wholeMonths: Long get() = tenths / 10
}

/**
 * The cushion at [worth] (the money now) against the spending of the last [HISTORY_MONTHS] full months of [series]
 * (oldest first; a running month is left out — it is not full yet). Months without any spending do not pull the average
 * down: a month the app was not used says nothing. `null` — no full month with spending, so there is nothing to divide by.
 */
fun cushion(worth: NetWorth, series: List<MonthTotals>): Cushion? {
    val running = worth.points.filter { it.running }.map { it.period }.toSet()
    val spending = series.filter { it.period !in running && it.spent.isPositive }.takeLast(HISTORY_MONTHS)
    if (spending.isEmpty()) return null
    val currency = worth.now.currency
    val average = spending.fold(Money.zero(currency)) { sum, m -> sum + m.spent }.scale(1, spending.size.toLong())
    if (!average.isPositive) return null
    return Cushion(worth.now, average, spending.size, worth.missing + spending.flatMap { it.missing })
}

/** Months of spending the cushion is counted from: half a year. */
const val HISTORY_MONTHS = 6
