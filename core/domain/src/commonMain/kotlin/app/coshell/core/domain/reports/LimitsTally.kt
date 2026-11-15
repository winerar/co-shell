package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetProgress
import app.coshell.core.domain.budgets.BudgetState

/**
 * The limits of a month in «Итог месяца» (#235): how many there were, how many stayed within and which ones were
 * overspent — [over] largest overspending first by its share of the limit (limits are in different currencies,
 * so amounts are not compared). Each limit counts once, a folder's and a category's inside it too.
 */
data class LimitsTally(
    val total: Int,
    val within: Int,
    val over: List<BudgetProgress>,
)

/** [limits] of one month (`BudgetCalculator.month`) counted up; `null` when the month had none. */
fun limitsTally(limits: List<BudgetProgress>): LimitsTally? {
    if (limits.isEmpty()) return null
    val over = limits.filter { it.state == BudgetState.OVER }
        .sortedWith(compareByDescending<BudgetProgress> { overShare(it) }.thenBy { it.budget.categoryId })
    return LimitsTally(total = limits.size, within = limits.size - over.size, over = over)
}

/** Overspending in percents of what the limit had; a limit with nothing available — by the amount itself. */
private fun overShare(progress: BudgetProgress): Long {
    val overspent = -progress.left
    return percentOf(overspent, progress.available)?.toLong() ?: Long.MAX_VALUE
}
