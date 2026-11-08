package app.coshell.core.model

import kotlinx.datetime.YearMonth

/** Monthly spending limit for a category, in its own currency. */
data class Budget(
    val id: String,
    val categoryId: String,
    val month: YearMonth,
    val limit: Money,
) {
    init {
        require(limit.isPositive) { "Budget limit must be positive" }
    }
}
