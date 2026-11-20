package app.coshell.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Radii
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * Manual order by dragging (#338, boards `Fb338Acc`, `Fb338Cat`): hold a row and drag it; the others make room.
 * Dropped on the middle of a row that [CoShellReorderColumn]'s `canNest` allows — the row is a drop target (dashed
 * frame) and the drop goes «into» it (a category becomes a subcategory). The list is short (accounts, categories),
 * so it is a scrolling [Column], not lazy; near the edges it scrolls by itself.
 */
@Stable
class ReorderState internal constructor(internal val scroll: ScrollState) {
    /** The key being dragged; `null` — none. */
    var dragging: Any? by mutableStateOf(null)
        internal set
    internal var offset by mutableFloatStateOf(0f)
    internal val tops = mutableStateMapOf<Any, Float>()
    internal val heights = mutableStateMapOf<Any, Float>()
    internal var keys: List<Any> by mutableStateOf(emptyList())
    internal var gap = 0f
    internal var canNest: (Any, Any) -> Boolean = { _, _ -> false }
    internal var onDrop: (ReorderDrop) -> Unit = {}

    private val center: Float
        get() = dragging?.let { (tops[it] ?: 0f) + (heights[it] ?: 0f) / 2 + offset } ?: 0f

    /** Rows other than the dragged one, in list order. */
    private val others: List<Any> get() = keys.filter { it != dragging }

    /** The row the dragged one would drop into; `null` — it goes between rows. */
    val over: Any?
        get() {
            val moving = dragging ?: return null
            val c = center
            return others.firstOrNull { key ->
                val top = tops[key] ?: return@firstOrNull false
                val h = heights[key] ?: return@firstOrNull false
                c > top + h * (1 - NEST_ZONE) / 2 && c < top + h * (1 + NEST_ZONE) / 2 && canNest(moving, key)
            }
        }

    /** Where the dragged row would land among the other rows (0 — first). */
    val target: Int
        get() = if (dragging == null) 0 else others.count { key -> (tops[key] ?: 0f) + (heights[key] ?: 0f) / 2 < center }

    /** How far the row [key] steps aside to make room for the dragged one. */
    internal fun shift(key: Any): Float {
        val moving = dragging ?: return 0f
        if (key == moving || over != null) return 0f
        val from = keys.indexOf(moving)
        val at = keys.indexOf(key)
        val j = others.indexOf(key)
        val room = (heights[moving] ?: 0f) + gap
        val t = target
        return when {
            at < from && j >= t -> room
            at > from && j < t -> -room
            else -> 0f
        }
    }

    internal companion object {
        /** The middle part of a row that means «into it». */
        const val NEST_ZONE = 0.4f
    }
}

/** Where a drag ended: [key] goes to [index] among the other rows, or into the row [into]. */
data class ReorderDrop(val key: Any, val index: Int, val into: Any?)

@Composable
fun rememberReorderState(scroll: ScrollState = rememberScrollState()): ReorderState = remember(scroll) { ReorderState(scroll) }

/**
 * The scrolling column of a «Порядок» list: [keys] — the keys of its [CoShellReorderRow]s in order, [spacing] — the gap
 * between them; [onDrop] — a drag ended somewhere new; [canNest] — whether the dragged key may go into another one.
 */
@Composable
fun CoShellReorderColumn(
    state: ReorderState,
    keys: List<Any>,
    onDrop: (ReorderDrop) -> Unit,
    modifier: Modifier = Modifier,
    canNest: (dragged: Any, into: Any) -> Boolean = { _, _ -> false },
    spacing: Dp = Spacing.s,
    contentPadding: PaddingValues = PaddingValues(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val density = LocalDensity.current
    state.keys = keys
    state.gap = with(density) { spacing.toPx() }
    state.canNest = canNest
    val drop by rememberUpdatedState(onDrop)
    state.onDrop = { drop(it) }
    val edge = with(density) { Sizes.button.toPx() }
    LaunchedEffect(state.dragging) {
        if (state.dragging == null) return@LaunchedEffect
        // Near the top or bottom of the window the list scrolls by itself, and the dragged row keeps under the finger.
        while (state.dragging != null) {
            withFrameNanos { }
            val key = state.dragging ?: break
            val y = (state.tops[key] ?: 0f) + (state.heights[key] ?: 0f) / 2 + state.offset - state.scroll.value
            val view = state.scroll.viewportSize.toFloat()
            val step = when {
                y < edge -> -AUTO_SCROLL
                y > view - edge -> AUTO_SCROLL
                else -> 0f
            }
            if (step != 0f) state.offset += state.scroll.scrollBy(step)
        }
    }
    Column(
        modifier.verticalScroll(state.scroll).padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(spacing),
        content = content,
    )
}

/**
 * A row of [CoShellReorderColumn]: hold anywhere and drag. Lifted — a frame in the theme colour on the raised surface
 * (no shadow, #477); a drop target — a dashed frame and a light tint. [moveUpLabel]/[onMoveUp] and the down pair — the
 * same move for TalkBack (custom actions), `null` — the row is already at that end.
 */
@Composable
fun CoShellReorderRow(
    state: ReorderState,
    key: Any,
    modifier: Modifier = Modifier,
    moveUpLabel: String? = null,
    onMoveUp: (() -> Unit)? = null,
    moveDownLabel: String? = null,
    onMoveDown: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val lifted = state.dragging == key
    val target = state.dragging != null && state.over == key
    val shift by animateFloatAsState(state.shift(key), Motion.spatialFast(), label = "reorderShift")
    val shape = RoundedCornerShape(Radii.large)
    val accent = MaterialTheme.colorScheme.primary
    val frame = when {
        lifted -> Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh, shape).border(Sizes.selectionRing, accent, shape)
        target -> Modifier.background(accent.copy(alpha = DROP_TINT), shape).dashedBorder(accent, Radii.large)
        else -> Modifier
    }
    val actions = listOfNotNull(
        moveUpLabel?.let { label -> onMoveUp?.let { CustomAccessibilityAction(label) { it(); true } } },
        moveDownLabel?.let { label -> onMoveDown?.let { CustomAccessibilityAction(label) { it(); true } } },
    )
    Box(
        modifier
            .onPlaced { state.tops[key] = it.positionInParent().y; state.heights[key] = it.size.height.toFloat() }
            .graphicsLayer {
                translationY = if (lifted) state.offset else shift
                if (lifted) {
                    scaleX = LIFT_SCALE
                    scaleY = LIFT_SCALE
                }
            }
            .then(frame)
            .semantics { if (actions.isNotEmpty()) customActions = actions }
            .pointerInput(key) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        state.offset = 0f
                        state.dragging = key
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        state.offset += amount.y
                    },
                    onDragEnd = { state.finish(key, drop = true) },
                    onDragCancel = { state.finish(key, drop = false) },
                )
            },
    ) {
        content()
    }
}

private fun ReorderState.finish(key: Any, drop: Boolean) {
    if (dragging != key) return
    val result = ReorderDrop(key, target, over)
    val from = keys.indexOf(key)
    dragging = null
    offset = 0f
    if (drop && (result.into != null || result.index != from)) onDrop(result)
}

/** How much the lifted row grows. */
private const val LIFT_SCALE = 1.02f

/** Tint of a drop target. */
private const val DROP_TINT = 0.08f

/** Pixels per frame the list scrolls by itself near an edge. */
private const val AUTO_SCROLL = 12f
