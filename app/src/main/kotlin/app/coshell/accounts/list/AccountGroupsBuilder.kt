package app.coshell.accounts.list

import app.coshell.R
import app.coshell.accounts.AccountTypeGroupOrder
import app.coshell.accounts.groupLabelRes

/**
 * Splits [rows] into the groups of the accounts list for [grouping]; [collapsed] holds keys of folded groups,
 * [totalOf] sums a group in the main currency. Pure — tested in `AccountGroupsBuilderTest`.
 */
internal fun buildAccountGroups(
    rows: List<AccountsUiState.Row>,
    grouping: AccountGrouping,
    collapsed: Set<String>,
    totalOf: (List<AccountsUiState.Row>) -> String?,
): List<AccountsUiState.Group> {
    fun group(key: String, items: List<AccountsUiState.Row>, titleRes: Int? = null, titleText: String? = null) =
        AccountsUiState.Group(
            key = "${grouping.name}:$key",
            titleRes = titleRes,
            titleText = titleText,
            rows = items,
            total = totalOf(items),
            expanded = "${grouping.name}:$key" !in collapsed,
        )
    return when (grouping) {
        AccountGrouping.TYPE -> {
            val byType = rows.groupBy { it.type }
            AccountTypeGroupOrder.mapNotNull { type -> byType[type]?.let { group(type.name, it, titleRes = type.groupLabelRes) } }
        }
        AccountGrouping.CURRENCY ->
            // Currencies with more accounts first, then by code.
            rows.groupBy { it.currency }.entries
                .sortedWith(compareByDescending<Map.Entry<String, List<AccountsUiState.Row>>> { it.value.size }.thenBy { it.key })
                .map { (code, items) -> group(code, items, titleText = code) }
        AccountGrouping.OWNER -> {
            val shared = rows.filter { it.personalOf == null }
            val mine = rows.filter { it.personalOf == "" }
            val others = rows.filter { !it.personalOf.isNullOrEmpty() }.groupBy { it.personalOf!! }
            listOfNotNull(
                shared.takeIf { it.isNotEmpty() }?.let { group("shared", it, titleRes = R.string.accounts_group_shared) },
                mine.takeIf { it.isNotEmpty() }?.let { group("mine", it, titleRes = R.string.accounts_group_mine) },
            ) + others.map { (name, items) -> group("of:$name", items, titleText = name) }
        }
    }
}
