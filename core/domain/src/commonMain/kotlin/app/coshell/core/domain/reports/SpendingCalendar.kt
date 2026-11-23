package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetPeriod
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/** Net spending per day in one currency; days with nothing spent are absent, [missing] — currencies left out for lack of a rate. */
data class DaySums(val byDay: Map<LocalDate, Money>, val missing: Set<CurrencyCode>)

/**
 * One cell of «Календарь трат» (#553). [level] is 0 for a day without spending and 1..[SpendingCalendar.LEVELS] by amount;
 * a day after today ([future]) has no level and is drawn quiet.
 */
data class CalendarDay(val day: LocalDate, val spent: Money, val level: Int, val future: Boolean)

/** The days of one period with their brightness; [noSpendDays] counts the days up to today with nothing spent. */
data class SpendingCalendar(val days: List<CalendarDay>, val noSpendDays: Int, val missing: Set<CurrencyCode>) {
    companion object {
        const val LEVELS = 4
    }
}

/**
 * «Календарь трат»: brightness by quartiles of the days that had spending, so one huge day (the rent) does not make the
 * others look the same. A refund day (net ≤ 0) counts as a day without spending.
 */
fun spendingCalendar(ledger: ReportLedger, period: BudgetPeriod, today: LocalDate): SpendingCalendar {
    val sums = ledger.spentByDay(period.start, period.endExclusive)
    val zero = Money.zero(ledger.currency)
    val spentDays = (0 until period.days).map { period.start.plus(DatePeriod(days = it)) }
    val amounts = spentDays.associateWith { sums.byDay[it]?.takeIf { m -> m.amountMinor > 0 } ?: zero }
    val thresholds = quartileThresholds(amounts.values.map { it.amountMinor }.filter { it > 0 })
    val days = spentDays.map { d ->
        val spent = amounts.getValue(d)
        val future = d > today
        CalendarDay(d, spent, if (future) 0 else levelOf(spent.amountMinor, thresholds), future)
    }
    return SpendingCalendar(days, days.count { !it.future && it.spent.amountMinor == 0L }, sums.missing)
}

/** Upper bounds of levels 1..3 (level 4 is above the last one); empty for no data. */
internal fun quartileThresholds(positive: List<Long>): List<Long> {
    if (positive.isEmpty()) return emptyList()
    val sorted = positive.sorted()
    return (1..3).map { quarter -> sorted[(sorted.size - 1) * quarter / 4] }
}

internal fun levelOf(minor: Long, thresholds: List<Long>): Int =
    if (minor <= 0) 0 else 1 + thresholds.count { minor > it }
