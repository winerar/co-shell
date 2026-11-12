package app.coshell.home.budget

import app.coshell.budgets.BudgetSource
import app.coshell.budgets.list.BudgetFilter
import app.coshell.budgets.list.BudgetsUiState
import app.coshell.budgets.list.buildEnvelopes
import app.coshell.budgets.list.buildSummary
import app.coshell.core.designsystem.components.BulletTone
import app.coshell.core.domain.budgets.budgetPeriodOf
import app.coshell.core.model.MoneyFormat
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/**
 * Budgets on «Обзор» (board `BudHome`, #184): the tile «Бюджет · 19 дней» with what is left of the current budget
 * month and «Горит в бюджете» — the envelopes over the limit or ahead of pace (up to [HOT_COUNT]).
 * @property month the current budget month (a tap on an envelope opens it in this month).
 */
data class HomeBudget(
    val month: YearMonth,
    val summary: BudgetsUiState.Summary,
    /** Over the limit first, then ahead of pace; empty — no block. */
    val hot: List<BudgetsUiState.Envelope> = emptyList(),
) {
    companion object {
        /** «2–3 конверта» on the board: more would push the latest operations off the screen. */
        const val HOT_COUNT = 3
    }
}

/**
 * Pure: the budget part of «Обзор» for [today] — the same numbers as «Бюджеты» shows for the current month.
 * `null` — no limit in this month: no tile and no block.
 */
internal fun buildHomeBudget(snapshot: BudgetSource.Snapshot, today: LocalDate, format: MoneyFormat): HomeBudget? {
    if (snapshot.budgets.isEmpty()) return null
    val month = budgetPeriodOf(today, snapshot.startDay).month
    val calculator = snapshot.calculator()
    val envelopes = buildEnvelopes(calculator.month(month, today), snapshot.categories, format)
    if (envelopes.isEmpty()) return null
    return HomeBudget(
        month = month,
        summary = buildSummary(calculator.summary(month, today, snapshot.mainCurrency), today, format),
        hot = hotEnvelopes(envelopes),
    )
}

/**
 * The envelopes that need a look, a folder's inner limits among them (shown as rows of their own, without nesting):
 * over the limit first, then ahead of pace; [envelopes] are already sorted by name within a state.
 */
internal fun hotEnvelopes(envelopes: List<BudgetsUiState.Envelope>, count: Int = HomeBudget.HOT_COUNT): List<BudgetsUiState.Envelope> =
    envelopes.flatMap { listOf(it.copy(children = emptyList())) + it.children }
        .filter { it.matches(BudgetFilter.HOT) }
        .sortedBy { if (it.tone == BulletTone.DANGER) 0 else 1 }
        .take(count)
