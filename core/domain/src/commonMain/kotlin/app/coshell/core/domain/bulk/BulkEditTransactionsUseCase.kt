package app.coshell.core.domain.bulk

import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.model.Transaction

/**
 * Applies one [BulkEdit] to the chosen operations (#794). Nothing is written unless the whole edit is allowed;
 * rows it would not change (already in that category, already tagged) are left alone and not counted.
 * [Result.Changed.before] is what undo writes back.
 */
class BulkEditTransactionsUseCase(
    private val categories: CategoryRepository,
    private val transactions: TransactionRepository,
) {
    sealed interface Result {
        data class Changed(val before: List<Transaction>) : Result {
            val count: Int get() = before.size
        }

        /** Every chosen row already looks like that. */
        data object NothingToChange : Result

        /** The category is missing, or does not fit the type of the rows. */
        data object NotAllowed : Result
    }

    suspend operator fun invoke(ids: Collection<String>, edit: BulkEdit): Result {
        val rows = ids.mapNotNull { transactions.getTransaction(it) }
        if (rows.isEmpty()) return Result.NothingToChange
        if (edit is BulkEdit.SetCategory) {
            val allowed = BulkEditRules.categoryType(rows)
            val category = categories.getCategory(edit.categoryId)
            if (allowed == null || category == null || category.archived || category.type != allowed) return Result.NotAllowed
        }
        val changed = rows.map { it to BulkEditRules.apply(it, edit) }.filter { (old, new) -> old != new }
        if (changed.isEmpty()) return Result.NothingToChange
        changed.forEach { (_, new) -> transactions.saveTransaction(new) }
        return Result.Changed(changed.map { it.first })
    }
}
