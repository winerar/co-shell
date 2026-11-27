package app.coshell.core.domain.budgets

import app.coshell.core.model.Money

/** «Сегодня можно» (#769): what may be spent today to stay within the month, and what is already spent today. */
data class Allowance(
    /** For today, counted as of the start of the day: today's spending does not shrink it. Zero when the plan is spent. */
    val perDay: Money,
    /** What is left for the rest of the month after spending, regular payments and goals; negative — over the plan. */
    val left: Money,
    val spentToday: Money,
    /** Days left in the month, today included. */
    val days: Int,
) {
    val over: Boolean get() = left.isNegative
}

/**
 * The money of a month split over its days left (#769, safe-to-spend). [base] is the month's limits (or its income when there are no
 * limits); [spent] — spending of the month so far, today included; [upcoming] — regular payments due before the month ends; [goals] —
 * what to put aside for savings goals this month. Today's allowance is counted as if today's spending had not happened yet, so it
 * stays the same all day and «сегодня уже …» is measured against it. Rounded down: the days left never get more than there is.
 */
object DailyAllowance {
    fun of(base: Money, spent: Money, spentToday: Money, upcoming: Money, goals: Money, days: Int): Allowance {
        require(days >= 1) { "at least today is left" }
        val left = base - spent - upcoming - goals
        val fromMorning = left + spentToday
        val perDay = if (fromMorning.amountMinor <= 0) Money.zero(base.currency) else Money(fromMorning.amountMinor / days, base.currency)
        return Allowance(perDay, left, spentToday, days)
    }
}
