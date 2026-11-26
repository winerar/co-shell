package app.coshell.core.domain.goals

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Goal
import app.coshell.core.model.Money
import kotlinx.datetime.LocalDate

/** A goal with where it stands, as the screens show it (#768). */
data class GoalLine(val goal: Goal, val progress: GoalProgress)

/**
 * Savings goals over account balances (#768, poll «А»: saved = the balance of the goal's account).
 * Goals on one account share it in order of their (current) deadline, nearest first, goals without a deadline last:
 * each takes up to its target, the next one gets what is left. A balance in another currency than the goal is converted
 * by [convert] (no rate — nothing saved). Lines come nearest deadline first, reached goals last.
 */
object GoalBook {
    fun of(
        goals: List<Goal>,
        balances: Map<String, Money>,
        today: LocalDate,
        convert: (Money, CurrencyCode) -> Money? = { m, c -> m.takeIf { it.currency == c } },
    ): List<GoalLine> {
        val left = mutableMapOf<String, Long>()
        val lines = goals.sortedWith(order(today)).map { goal ->
            val currency = goal.target.currency
            val pool = left.getOrPut(goal.accountId) {
                balances[goal.accountId]?.let { convert(it, currency)?.amountMinor } ?: 0L
            }
            val take = pool.coerceIn(0L, goal.target.amountMinor)
            left[goal.accountId] = pool - take
            GoalLine(goal, GoalPlan.of(goal.target, Money(take, currency), goal.deadline, goal.yearly, today))
        }
        return lines.sortedBy { it.progress.reached }
    }

    /** The day the goal is counted to now: a yearly one moves on to its next round. */
    fun currentDeadline(goal: Goal, today: LocalDate): LocalDate? =
        goal.deadline?.let { if (goal.yearly) GoalPlan.nextRound(it, today) else it }

    private fun order(today: LocalDate) = compareBy<Goal>(
        { currentDeadline(it, today) == null },
        { currentDeadline(it, today) },
        { it.name.lowercase() },
        { it.id },
    )
}
