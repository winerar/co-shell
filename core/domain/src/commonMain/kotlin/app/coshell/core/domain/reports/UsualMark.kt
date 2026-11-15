package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetPeriod
import app.coshell.core.model.Category
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/** Whether a category spends more or less than usual. */
enum class UsualDirection { ABOVE, BELOW }

/**
 * «Кафе: +45 % к среднему за 3 мес.» (#237): [amount] spent so far against [expected] — the usual by the same day:
 * the average of what the months before spent in as many first days (all of them once the month is over). Rent paid
 * on the 5th is then usual on the 11th, not «+170 %». [percent] — the difference to [expected]; `null` when nothing
 * was usually spent in the whole month («новое»).
 */
data class UsualMark(
    val direction: UsualDirection,
    val amount: Money,
    val expected: Money,
    val percent: Int?,
) {
    /** [amount] − [expected]: positive — spending more than usual by now. */
    val difference: Money get() = amount - expected
}

/** A difference smaller than this many percents of the usual is not «выше обычного» (one more coffee is not news). */
const val MARK_MIN_PERCENT = 20

/** A running month is not marked before this many days: one purchase on the 2nd says nothing yet. */
const val MARK_MIN_DAYS = 7

/** What one category set spent in a month: by the same day ([sameDays]) and in the whole month ([whole]). */
internal data class UsualSpending(val sameDays: Money, val whole: Money)

/**
 * The mark of [current] against [usual] — the same category set in each month of the history (not empty).
 * [usualTotal] — the usual spending of all categories by the same day: a difference below
 * [CHANGE_MIN_SHARE_PERCENT] of it is noise, as in [usualChanges]. Spending that came earlier than usual (nothing by
 * now in the months before, but something later) is not «выше обычного». `null` before [MARK_MIN_DAYS] or when the
 * difference is too small by amount or by percent.
 */
internal fun usualMark(current: Money, usual: List<UsualSpending>, usualTotal: Money, month: BudgetPeriod, today: LocalDate): UsualMark? {
    val passed = month.daysPassed(today)
    if (usual.isEmpty() || passed == 0 || (passed < month.days && passed < MARK_MIN_DAYS)) return null

    val expected = usual.map { it.sameDays }.average(current.currency)
    val difference = current - expected
    val threshold = usualTotal.scale(CHANGE_MIN_SHARE_PERCENT.toLong(), 100)
    if (difference.amountMinor == 0L || difference.abs() < threshold) return null

    val percent = percentOf(difference, expected)
    if (percent == null && usual.any { it.whole.isPositive }) return null
    if (percent != null && kotlin.math.abs(percent) < MARK_MIN_PERCENT) return null
    val direction = if (difference.isPositive) UsualDirection.ABOVE else UsualDirection.BELOW
    return UsualMark(direction, current, expected, percent)
}

/**
 * Marks of the folders (and categories without one) of «На что ушли деньги» for [month] up to [today]: folder id →
 * mark, only those spending noticeably more or less than by the same day of the [USUAL_MONTHS] before. The ledger
 * must hold [month] and the months before it; months before the history starts are not counted (see [usualHistory]).
 */
fun usualMarks(
    ledger: ReportLedger,
    categories: List<Category>,
    month: BudgetPeriod,
    startDay: Int,
    today: LocalDate,
): Map<String, UsualMark> = usualMarksBy(ledger, month, startDay, today) { spendingByFolder(it, categories, ledger.currency) }

/**
 * The same marks for each category on its own (#238, «Все категории» lists them flat): category id → mark.
 * Refunds above a category's spending count as zero.
 */
fun usualCategoryMarks(ledger: ReportLedger, month: BudgetPeriod, startDay: Int, today: LocalDate): Map<String, UsualMark> =
    usualMarksBy(ledger, month, startDay, today) { it.byCategory }

/** Marks of the groups [group] makes of a month's sums (id → amount). */
private fun usualMarksBy(
    ledger: ReportLedger,
    month: BudgetPeriod,
    startDay: Int,
    today: LocalDate,
    group: (CategorySums) -> Map<String, Money>,
): Map<String, UsualMark> {
    val currency = ledger.currency
    val history = usualHistory(ledger, monthsBefore(month, startDay))
    if (history.isEmpty()) return emptyMap()
    val passed = month.daysPassed(today)
    val complete = passed == month.days

    fun grouped(sums: CategorySums) = group(sums).mapValues { it.value.atLeastZero() }

    val current = grouped(ledger.spent(month))
    val sameDays = history.map { grouped(ledger.spentByDay(it, passed, complete)) }
    val whole = history.map { grouped(ledger.spent(it)) }
    val usualTotal = usualTotal(ledger, history, passed, complete)
    val zero = Money.zero(currency)
    val ids = (current.keys + whole.flatMap { it.keys }).toSortedSet()
    return ids.mapNotNull { id ->
        val usual = history.indices.map { UsualSpending(sameDays[it][id] ?: zero, whole[it][id] ?: zero) }
        usualMark(current[id] ?: zero, usual, usualTotal, month, today)?.let { id to it }
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

/** The average spending of all categories over [history] (not empty) by the same day (see [spentByDay]). */
internal fun usualTotal(ledger: ReportLedger, history: List<BudgetPeriod>, passed: Int, complete: Boolean): Money =
    history.map { ledger.spentByDay(it, passed, complete).total(ledger.currency).atLeastZero() }.average(ledger.currency)

/**
 * Spending of [period] in its first [passed] days (no more than it has) — or all of it when the month compared with
 * it is [complete]: a whole October against a whole September of 30 days.
 */
internal fun ReportLedger.spentByDay(period: BudgetPeriod, passed: Int, complete: Boolean): CategorySums =
    if (complete) spent(period)
    else spent(period.start, period.start.plus(DatePeriod(days = passed.coerceAtMost(period.days))))

internal fun List<Money>.average(currency: CurrencyCode): Money =
    fold(Money.zero(currency)) { acc, it -> acc + it }.scale(1, size.toLong())

internal fun Money.atLeastZero(): Money = if (isNegative) Money.zero(currency) else this
