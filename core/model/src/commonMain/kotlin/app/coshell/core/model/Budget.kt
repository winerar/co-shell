package app.coshell.core.model

import kotlinx.datetime.YearMonth

/**
 * Monthly spending limit ("envelope") for a category or a folder, in its own currency.
 * It repeats every month from [startMonth] until [lastMonth]; changing the amount later adds
 * a new record from that month, so past months keep the limit they had.
 */
data class Budget(
    val id: String,
    /** A category, or a folder (a category that has subcategories) — then the limit covers all of them. */
    val categoryId: String,
    /** First month the limit applies to. */
    val startMonth: YearMonth,
    val limit: Money,
    /** Free text "for myself" shown next to the plan. */
    val note: String? = null,
    /** Carry what is left (or overspent) over to the next month. */
    val rollover: Boolean = false,
    /** Last month the limit applies to, inclusive; `null` — no end. Earlier than [startMonth] — never applies. */
    val lastMonth: YearMonth? = null,
) {
    init {
        require(limit.isPositive) { "Budget limit must be positive" }
        require(note == null || note.isNotBlank()) { "Budget note must be null or not blank" }
    }

    fun covers(month: YearMonth): Boolean = month >= startMonth && (lastMonth == null || month <= lastMonth)
}

/**
 * Stable id: one record per category and start month, so the same limit set on two phones
 * becomes one synced document instead of two.
 */
fun budgetId(categoryId: String, startMonth: YearMonth): String = "$categoryId:$startMonth"
