package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetPeriod
import app.coshell.core.model.Category
import app.coshell.core.model.Money
import kotlinx.datetime.LocalDate

/** Whether a category spends more or less than usual. */
enum class UsualDirection { ABOVE, BELOW }

/**
 * «Кафе: +45 % к среднему за 3 мес.» (#237): [amount] spent so far against [expected] — the usual monthly spending
 * cut to the days that have passed (the pace), so the 10th of a month is compared with a third of the usual.
 * [percent] — the difference to [expected]; `null` when nothing was usually spent («новое»).
 */
data class UsualMark(
    val direction: UsualDirection,
    val amount: Money,
    val expected: Money,
    val percent: Int?,
) {
    /** [amount] − [expected]: positive — spending faster than usual. */
    val difference: Money get() = amount - expected
}

/** A difference smaller than this many percents of the usual is not «выше обычного» (one more coffee is not news). */
const val MARK_MIN_PERCENT = 20

/** A running month is not marked before this many days: one purchase on the 2nd says nothing about the pace. */
const val MARK_MIN_DAYS = 7

/**
 * The usual of [current] — the spending of one category set in each month of [usualMonths] — and the mark, if any.
 * [usualTotal] — the usual monthly spending of all categories: a difference below [CHANGE_MIN_SHARE_PERCENT] of it
 * (cut to the pace) is noise, as in [usualChanges]. `null` without history, before [MARK_MIN_DAYS] or when the
 * difference is too small by amount or by percent.
 */
internal fun usualMark(
    current: Money,
    usualMonths: List<Money>,
    usualTotal: Money,
    month: BudgetPeriod,
    today: LocalDate,
): UsualMark? {
    if (usualMonths.isEmpty()) return null
    val passed = month.daysPassed(today)
    if (passed == 0 || (passed < month.days && passed < MARK_MIN_DAYS)) return null

    val currency = current.currency
    val usual = usualMonths.fold(Money.zero(currency)) { acc, it -> acc + it }.scale(1, usualMonths.size.toLong())
    val expected = usual.scale(passed.toLong(), month.days.toLong())
    val threshold = usualTotal.scale(passed.toLong(), month.days.toLong()).scale(CHANGE_MIN_SHARE_PERCENT.toLong(), 100)
    val difference = current - expected
    if (difference.amountMinor == 0L || difference.abs() < threshold) return null

    val percent = percentOf(difference, expected)
    if (percent != null && kotlin.math.abs(percent) < MARK_MIN_PERCENT) return null
    val direction = if (difference.isPositive) UsualDirection.ABOVE else UsualDirection.BELOW
    return UsualMark(direction, current, expected, percent)
}

/**
 * Marks of the folders (and categories without one) of «На что ушли деньги» for [month] up to [today]: folder id →
 * mark, only those spending noticeably more or less than over the [USUAL_MONTHS] before. The ledger must hold
 * [month] and the months before it; months before the history starts are not counted (see [usualHistory]).
 */
fun usualMarks(
    ledger: ReportLedger,
    categories: List<Category>,
    month: BudgetPeriod,
    startDay: Int,
    today: LocalDate,
): Map<String, UsualMark> {
    val currency = ledger.currency
    val history = usualHistory(ledger, monthsBefore(month, startDay))
    if (history.isEmpty()) return emptyMap()

    fun byFolder(period: BudgetPeriod) =
        spendingByFolder(ledger.spent(period), categories, currency).mapValues { it.value.atLeastZero() }

    val current = byFolder(month)
    val past = history.map { byFolder(it) }
    val usualTotal = usualTotal(ledger, history)
    val zero = Money.zero(currency)
    val ids = (current.keys + past.flatMap { it.keys }).toSortedSet()
    return ids.mapNotNull { id ->
        usualMark(current[id] ?: zero, past.map { it[id] ?: zero }, usualTotal, month, today)?.let { id to it }
    }.toMap()
}

/**
 * [months] (oldest first) from the first one with any operations on: the months before are before the family's
 * history and would pull the average down (as `BudgetLedger.averageSpent`). A quiet month inside the history counts.
 */
internal fun usualHistory(ledger: ReportLedger, months: List<BudgetPeriod>): List<BudgetPeriod> {
    val first = months.indexOfFirst { ledger.hasOperations(it) }
    return if (first < 0) emptyList() else months.drop(first)
}

/** The average monthly spending of all categories over [history] (not empty). */
internal fun usualTotal(ledger: ReportLedger, history: List<BudgetPeriod>): Money {
    val currency = ledger.currency
    return history.fold(Money.zero(currency)) { acc, it -> acc + ledger.spent(it).total(currency).atLeastZero() }
        .scale(1, history.size.toLong())
}

internal fun Money.atLeastZero(): Money = if (isNegative) Money.zero(currency) else this
