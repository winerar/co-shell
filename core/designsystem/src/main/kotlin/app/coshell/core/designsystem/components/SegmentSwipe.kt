package app.coshell.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Sizes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

// Segments flip with a swipe over the content under them (owner 2026-11-12, #173, scheme А): the drag
// first moves between the segments; outward from the first or last segment it is not claimed, so the
// bottom tabs' swipe (19В) takes it. A drag steeper than 30° is a scroll and is left to the list.

/** Steepest drag, as dy / dx, that still counts as a swipe: tan 30°. */
private const val MAX_SLOPE = 0.577f
/** How far the content leans after the finger, like the bottom tabs' swipe. */
private const val FOLLOW = 0.25f

/**
 * Whether a drag of ([dx], [dy]) px that just passed the touch slop on segment [current] of [count] is a
 * segment swipe: mostly horizontal (within 30°) and towards an existing neighbour — or anywhere with
 * [atEdges] (the track keeps its drag: handing it to the bottom tabs would flip them backwards). Pure.
 */
fun claimsSegmentSwipe(dx: Float, dy: Float, current: Int, count: Int, atEdges: Boolean = false): Boolean {
    if (dx == 0f || abs(dy) > abs(dx) * MAX_SLOPE) return false
    return atEdges || segmentSwipeTarget(current, dx, threshold = 0f, count) != null
}

/**
 * Segment to select after a horizontal drag of [drag] px on segment [current] of [count]: to the left —
 * the next one, to the right — the previous one; shorter than [threshold] or past the ends — `null`. Pure.
 */
fun segmentSwipeTarget(current: Int, drag: Float, threshold: Float, count: Int): Int? {
    if (drag == 0f || abs(drag) < threshold) return null
    val target = if (drag < 0) current + 1 else current - 1
    return target.takeIf { it in 0 until count }
}

/** Shared between [segmentSwipe] (the content) and [CoShellSegmented] (the pill that follows the finger). */
@Stable
class SegmentSwipe internal constructor(internal val threshold: Float) {
    internal val drag = Animatable(0f)

    /** Pill shift in segments: halfway to the neighbour at the switch threshold. */
    internal val pillShift: Float get() = (-drag.value / (threshold * 2)).coerceIn(-1f, 1f)
}

@Composable
fun rememberSegmentSwipe(): SegmentSwipe {
    val threshold = with(LocalDensity.current) { Sizes.tabSwipe.toPx() }
    return remember(threshold) { SegmentSwipe(threshold) }
}

/**
 * Lets the content under a [CoShellSegmented] switch segments with a swipe; pass the same [swipe] to the
 * segments so their pill follows the finger. [selected] of [count] — the shown segment.
 */
fun Modifier.segmentSwipe(
    swipe: SegmentSwipe,
    selected: Int,
    count: Int,
    onSelect: (Int) -> Unit,
    enabled: Boolean = true,
): Modifier = swipeGesture(swipe, selected, count, onSelect, enabled, lean = true)

/**
 * The gesture itself. [lean]: the content follows the finger and a drag to the left opens the next segment,
 * like a page; on the track (`lean = false`) the finger drags the pill, so a drag to the right opens the next.
 */
internal fun Modifier.swipeGesture(
    swipe: SegmentSwipe,
    selected: Int,
    count: Int,
    onSelect: (Int) -> Unit,
    enabled: Boolean,
    lean: Boolean,
): Modifier = composed {
    val scope = rememberCoroutineScope()
    val sign = if (lean) 1f else -1f
    val select by rememberUpdatedState(onSelect)
    val gesture = if (!enabled || count < 2) Modifier else Modifier.pointerInput(selected, count) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            var total = Offset.Zero
            // Touch slop: whoever claims first wins — a list scrolling vertically, a row scrolling sideways.
            while (true) {
                val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: return@awaitEachGesture
                if (!change.pressed || change.isConsumed) return@awaitEachGesture
                total += change.positionChange() * sign
                if (total.getDistance() < viewConfiguration.touchSlop) continue
                if (!claimsSegmentSwipe(total.x, total.y, selected, count, atEdges = !lean)) return@awaitEachGesture
                change.consume()
                break
            }
            var drag = 0f
            val completed = horizontalDrag(down.id) { change ->
                drag += change.positionChange().x * sign
                change.consume()
                val edge = segmentSwipeTarget(selected, drag, 0f, count) == null
                scope.launch { swipe.drag.snapTo(if (edge) 0f else drag) }
            }
            if (completed) segmentSwipeTarget(selected, drag, swipe.threshold, count)?.let(select)
            scope.settle(swipe)
        }
    }
    (if (lean) graphicsLayer { translationX = swipe.drag.value * FOLLOW } else Modifier).then(gesture)
}

private fun CoroutineScope.settle(swipe: SegmentSwipe) {
    launch { swipe.drag.animateTo(0f, Motion.spatial()) }
}
