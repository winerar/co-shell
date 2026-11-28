package app.coshell.ui.edit

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalViewConfiguration
import kotlinx.coroutines.withTimeoutOrNull

// Hold a card to start editing a feed of cards: «Обзор» (#154) and «Отчёты» (#826).

/**
 * A long press anywhere on the card starts the editing; the card itself still gets its taps (the press is only watched).
 * Only a finger that stays still for the whole timeout counts: a scroll or a drag (the pointer moves, or another
 * gesture takes it) never starts the editing — that was the «editing is always on» bug.
 */
fun Modifier.longPressToEdit(enabled: Boolean, onLongPress: () -> Unit): Modifier = if (!enabled) this else composed {
    val timeout = LocalViewConfiguration.current.longPressTimeoutMillis
    pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            val slop = viewConfiguration.touchSlop
            // null only when the timeout ran out with the finger still; any lift, move or other gesture ends the wait.
            val ended = withTimeoutOrNull(timeout) {
                var still = true
                while (still) {
                    val change = awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull { it.id == down.id }
                    still = change != null && !change.changedToUpIgnoreConsumed() && !change.isConsumed &&
                        (change.position - down.position).getDistance() <= slop
                }
            }
            if (ended == null) onLongPress()
        }
    }
}
