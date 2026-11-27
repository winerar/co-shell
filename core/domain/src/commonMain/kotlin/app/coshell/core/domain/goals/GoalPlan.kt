package app.coshell.core.domain.goals

import app.coshell.core.model.Money
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.monthsUntil
import kotlinx.datetime.plus

/**
 * Where a savings goal stands (#768): how much is left and how much to put aside each month to make it by the deadline.
 * [deadline] is the day of the current round: for a yearly goal (insurance, taxes) a passed day moves on by whole years.
 */
data class GoalProgress(
    val saved: Money,
    val left: Money,
    val reached: Boolean,
    val deadline: LocalDate?,
    /** What to put aside each month to make it; null with no deadline, when reached or when the deadline has passed. */
    val perMonth: Money?,
    /** A one-off goal whose day has passed and that is not reached yet. */
    val overdue: Boolean,
)

object GoalPlan {
    fun of(target: Money, saved: Money, deadline: LocalDate?, yearly: Boolean, today: LocalDate): GoalProgress {
        require(target.amountMinor > 0) { "goal target must be positive" }
        val kept = if (saved.isNegative) Money.zero(saved.currency) else saved
        val left = if (kept >= target) Money.zero(target.currency) else target - kept
        val reached = left.amountMinor == 0L
        val day = deadline?.let { if (yearly) nextRound(it, today) else it }
        val months = day?.takeIf { it >= today }?.let { monthsLeft(today, it) }
        return GoalProgress(
            saved = kept,
            left = left,
            reached = reached,
            deadline = day,
            perMonth = if (reached || months == null) null else perMonth(left, months),
            overdue = !reached && day != null && day < today,
        )
    }

    /**
     * How many monthly contributions are left until [deadline]: whole months, plus one for a part of a month, at least one
     * (26 November → 1 July is 7 months and 5 days, so 8).
     */
    fun monthsLeft(today: LocalDate, deadline: LocalDate): Int {
        require(deadline >= today) { "deadline must not be in the past" }
        val whole = today.monthsUntil(deadline)
        val rest = today.plus(DatePeriod(months = whole)) < deadline
        return maxOf(1, whole + if (rest) 1 else 0)
    }

    /** The first round of a yearly [deadline] on or after [today]. */
    fun nextRound(deadline: LocalDate, today: LocalDate): LocalDate {
        var years = 0
        var day = deadline
        while (day < today) {
            years++
            day = deadline.plus(DatePeriod(years = years))
        }
        return day
    }

    /** [left] split over [months], rounded up to the minor unit so the sum is never short. */
    private fun perMonth(left: Money, months: Int): Money {
        val minor = left.amountMinor / months + if (left.amountMinor % months == 0L) 0 else 1
        return Money(minor, left.currency)
    }
}
