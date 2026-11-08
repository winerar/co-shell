package app.coshell.accounts

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
    )
}
