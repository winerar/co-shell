package app.coshell.reports.review

import app.coshell.budgets.list.BudgetsUiState.Envelope
import kotlinx.datetime.YearMonth

/**
 * «Итог месяца» (#236, boards `Mon236A`, `Mon236Screen`, `Mon236Limits`, `Mon236Edge`): how a finished month went —
 * what was left and the savings rate, the three biggest changes against the usual months, the limits kept and
 * overspent. Amounts are numbers without the code, in [currency] (the main report currency) at each day's rate.
 */
data class MonthReviewUiState(
    val isLoading: Boolean = false,
    val month: YearMonth? = null,
    /** The family's month start day: the period of «Из чего сложилось» opened from a change. */
    val startDay: Int = 1,
    val currency: String = "",
    /** «750» or «-1 350»; [overspent] — spent more than came in. */
    val saved: String = "",
    val overspent: Boolean = false,
    /** Whole percents of the income left; `null` — nothing came in. */
    val savingsRate: Int? = null,
    val spent: String = "",
    val earned: String = "",
    /** The months the changes are compared with: 0 — no history yet, 1–3. */
    val usualMonths: Int = 0,
    val changes: List<Change> = emptyList(),
    /** `null` — the month had no limits: no block. */
    val limits: Limits? = null,
    /** Currencies left out for lack of a rate. */
    val missing: List<String> = emptyList(),
    /** Nothing spent, nothing came in: the card does not show. */
    val isEmpty: Boolean = false,
) {
    /** «Подарки · 120 · обычно 30 · +90 · +300%». */
    data class Change(
        val categoryId: String,
        /** `null` — a category deleted on another phone. */
        val name: String?,
        val icon: String?,
        val color: Long?,
        val amount: String,
        val usual: String,
        /** Signed: «+90», «-75». */
        val difference: String,
        /** Signed percent of the usual; `null` — nothing usual to compare with. */
        val percent: Int?,
        /** Spent more than usual (the warning colour), less — the calm one. */
        val more: Boolean,
    )

    /** The month's limits, inner limits of a folder as rows of their own. */
    data class Limits(
        val within: Int,
        /** Overspent, the largest overspending first. */
        val over: List<Envelope>,
        /** Every limit: overspent first, then by name (as «Бюджеты» sorts). */
        val all: List<Envelope>,
    ) {
        val total: Int get() = all.size
    }
}
