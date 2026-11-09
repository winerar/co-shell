package app.coshell.core.designsystem.motion

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin

// Screens and sheets grow out of the element that opened them (issue #63, owner's decision): every tactile
// element records where the finger was, and the next screen or sheet takes that point as its origin.

/** A tap in window coordinates; [color] — the pressed element's fill when it should flow into what opens (the «+»). */
@Immutable
data class PressPoint(val position: Offset, val color: Color = Color.Unspecified)

/**
 * The last tap, kept for a moment ([MAX_AGE_MS]): a tap that opened nothing must not shape a later
 * transition (shake, a system back). Used from the main thread only. [clockMs] — for tests.
 */
class PressOriginStore(private val clockMs: () -> Long = { System.nanoTime() / NANOS_IN_MS }) {
    private var last: PressPoint? = null
    private var atMs = 0L

    fun record(point: PressPoint) {
        last = point
        atMs = clockMs()
    }

    /** Forget the tap (the press turned into a scroll). */
    fun clear() {
        last = null
    }

    /** The recent tap, once: one tap opens one thing. */
    fun take(): PressPoint? {
        val point = last ?: return null
        last = null
        return point.takeIf { clockMs() - atMs <= MAX_AGE_MS }
    }

    companion object {
        const val MAX_AGE_MS = 1_000L
        private const val NANOS_IN_MS = 1_000_000L
    }
}

/** The app's one store: written by `Modifier.tactile`, read by screen transitions and `CoShellSheet`. */
val PressOrigin = PressOriginStore()

/** [point] as a pivot inside [bounds] (fractions 0…1, clamped): a screen or sheet scales around it. Pure. */
fun transformOriginIn(bounds: Rect, point: Offset): TransformOrigin {
    if (bounds.width <= 0f || bounds.height <= 0f) return TransformOrigin.Center
    return TransformOrigin(
        ((point.x - bounds.left) / bounds.width).coerceIn(0f, 1f),
        ((point.y - bounds.top) / bounds.height).coerceIn(0f, 1f),
    )
}
