package app.coshell.accounts.list

import app.coshell.core.model.AccountType
import app.coshell.core.model.Money

data class AccountsUiState(
    val isLoading: Boolean = false,
    /** Every active account, in the user's order. */
    val active: List<Row> = emptyList(),
    val archived: List<Row> = emptyList(),
    val showArchived: Boolean = false,
    /** Accounts in the total, in the main report currency: «≈ 9 640 USD»; `null` — no rates yet. */
    val total: String? = null,
    val grouping: AccountGrouping = AccountGrouping.TYPE,
    /** «Чьи» makes sense only with other family members. */
    val canGroupByOwner: Boolean = false,
    val groups: List<Group> = emptyList(),
    val query: String = "",
    val searchOpen: Boolean = false,
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
        /** The account's colour (stored palette value or a bank preset), `null` — none. */
        val color: Long? = null,
        /** Balance in the main report currency when it differs: «≈ 1 220 USD». */
        val converted: String? = null,
        val currency: String = "",
        internal val money: Money? = null,
    )

    /** A foldable group of the list with its total; [key] is stable across recompositions. */
    data class Group(
        val key: String,
        val titleRes: Int? = null,
        val titleText: String? = null,
        val rows: List<Row>,
        val total: String?,
        val expanded: Boolean,
    )
}

enum class AccountGrouping { TYPE, CURRENCY, OWNER }
