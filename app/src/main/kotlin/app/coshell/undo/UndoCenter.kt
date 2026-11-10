package app.coshell.undo

import androidx.annotation.StringRes
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
class UndoOffer(@StringRes val message: Int, val args: List<Any> = emptyList(), val undo: suspend () -> Unit)

/**
 * Undo instead of «Are you sure?» dialogs (design-questions §5): a screen does the action at once and
 * [offer]s a way back; `CoShellNavHost` shows the snackbar, so it survives closing an editor sheet.
 * Only the newest offer matters — an older one is dropped when a new one arrives.
 */
@Singleton
class UndoCenter @Inject constructor() {
    private val _offers = MutableSharedFlow<UndoOffer>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val offers: SharedFlow<UndoOffer> = _offers.asSharedFlow()

    fun offer(offer: UndoOffer) {
        _offers.tryEmit(offer)
    }
}
