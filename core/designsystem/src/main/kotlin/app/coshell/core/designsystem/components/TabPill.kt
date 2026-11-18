package app.coshell.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import app.coshell.core.designsystem.tokens.Motion
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

// The active tab's pill flows between tabs (design-refactor Step 4): one pill for the whole bar instead of
// one per tab. Its edges move on different springs — the leading edge runs ahead, the trailing one catches
// up — so the pill stretches like a drop on the way and settles round. Drawn behind the bar: no layout.

/**
 * How far a finger has pulled the screen towards the neighbouring tab (poll 2026-11-16 «В»): −1…1, positive — towards
 * the next tab (index + 1), negative — the previous one; 0 — no swipe. The pill follows it ([AnimateTabPill]).
 */
@Stable
class TabDrag {
    var fraction by mutableFloatStateOf(0f)
}

/** Where the tabs' pill slots are, and the pill's animated edges, in the bar's coordinates. */
@Stable
internal class TabPillState {
    private val slots = mutableStateMapOf<Int, LayoutCoordinates>()
    internal var bar: LayoutCoordinates? by mutableStateOf(null)
    internal val left = Animatable(Float.NaN)
    internal val right = Animatable(Float.NaN)
    internal var top by mutableStateOf(Float.NaN)
    internal var bottom by mutableStateOf(Float.NaN)

    fun place(index: Int, slot: LayoutCoordinates) {
        slots[index] = slot
    }

    /** Tab [index]'s pill slot in the bar; read in composition, so it updates once both are placed. */
    fun boundsOf(index: Int): Rect? {
        val barCoords = bar?.takeIf { it.isAttached } ?: return null
        val slot = slots[index]?.takeIf { it.isAttached } ?: return null
        val local = barCoords.localPositionOf(slot, Offset.Zero)
        return Rect(local, Size(slot.size.width.toFloat(), slot.size.height.toFloat()))
    }
}

/** Records the box the pill fills under a tab; put it on that box, before any scale. */
internal fun Modifier.tabPillAnchor(state: TabPillState, index: Int): Modifier = onPlaced { state.place(index, it) }

/** The bar side: remembers its coordinates and draws the pill in the selected tab's slot. */
internal fun Modifier.tabPill(state: TabPillState, color: Color, edge: BorderStroke? = null): Modifier =
    onPlaced { state.bar = it }.drawBehind {
        val l = state.left.value
        val r = state.right.value
        if (l.isNaN() || r.isNaN() || state.top.isNaN()) return@drawBehind
        val h = state.bottom - state.top
        drawRoundRect(
            color = color,
            topLeft = Offset(l, state.top),
            size = Size(r - l, h),
            cornerRadius = CornerRadius(h / 2f),
        )
        drawFillEdge(edge, Offset(l, state.top), Size(r - l, h), h / 2f)
    }

/** Moves the pill to [selected]'s slot: the first time it just appears there, later it flows; while [drag] pulls, it follows the finger. */
@Composable
internal fun AnimateTabPill(state: TabPillState, selected: Int, drag: TabDrag? = null) {
    val target = state.boundsOf(selected)
    LaunchedEffect(target) {
        val slot = target ?: return@LaunchedEffect
        state.top = slot.top
        state.bottom = slot.bottom
        if (state.left.value.isNaN()) {
            state.left.snapTo(slot.left)
            state.right.snapTo(slot.right)
            return@LaunchedEffect
        }
        state.flowTo(slot)
    }
    if (drag == null) return
    val current = rememberUpdatedState(drag)
    LaunchedEffect(selected) {
        var pulled = false
        snapshotFlow { current.value.fraction }.collect { p ->
            val from = state.boundsOf(selected) ?: return@collect
            if (p == 0f) {
                // Let go without a switch: the pill springs back (after a switch the selected tab has changed and the effect above moves it).
                if (pulled) state.flowTo(from)
                pulled = false
                return@collect
            }
            val to = state.boundsOf(selected + if (p > 0f) 1 else -1) ?: return@collect
            pulled = true
            val t = kotlin.math.abs(p).coerceIn(0f, 1f)
            state.left.snapTo(from.left + (to.left - from.left) * t)
            state.right.snapTo(from.right + (to.right - from.right) * t)
        }
    }
}

private suspend fun TabPillState.flowTo(slot: Rect) {
    val movingRight = slot.left > left.value
    coroutineScope {
        launch { left.animateTo(slot.left, if (movingRight) Motion.spatial() else Motion.spatialFast()) }
        launch { right.animateTo(slot.right, if (movingRight) Motion.spatialFast() else Motion.spatial()) }
    }
}

@Composable
internal fun rememberTabPillState(): TabPillState = remember { TabPillState() }

