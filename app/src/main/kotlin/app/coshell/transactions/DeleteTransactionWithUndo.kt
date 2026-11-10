package app.coshell.transactions

import app.coshell.R
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.model.Transaction
import app.coshell.undo.UndoCenter
import app.coshell.undo.UndoOffer
import javax.inject.Inject

/**
 * Deletes transactions at once (no confirmation) and offers «Отменить» — used by the list's selection
 * mode and the editor. Undo saves the same transactions again: the soft-deleted rows come back, and sync
 * carries the restore to the other phone like any edit. One offer for the whole batch.
 */
class DeleteTransactionWithUndo @Inject constructor(
    private val transactions: TransactionRepository,
    private val undo: UndoCenter,
) {
    suspend operator fun invoke(id: String) = invoke(listOf(id))

    suspend operator fun invoke(ids: Collection<String>) {
        val deleted = ids.mapNotNull { transactions.getTransaction(it) }
        if (deleted.isEmpty()) return
        deleted.forEach { transactions.deleteTransaction(it.id) }
        val offer = if (deleted.size == 1) UndoOffer(R.string.transaction_deleted, undo = { restore(deleted) })
        else UndoOffer(R.string.transactions_deleted_many, listOf(deleted.size)) { restore(deleted) }
        undo.offer(offer)
    }

    private suspend fun restore(deleted: List<Transaction>) = deleted.forEach { transactions.saveTransaction(it) }
}
