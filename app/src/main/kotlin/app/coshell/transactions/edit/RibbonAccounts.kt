package app.coshell.transactions.edit

import app.coshell.transactions.edit.TransactionEditUiState.AccountOption

/** Badges in the entry-sheet ribbon: five, the rest are behind «Все ›». */
internal const val RIBBON_SIZE = 5

/**
 * Which accounts the ribbon shows (#505): the recently used ones in that order, topped up with the first others; the
 * chosen account is always among them (it takes the last place when it is not recent). [excludeId] — the source of a transfer.
 */
internal fun ribbonAccounts(
    accounts: List<AccountOption>,
    recentIds: List<String>,
    selectedId: String?,
    excludeId: String? = null,
    size: Int = RIBBON_SIZE,
): List<AccountOption> {
    val pool = accounts.filter { it.id != excludeId }
    val byId = pool.associateBy { it.id }
    val picked = LinkedHashMap<String, AccountOption>()
    recentIds.mapNotNull(byId::get).forEach { picked.putIfAbsent(it.id, it) }
    pool.forEach { picked.putIfAbsent(it.id, it) }
    val shown = picked.values.take(size).toMutableList()
    val chosen = selectedId?.let(byId::get)
    if (chosen != null && shown.none { it.id == chosen.id }) {
        if (shown.size == size) shown.removeAt(size - 1)
        shown.add(chosen)
    }
    return shown
}
