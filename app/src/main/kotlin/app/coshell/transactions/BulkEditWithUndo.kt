package app.coshell.transactions

import app.coshell.R
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.domain.bulk.BulkEdit
import app.coshell.core.domain.bulk.BulkEditTransactionsUseCase
import app.coshell.core.model.Transaction
import app.coshell.undo.UndoCenter
import app.coshell.undo.UndoOffer
import javax.inject.Inject

/**
 * «Изменить» in the list's selection mode (#794): applies one [BulkEdit] at once and offers «Отменить» for the whole batch.
 * Undo saves the old rows again, like [DeleteTransactionWithUndo]; sync carries it to the other phone as any edit.
 */
class BulkEditWithUndo @Inject constructor(
    private val transactions: TransactionRepository,
    categories: CategoryRepository,
    private val undo: UndoCenter,
) {
    private val useCase = BulkEditTransactionsUseCase(categories, transactions)

    suspend operator fun invoke(ids: Collection<String>, edit: BulkEdit): BulkEditTransactionsUseCase.Result {
        val result = useCase(ids, edit)
        if (result is BulkEditTransactionsUseCase.Result.Changed) {
            undo.offer(UndoOffer(R.string.transactions_changed_many, listOf(result.count)) { restore(result.before) })
        }
        return result
    }

    private suspend fun restore(before: List<Transaction>) = before.forEach { transactions.saveTransaction(it) }
}
