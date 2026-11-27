package app.coshell.transactions.list

import app.coshell.core.domain.search.DuplicatePair
import app.coshell.transactions.list.TransactionsUiState.Day
import app.coshell.transactions.list.TransactionsUiState.RowDuplicate

/** Marks the rows of [pairs] (row id → its pair, `duplicatePairs`) «похоже на дубль» (#791); the rest stay as they are. */
internal fun List<Day>.withDuplicates(pairs: Map<String, DuplicatePair>): List<Day> {
    if (pairs.isEmpty()) return this
    return map { day ->
        if (day.rows.none { it.id in pairs }) day
        else day.copy(rows = day.rows.map { row -> pairs[row.id]?.let { row.copy(duplicate = RowDuplicate(it.key, it.first, it.second)) } ?: row })
    }
}
