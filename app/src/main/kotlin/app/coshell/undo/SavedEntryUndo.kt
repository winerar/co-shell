package app.coshell.undo

import app.coshell.R
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.domain.shake.ShakeIntent
import app.coshell.core.domain.shake.ShakeIntents
import app.coshell.core.model.Transaction
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** What the entry sheet tells after it saved a **new** operation (edits never count, #325). */
fun interface SavedEntries {
    fun saved(items: List<Transaction>, amount: String)
}

/**
 * «Сохранено · 3 500 ARS · Отменить» for 4 s after a new operation, and the same window for a shake (#325, boards
 * `Shk325*`): a shake inside it takes the save back and says «Отменено · Вернуть». Only the operations of the last save
 * are ever touched, and only while their snackbar is still the newest offer ([UndoCenter.latest]) — a delete or another
 * undo in between closes the window, so a shake can never remove anything else.
 */
@Singleton
class SavedEntryUndo @Inject constructor(
    private val transactions: TransactionRepository,
    private val undo: UndoCenter,
    private val clock: Clock,
) : SavedEntries {
    private class Saved(val items: List<Transaction>, val amount: String, val at: Instant, val offer: UndoOffer)

    @Volatile
    private var last: Saved? = null
    private val _saves = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    /** One item per saved new operation — the card «Ошиблись? Встряхните…» waits for it (once, `hints.md`). */
    val saves: SharedFlow<Unit> = _saves.asSharedFlow()

    override fun saved(items: List<Transaction>, amount: String) {
        if (items.isEmpty()) return
        val offer = UndoOffer(R.string.entry_saved, listOf(amount), short = true, undo = { take(items) })
        last = Saved(items, amount, clock.now(), offer)
        undo.offer(offer)
        _saves.tryEmit(Unit)
    }

    /** What a shake means now: undo the save in the window, otherwise the usual (scanner). */
    fun intent(): ShakeIntent {
        val saved = last?.takeIf { undo.latest === it.offer }
        return ShakeIntents.of(clock.now(), saved?.at, undoEnabled = true)
    }

    /** The shake's undo: removes the last save and offers «Вернуть». */
    suspend fun undoLast() {
        val saved = last ?: return
        take(saved.items)
        undo.offer(UndoOffer(R.string.entry_undone, listOf(saved.amount), R.string.action_restore) { restore(saved.items) })
    }

    private suspend fun take(items: List<Transaction>) {
        last = null
        items.forEach { transactions.deleteTransaction(it.id) }
    }

    private suspend fun restore(items: List<Transaction>) = items.forEach { transactions.saveTransaction(it) }
}
