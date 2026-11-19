package app.coshell.core.domain.hints

import app.coshell.core.domain.budgets.BudgetProgress

/** Trigger of the `limit_80` moment (`hints.md`): the limit nearest to its end among those 80–99 % spent. */
object LimitWatch {
    const val FROM_PERCENT = 80

    /** Not yet over: «ещё можно» must stay true. A limit with nothing available never counts. */
    fun nearest(progress: List<BudgetProgress>): BudgetProgress? = progress
        .filter { it.available.isPositive && it.left.isPositive && it.spent.amountMinor * 100 >= it.available.amountMinor * FROM_PERCENT }
        .minByOrNull { it.left.amountMinor * 1_000_000 / it.available.amountMinor }
}
