package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.budgetPeriodOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/** How many first days of a budget month «Обзор» offers the review of the month before (#236, board `Mon236A`). */
const val REVIEW_CARD_DAYS = 7

/**
 * The month whose review «Обзор» shows on [today], or `null`: only in the first [REVIEW_CARD_DAYS] days of the
 * family's month (from [startDay], payday included), and not once the user hid it ([dismissed] — the month hidden
 * last; ✕ keeps it away until the next month).
 */
fun monthReviewDue(today: LocalDate, startDay: Int, dismissed: YearMonth?): YearMonth? {
    val current = budgetPeriodOf(today, startDay)
    if (current.daysPassed(today) > REVIEW_CARD_DAYS) return null
    val previous = current.previous(startDay).month
    return previous.takeIf { it != dismissed }
}
