package app.coshell.core.domain.budgets

import app.coshell.core.model.Budget
import kotlinx.datetime.YearMonth

/**
 * The limit of each category in [month]: among the records started by then the latest one wins,
 * and only if it has not ended. An ended record hides the older ones — that is how a limit is removed
 * "from this month on" without touching the past.
 */
fun List<Budget>.inMonth(month: YearMonth): List<Budget> =
    filter { it.startMonth <= month }
        .groupBy { it.categoryId }
        .mapNotNull { (_, records) -> records.latest().takeIf { it.covers(month) } }

fun List<Budget>.forCategory(categoryId: String, month: YearMonth): Budget? =
    filter { it.categoryId == categoryId }.inMonth(month).singleOrNull()

/** Ties (records made before ids became stable) are broken by id, the same way on every phone. */
private fun List<Budget>.latest(): Budget = maxWith(compareBy<Budget> { it.startMonth }.thenBy { it.id })
