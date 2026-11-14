package app.coshell.core.designsystem.motion

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * «Hold to peek» (#318): a long press on this element calls [onPeek] with a light buzz, and lifting the finger
 * calls [onRelease] — the hint lives exactly as long as the finger. A short tap calls [onTap] (the element's
 * own row still opens); moving past the touch slop before the long press gives the gesture back to the list
 * (scroll, tab swipe). It takes the press from its parent, so a row's own long press (selection mode) does not
 * fire on this part of the row; pass [enabled] = false to hand everything back (e.g. while choosing rows).
 */
fun Modifier.holdToPeek(
    enabled: Boolean,
    onTap: () -> Unit,
    onPeek: () -> Unit,
    onRelease: () -> Unit,
): Modifier = if (!enabled) this else composed {
    val haptics = LocalHapticFeedback.current
    val tap by rememberUpdatedState(onTap)
    val peek by rememberUpdatedState(onPeek)
    val release by rememberUpdatedState(onRelease)
    pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown()
            down.consume()
            // Up, slop or the long-press timeout — whichever comes first.
            val early = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) { awaitTapOrSlop(down) }
            when (early) {
                Early.Tap -> tap()
                Early.Cancel -> Unit
                null -> {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    peek()
                    try {
                        // Hold everything (no scroll, no tab swipe) until the last finger is up.
                        do {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            event.changes.forEach { it.consume() }
                        } while (event.changes.any { it.pressed })
                    } finally {
                        release()
                    }
                }
            }
        }
    }
}

private enum class Early { Tap, Cancel }

/** Waits for the finger to lift ([Early.Tap]) or to move past the touch slop ([Early.Cancel]). */
private suspend fun AwaitPointerEventScope.awaitTapOrSlop(down: PointerInputChange): Early {
    while (true) {
        val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: return Early.Cancel
        if (!change.pressed) {
            change.consume()
            return Early.Tap
        }
        if ((change.position - down.position).getDistance() > viewConfiguration.touchSlop) return Early.Cancel
    }
}
