package app.coshell.reports.recap

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import app.coshell.core.designsystem.tokens.Sizes
import kotlin.math.abs

/**
 * Where a touch on a story card goes (#554, #598, #693): a tap on the left third — back, elsewhere — on; a swipe to the right — back,
 * to the left — on (like stories everywhere). The card keeps its own tactile click; this only tells it which way.
 */
internal const val BACK_ZONE = 1f / 3f

/** −1 back, +1 on: a swipe of at least [swipe] wins over where the finger went down. Pure. */
internal fun storyStep(downX: Float, dragX: Float, width: Float, swipe: Float): Int = when {
    dragX >= swipe -> -1
    dragX <= -swipe -> 1
    downX < width * BACK_ZONE -> -1
    else -> 1
}

/** The step of the last tap, read by the card's `onClick` (TalkBack's click has no position — it goes on). */
internal class StoryTap {
    var step = 1
}

/**
 * Watches the touch before the card does: a tap only sets [tap] (the card's click follows), a swipe is consumed (no click)
 * and goes straight to [onSwipe].
 */
internal fun Modifier.storyTaps(tap: StoryTap, onSwipe: (Int) -> Unit): Modifier = pointerInput(tap) {
    val swipe = Sizes.tabSwipe.toPx()
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        tap.step = 1
        while (true) {
            val change = awaitPointerEvent(PointerEventPass.Initial).changes.firstOrNull { it.id == down.id } ?: break
            if (change.pressed) continue
            val dx = change.position.x - down.position.x
            val step = storyStep(down.position.x, dx, size.width.toFloat(), swipe)
            if (abs(dx) >= swipe) {
                change.consume()
                onSwipe(step)
            } else {
                tap.step = step
            }
            break
        }
    }
}
