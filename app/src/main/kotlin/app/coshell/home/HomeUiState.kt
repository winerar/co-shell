package app.coshell.home

import kotlinx.datetime.LocalDate

data class HomeUiState(
    val isLoading: Boolean = false,
    /** Total of all accounts in each report currency, main first, e.g. ["125 300,00 USD", "180 000 000 ARS"]. */
    val totals: List<String> = emptyList(),
    /** Oldest rate behind the totals ("по курсу на 9 ноября"); `null` when nothing was converted. */
    val rateDate: LocalDate? = null,
    /** Currencies left out of the totals for lack of any rate. */
    val missing: List<String> = emptyList(),
    /** Plain sums per account currency — the breakdown under the totals (only when there are several). */
    val byCurrency: List<String> = emptyList(),
    val accounts: List<AccountRow> = emptyList(),
) {
    data class AccountRow(val id: String, val name: String, val balance: String, val isNegative: Boolean)
}
