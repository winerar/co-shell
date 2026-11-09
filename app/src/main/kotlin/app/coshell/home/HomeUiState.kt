package app.coshell.home

import kotlinx.datetime.LocalDate

data class HomeUiState(
    val isLoading: Boolean = false,
    /** Total of all accounts (except excluded ones) in each report currency, main first, e.g. ["125 300,00 USD", "180 000 000 ARS"]. */
    val totals: List<String> = emptyList(),
    /** Oldest rate behind the totals ("по курсу на 9 ноября"); `null` when nothing was converted. */
    val rateDate: LocalDate? = null,
    /** Currencies left out of the totals for lack of any rate. */
    val missing: List<String> = emptyList(),
    /** Plain sums per account currency — the breakdown under the totals (only when there are several). */
    val byCurrency: List<String> = emptyList(),
    /** This month's expenses; `null` while loading. */
    val spending: MonthSpending? = null,
    val accounts: List<AccountRow> = emptyList(),
) {
    /**
     * Expenses of the current month in each report currency, main first; each expense at the rate of its day.
     * @property month first day of the month ("Траты за ноябрь").
     * @property missing currencies of expenses left out for lack of any rate.
     * @property converted some expense is in another currency than a report one (show the rate hint).
     */
    data class MonthSpending(
        val month: LocalDate,
        val totals: List<String>,
        val missing: List<String>,
        val converted: Boolean = false,
    )

    /** @property excludedFromTotal the account is left out of [totals] ("не в общей сумме"). */
    data class AccountRow(
        val id: String,
        val name: String,
        val balance: String,
        val isNegative: Boolean,
        val excludedFromTotal: Boolean = false,
    )
}
