package app.coshell.accounts.list

import app.coshell.core.model.AccountType

data class AccountsUiState(
    val isLoading: Boolean = false,
    val active: List<Row> = emptyList(),
    val archived: List<Row> = emptyList(),
    val showArchived: Boolean = false,
) {
    data class Row(
        val id: String,
        val name: String,
        val type: AccountType,
        val balance: String,
        val isNegative: Boolean,
        /** Left out of the total balance on «Обзор». */
        val excludedFromTotal: Boolean = false,
        /** Personal account: `""` — mine, a name — the other member's; `null` — shared. */
        val personalOf: String? = null,
        /** The account's colour (stored palette value), `null` — none. */
        val color: Long? = null,
    )
}
