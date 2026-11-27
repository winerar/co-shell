package app.coshell.undo

import app.coshell.R
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.model.Transaction
import javax.inject.Inject
import javax.inject.Singleton

/** What the entry sheet tells after it saved an **edit** of an existing operation (#798). */
fun interface EditedEntries {
    fun edited(before: Transaction, after: Transaction)
}

/**
 * «Изменено · Отменить» after an edit of an existing operation (#798): undo saves the old row again, like
 * [app.coshell.transactions.DeleteTransactionWithUndo]; sync carries it to the other phone as any edit. Nothing is offered
 * when the sheet saved the same values. Unlike a new save it has no shake window ([SavedEntryUndo], #325).
 */
@Singleton
class EditedEntryUndo @Inject constructor(
    private val transactions: TransactionRepository,
    private val undo: UndoCenter,
) : EditedEntries {
    override fun edited(before: Transaction, after: Transaction) {
        if (before == after) return
        undo.offer(UndoOffer(R.string.entry_edited) { transactions.saveTransaction(before) })
    }
}
