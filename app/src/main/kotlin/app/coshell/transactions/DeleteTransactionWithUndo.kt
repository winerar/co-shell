package app.coshell.transactions

import app.coshell.R
import app.coshell.core.domain.TransactionRepository
import app.coshell.undo.UndoCenter
import app.coshell.undo.UndoOffer
import javax.inject.Inject

/**
 * Deletes a transaction at once (no confirmation) and offers «Отменить» — used by the list swipe
 * and the editor. Undo saves the same transaction again: the soft-deleted row comes back, and sync
 * carries the restore to the other phone like any edit.
 */
class DeleteTransactionWithUndo @Inject constructor(
    private val transactions: TransactionRepository,
    private val undo: UndoCenter,
) {
    suspend operator fun invoke(id: String) {
        val transaction = transactions.getTransaction(id) ?: return
        transactions.deleteTransaction(id)
        undo.offer(UndoOffer(R.string.transaction_deleted) { transactions.saveTransaction(transaction) })
    }
}
