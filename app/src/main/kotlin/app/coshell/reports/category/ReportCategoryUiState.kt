package app.coshell.reports.category

import app.coshell.core.domain.reports.ReportPeriod
import app.coshell.reports.ReportsUiState.Bar
import app.coshell.transactions.list.TransactionsUiState.Day

/**
 * «Из чего сложилось» (board `RepDrill`, #188): «Дом за ноябрь · 290 USD · 38% трат», the subcategories with their
 * shares and the period's operations by day. Amounts of the head and the bars are in the report [currency]; the
 * operations keep their own currency.
 */
data class ReportCategoryUiState(
    val isLoading: Boolean = false,
    /** `null` — a category deleted on another phone while its operations stayed. */
    val name: String? = null,
    val period: ReportPeriod? = null,
    val currency: String = "",
    /** «290» and «38» (% of all spending); `null` — the category spent no more than it got back. */
    val total: String? = null,
    val percent: Int = 0,
    /** Subcategories (and the category's own operations under its name), largest first; empty for a plain category. */
    val parts: List<Bar> = emptyList(),
    val days: List<Day> = emptyList(),
    /** Currencies left out of the head for lack of a rate. */
    val missing: List<String> = emptyList(),
) {
    val operationCount: Int get() = days.sumOf { it.rows.size }
}
