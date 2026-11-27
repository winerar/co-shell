package app.coshell.core.domain.recurring

import app.coshell.core.domain.rates.CurrencyConverter
import app.coshell.core.domain.reports.MonthTotals
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Recurrence
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate

/**
 * «Постоянные траты» (#771, poll 2026-11-27 «В · А»): what the repeating expenses cost in a year. Only expense templates with
 * «Повтор» and an amount: a month ×12, a week ×52, a year ×1. Pure; the card in «Отчёты» and «Регулярные → За год» show it.
 */
data class FixedItem(
    val template: TransactionTemplate,
    /** A year and a month of it in the template's own currency. */
    val yearly: Money,
    val monthly: Money,
    /** [yearly] in the totals currency at today's rate; `null` — no rate. */
    val yearlyInTotal: Money?,
)

/**
 * [items] — the dearest first (by [FixedItem.yearlyInTotal], those without a rate last); [yearly]/[monthly] — their sum in the
 * totals currency; [sharePercent] — the month of them against the average spending of the last full months (`null` — no
 * spending to compare with); [missing] — currencies without a rate, left out of the sum.
 */
data class FixedSpending(
    val items: List<FixedItem>,
    val yearly: Money,
    val monthly: Money,
    val sharePercent: Int?,
    val missing: Set<CurrencyCode>,
)

/** How many times a year the rule comes. */
val Recurrence.timesAYear: Int
    get() = when (this) {
        is Recurrence.Monthly -> MONTHS
        is Recurrence.Weekly -> WEEKS
        is Recurrence.Yearly -> 1
    }

/** A year of each repeating expense with an amount; `null` when there is none. */
fun fixedSpending(
    templates: List<TransactionTemplate>,
    converter: CurrencyConverter,
    total: CurrencyCode,
    today: LocalDate,
    months: List<MonthTotals> = emptyList(),
): FixedSpending? {
    val items = templates.mapNotNull { template ->
        val schedule = template.schedule ?: return@mapNotNull null
        val amount = template.amount?.takeIf { it.isPositive && template.type == TransactionType.EXPENSE } ?: return@mapNotNull null
        val yearly = amount.scale(schedule.recurrence.timesAYear.toLong(), 1)
        FixedItem(template, yearly, yearly.scale(1, MONTHS.toLong()), converter.convert(yearly, total, today)?.money)
    }
    if (items.isEmpty()) return null
    val sorted = items.sortedWith(compareByDescending<FixedItem> { it.yearlyInTotal?.amountMinor ?: -1L }.thenBy { it.template.name.lowercase() })
    val yearly = items.mapNotNull { it.yearlyInTotal }.fold(Money.zero(total)) { sum, m -> sum + m }
    val monthly = yearly.scale(1, MONTHS.toLong())
    return FixedSpending(
        items = sorted,
        yearly = yearly,
        monthly = monthly,
        sharePercent = sharePercent(monthly, months, today),
        missing = items.filter { it.yearlyInTotal == null }.map { it.yearly.currency }.toSet(),
    )
}

/**
 * [monthly] against the average spending of up to [SHARE_MONTHS] last months that are over by [today] and had spending, in
 * whole percent (capped at 100: templates may plan more than was spent). [months] are in [monthly]'s currency.
 */
internal fun sharePercent(monthly: Money, months: List<MonthTotals>, today: LocalDate): Int? {
    if (!monthly.isPositive) return null
    val full = months.filter { it.period.endExclusive <= today && it.spent.isPositive && it.spent.currency == monthly.currency }.takeLast(SHARE_MONTHS)
    if (full.isEmpty()) return null
    val average = full.fold(Money.zero(monthly.currency)) { sum, m -> sum + m.spent }.scale(1, full.size.toLong())
    if (!average.isPositive) return null
    val percent = monthly.scale(PERCENT, average.amountMinor).amountMinor
    return percent.coerceIn(1L, PERCENT).toInt()
}

/** The months the share is counted over. */
const val SHARE_MONTHS = 3

private const val MONTHS = 12
private const val WEEKS = 52
private const val PERCENT = 100L
