package app.coshell.core.domain.reports

import app.coshell.core.domain.rates.CurrencyConverter
import app.coshell.core.domain.recurring.between
import app.coshell.core.domain.recurring.nextUnhandled
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * «Прогноз остатка» (#770, poll 2026-11-27 «А»): the balance of the accounts in the total, day by day, [horizon] days ahead.
 * It is the money now, plus the repeating payments and incomes of the templates with «Повтор», plus the way the rest of the
 * money has gone on average over the last [HISTORY_DAYS] days ([variableDaily], negative — spending more than coming in).
 * All of it in [currency], whole minor units: no floating point near money.
 *
 * [points] — today and every day up to the horizon; [lowest] — the first lowest day; [firstBelowZero] — the first day under
 * zero (`null` — the money lasts); [missing] — currencies left out for lack of a rate.
 */
data class BalanceForecast(
    val currency: CurrencyCode,
    val points: List<ForecastPoint>,
    val lowest: ForecastPoint,
    val firstBelowZero: LocalDate?,
    val missing: Set<CurrencyCode>,
) {
    /** What is left on the last day under zero (a positive number, «дальше −40»): the deepest dip; 0 — never under. */
    val deepestShortfallMinor: Long get() = (-lowest.balanceMinor).coerceAtLeast(0)
}

data class ForecastPoint(val day: LocalDate, val balanceMinor: Long)

/** A payment (negative) or income (positive) of a template on [day], in the forecast currency. */
class ScheduledFlow(val day: LocalDate, val amountMinor: Long)

/** Days of history the «rest of the money» average is taken over. */
const val HISTORY_DAYS = 90

object BalanceForecasts {
    /**
     * The days of [from]..[to] (inclusive) on which templates with «Повтор» and an amount pay or bring money, in [total] at
     * today's rate. A payment already due and waiting for «Провести» (before [from]) counts on [from]. Transfers move money
     * between our own accounts: not a flow of the total. Currencies without a rate go to the second part.
     */
    fun scheduledFlows(
        templates: List<TransactionTemplate>,
        converter: CurrencyConverter,
        total: CurrencyCode,
        today: LocalDate,
        from: LocalDate,
        to: LocalDate,
    ): Pair<List<ScheduledFlow>, Set<CurrencyCode>> {
        val flows = mutableListOf<ScheduledFlow>()
        val missing = linkedSetOf<CurrencyCode>()
        for (template in templates) {
            val schedule = template.schedule ?: continue
            val amount = template.amount?.takeIf { it.isPositive } ?: continue
            val sign = when (template.type) {
                TransactionType.EXPENSE -> -1L
                TransactionType.INCOME -> 1L
                TransactionType.TRANSFER -> continue
            }
            val converted = converter.convert(amount, total, today)?.money
            if (converted == null) {
                missing += amount.currency
                continue
            }
            val next = schedule.nextUnhandled()
            val days = schedule.recurrence.between(maxOf(next, from), to)
            // A payment overdue before [from] and not handled yet is expected right away.
            if (next < from && schedule.recurrence.between(next, from.minus(1, DateTimeUnit.DAY)).isNotEmpty()) {
                flows += ScheduledFlow(from, sign * converted.amountMinor)
            }
            days.forEach { flows += ScheduledFlow(it, sign * converted.amountMinor) }
        }
        return flows to missing
    }

    /** The same templates' flows on the days of [from]..[to] that are already behind: what history already contains. */
    fun pastScheduledNet(
        templates: List<TransactionTemplate>,
        converter: CurrencyConverter,
        total: CurrencyCode,
        today: LocalDate,
        from: LocalDate,
        to: LocalDate,
    ): Long {
        var net = 0L
        for (template in templates) {
            val schedule = template.schedule ?: continue
            val amount = template.amount?.takeIf { it.isPositive } ?: continue
            val sign = when (template.type) {
                TransactionType.EXPENSE -> -1L
                TransactionType.INCOME -> 1L
                TransactionType.TRANSFER -> continue
            }
            val converted = converter.convert(amount, total, today)?.money ?: continue
            net += sign * converted.amountMinor * schedule.recurrence.between(maxOf(schedule.start, from), to).size
        }
        return net
    }

    /**
     * What the money does on a day besides the templates: the net of the last [HISTORY_DAYS] days ([historyNet] — income
     * minus spending) without what the templates already did in them ([pastScheduledNet]), spread over the days. A day
     * without any history says nothing, so a phone with no history gives 0.
     */
    fun variableDaily(historyNet: Long, pastScheduledNet: Long): Long = (historyNet - pastScheduledNet) / HISTORY_DAYS

    /** The forecast: [startMinor] on [today], then each day's flows and [variableDaily]. */
    fun of(
        currency: CurrencyCode,
        startMinor: Long,
        today: LocalDate,
        horizon: Int,
        flows: List<ScheduledFlow>,
        variableDaily: Long,
        missing: Set<CurrencyCode> = emptySet(),
    ): BalanceForecast {
        val byDay = flows.groupBy { it.day }.mapValues { (_, list) -> list.sumOf { it.amountMinor } }
        val points = mutableListOf(ForecastPoint(today, startMinor + (byDay[today] ?: 0L)))
        for (i in 1..horizon) {
            val day = today.plus(i, DateTimeUnit.DAY)
            points += ForecastPoint(day, points.last().balanceMinor + variableDaily + (byDay[day] ?: 0L))
        }
        val lowest = points.minBy { it.balanceMinor }
        return BalanceForecast(currency, points, lowest, points.firstOrNull { it.balanceMinor < 0 }?.day, missing)
    }

    /** Money as a [Money] of the forecast currency, for formatting. */
    fun BalanceForecast.money(minor: Long): Money = Money(minor, currency)
}
