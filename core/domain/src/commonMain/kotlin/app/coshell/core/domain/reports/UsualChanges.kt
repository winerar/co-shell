package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetPeriod
import app.coshell.core.model.Category
import app.coshell.core.model.Money
import app.coshell.core.model.sum

/**
 * A folder (or a category without one) that spent noticeably more or less than usual (#235): [amount] this month
 * against [usual] — the average of the months before. [percent] — the difference to [usual]; `null` when it
 * spent nothing then («новое»).
 */
data class CategoryChange(
    val categoryId: String,
    val amount: Money,
    val usual: Money,
    val percent: Int?,
) {
    /** [amount] − [usual]: positive — spent more than usual. */
    val difference: Money get() = amount - usual
}

/** Months the usual spending is averaged over (research `reports-set.md` §4: one month alone is often random). */
const val USUAL_MONTHS = 3

/** How many changes «Итог месяца» shows. */
const val TOP_CHANGES = 3

/**
 * A change smaller than this share of the usual monthly spending (all categories) is noise and not shown.
 * A share, not an amount: 5 000 is a trifle in pesos and a lot in dollars.
 */
const val CHANGE_MIN_SHARE_PERCENT = 5

/**
 * The [top] folders whose spending in [month] differs most from their average over [before] (the months before it),
 * by the amount, larger changes first; less than [CHANGE_MIN_SHARE_PERCENT] of the usual monthly spending is not
 * a change. Months before [before] that have no operations at all are before the history starts and are left out
 * of the average; with none left there is nothing to compare with. Folders are grouped as in «На что ушли деньги»;
 * one with more refunds than spending counts as zero.
 */
fun usualChanges(
    ledger: ReportLedger,
    categories: List<Category>,
    month: BudgetPeriod,
    before: List<BudgetPeriod>,
    top: Int = TOP_CHANGES,
): List<CategoryChange> {
    val currency = ledger.currency
    val history = before.filter { ledger.hasOperations(it) }
    if (history.isEmpty()) return emptyList()

    fun byFolder(period: BudgetPeriod) =
        spendingByFolder(ledger.spent(period), categories, currency).mapValues { it.value.atLeastZero() }

    val current = byFolder(month)
    val past = history.map { byFolder(it) }
    val usualTotal = past.fold(Money.zero(currency)) { acc, it -> acc + it.values.sum(currency) }.scale(1, history.size.toLong())
    val threshold = usualTotal.scale(CHANGE_MIN_SHARE_PERCENT.toLong(), 100)

    val ids = (current.keys + past.flatMap { it.keys }).toSet()
    return ids.map { id ->
        val usual = past.fold(Money.zero(currency)) { acc, it -> acc + (it[id] ?: Money.zero(currency)) }.scale(1, history.size.toLong())
        val amount = current[id] ?: Money.zero(currency)
        CategoryChange(id, amount, usual, percentOf(amount - usual, usual))
    }
        .filter { it.difference.abs() >= threshold && !it.difference.isZero }
        .sortedWith(compareByDescending<CategoryChange> { it.difference.abs().amountMinor }.thenBy { it.categoryId })
        .take(top)
}

/** The [count] months right before [month], oldest first. */
fun monthsBefore(month: BudgetPeriod, startDay: Int, count: Int = USUAL_MONTHS): List<BudgetPeriod> =
    reportMonths(month.month, count + 1, startDay).dropLast(1)

/** [part] in whole percents of [whole], rounded half to even; `null` when [whole] is not positive. */
internal fun percentOf(part: Money, whole: Money): Int? {
    if (!whole.isPositive) return null
    val magnitude = part.abs().scale(100, whole.amountMinor).amountMinor
    val clamped = magnitude.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    return if (part.isNegative) -clamped else clamped
}

/** Whether [period] has any operations — months before the history starts have none. */
internal fun ReportLedger.hasOperations(period: BudgetPeriod): Boolean {
    val spent = spent(period)
    val earned = earned(period)
    return spent.byCategory.isNotEmpty() || earned.byCategory.isNotEmpty() || spent.missing.isNotEmpty() || earned.missing.isNotEmpty()
}

private val Money.isZero: Boolean get() = amountMinor == 0L
