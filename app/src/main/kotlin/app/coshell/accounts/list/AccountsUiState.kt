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
    )
}
