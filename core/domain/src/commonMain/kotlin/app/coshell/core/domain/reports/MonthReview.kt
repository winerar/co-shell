package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetPeriod
import app.coshell.core.domain.budgets.BudgetProgress
import app.coshell.core.model.Category
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlinx.datetime.LocalDate

/**
 * «Итог месяца» (#235, research `reports-set.md` П1): how the month went and what to change. Counted by the rules
 * of all reports (#187): refunds reduce spending, transfers and balance adjustments are not counted.
 */
data class MonthReview(
    val period: BudgetPeriod,
    val spent: Money,
    val earned: Money,
    /** The months [changes] are compared with: up to [USUAL_MONTHS], fewer at the start of the history. */
    val usualMonths: Int,
    /** Up to [TOP_CHANGES] folders that spent noticeably more or less than usual. */
    val changes: List<CategoryChange>,
    /** `null` — the month had no limits. */
    val limits: LimitsTally?,
    /** Currencies left out for lack of a rate. */
    val missing: Set<CurrencyCode> = emptySet(),
) {
    /** What was left: [earned] − [spent]; negative — spent more than came in. */
    val saved: Money get() = earned - spent

    /** Savings rate: [saved] in whole percents of [earned], negative when spent more; `null` — nothing came in. */
    val savingsRate: Int? get() = percentOf(saved, earned)

    val isEmpty: Boolean get() = !spent.isPositive && !earned.isPositive && changes.isEmpty()
}

/**
 * The review of [month] with the [usualMonths] before it. [limits] — `BudgetCalculator.month` of the same month.
 * The ledger must hold the days from [reviewStart] to the end of [month]. Meant for a finished month: a running
 * one is compared whole with whole months.
 */
fun monthReview(
    ledger: ReportLedger,
    categories: List<Category>,
    month: BudgetPeriod,
    startDay: Int,
    limits: List<BudgetProgress> = emptyList(),
    usualMonths: Int = USUAL_MONTHS,
): MonthReview {
    val currency = ledger.currency
    val spent = ledger.spent(month)
    val earned = ledger.earned(month)
    val before = monthsBefore(month, startDay, usualMonths)
    val history = before.count { ledger.hasOperations(it) }
    return MonthReview(
        period = month,
        spent = spent.total(currency),
        earned = earned.total(currency),
        usualMonths = history,
        changes = usualChanges(ledger, categories, month, before),
        limits = limitsTally(limits),
        missing = spent.missing + earned.missing,
    )
}

/** The first day the ledger of [monthReview] must hold: the start of the first month compared with. */
fun reviewStart(month: BudgetPeriod, startDay: Int, usualMonths: Int = USUAL_MONTHS): LocalDate =
    monthsBefore(month, startDay, usualMonths).firstOrNull()?.start ?: month.start
