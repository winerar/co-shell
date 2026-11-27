package app.coshell.core.domain.bulk

import app.coshell.core.model.CategoryType
import app.coshell.core.model.TagIds
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType

/** What «Изменить» in the list's selection mode does to every chosen operation (#794). */
sealed interface BulkEdit {
    /** Puts all of them into one category. */
    data class SetCategory(val categoryId: String) : BulkEdit

    /** Adds tags (references: ids or names) to what they already have; none is taken away. */
    data class AddTags(val tags: List<String>) : BulkEdit
}

/** Pure rules of [BulkEdit]: which edits fit a selection and what a row becomes. */
object BulkEditRules {
    /**
     * The category type the whole selection can take, or `null` when «Категория» must stay off: the rows are of
     * different types (expense and income), or a transfer is among them, or nothing is chosen.
     */
    fun categoryType(rows: List<Transaction>): CategoryType? {
        val type = rows.map { it.type }.distinct().singleOrNull() ?: return null
        return when (type) {
            TransactionType.EXPENSE -> CategoryType.EXPENSE
            TransactionType.INCOME -> CategoryType.INCOME
            TransactionType.TRANSFER -> null
        }
    }

    /** The row after [edit]; the same instance when nothing would change. */
    fun apply(row: Transaction, edit: BulkEdit): Transaction = when (edit) {
        is BulkEdit.SetCategory -> if (row.categoryId == edit.categoryId) row else row.copy(categoryId = edit.categoryId)
        is BulkEdit.AddTags -> {
            val have = TagIds.resolve(row.labels).toSet()
            val extra = edit.tags.filter { TagIds.of(it).let { id -> id.isNotEmpty() && id !in have } }
                .distinctBy { TagIds.of(it) }
            if (extra.isEmpty()) row else row.copy(labels = row.labels + extra)
        }
    }
}
