package app.coshell.undo

import androidx.annotation.StringRes
import app.coshell.R
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Something just done that can be taken back: shown as «[message] · Отменить» at the bottom.
 * [args] fill the message's placeholders («Удалено операций: %d»).
 */
class UndoOffer(
    @StringRes val message: Int,
    val args: List<Any> = emptyList(),
    /** The action word: «Отменить» by default, «Вернуть» for an undone save (#325). */
    @StringRes val actionLabel: Int = R.string.action_undo,
    /** Stays 4 s instead of 10 s: the «Сохранено» offer, which is also the shake-to-undo window (#325). */
    val short: Boolean = false,
    val undo: suspend () -> Unit,
)

/**
 * Undo instead of «Are you sure?» dialogs (design-questions §5): a screen does the action at once and
 * [offer]s a way back; `CoShellNavHost` shows the snackbar, so it survives closing an editor sheet.
 * Only the newest offer matters — an older one is dropped when a new one arrives.
 */
@Singleton
class UndoCenter @Inject constructor() {
    private val _offers = MutableSharedFlow<UndoOffer>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val offers: SharedFlow<UndoOffer> = _offers.asSharedFlow()

    /** The newest offer; [SavedEntryUndo] checks it is still the «Сохранено» one before a shake undoes a save. */
    @Volatile
    var latest: UndoOffer? = null
        private set

    fun offer(offer: UndoOffer) {
        latest = offer
        _offers.tryEmit(offer)
    }
}
