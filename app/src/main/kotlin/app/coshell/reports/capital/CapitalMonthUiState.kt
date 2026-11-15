package app.coshell.reports.capital

import app.coshell.accounts.list.AccountsUiState
import app.coshell.reports.ReportsUiState.CapitalPoint
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/**
 * «Капитал · октябрь» (#373, board `Rp189NwDrill`): the year line with the [selected] month end, what capital was then
 * ([value], [change] since the month before) and the accounts that made it up — in groups by type or flat by amount or
 * by change. Amounts are formatted; the report [currency] is shown once, next to [value].
 */
data class CapitalMonthUiState(
    val isLoading: Boolean = false,
    val currency: String = "",
    val points: List<CapitalPoint> = emptyList(),
    val selected: Int = -1,
    /** The selected month and its last day (today while it runs). */
    val month: YearMonth? = null,
    val asOf: LocalDate? = null,
    val value: String = "",
    /** «+260» since the month before; `null` — the first month of the line. */
    val change: String? = null,
    val falling: Boolean = false,
    /** Accounts at that day before the search («Найти счёт · 24»). */
    val count: Int = 0,
    val query: String = "",
    val order: CapitalOrder = CapitalOrder.GROUPS,
    /** [CapitalOrder.GROUPS]: by account type, foldable. */
    val groups: List<AccountsUiState.Group> = emptyList(),
    /** [CapitalOrder.AMOUNT] and [CapitalOrder.CHANGE]: one list. */
    val rows: List<AccountsUiState.Row> = emptyList(),
    /** Currencies left out for lack of a rate: «без учёта: AMD». */
    val missing: List<String> = emptyList(),
) {
    /** Nothing to show for this month: no account had money on it. */
    val isEmpty: Boolean get() = !isLoading && count == 0
}

/** «Группами · По сумме · По изменению». */
enum class CapitalOrder { GROUPS, AMOUNT, CHANGE }
