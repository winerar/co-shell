package app.coshell.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
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
internal fun Modifier.tabPill(state: TabPillState, color: Color): Modifier =
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
    }

/** Moves the pill to [selected]'s slot: the first time it just appears there, later it flows. */
@Composable
internal fun AnimateTabPill(state: TabPillState, selected: Int) {
    val target = state.boundsOf(selected)
    LaunchedEffect(target) {
        val slot = target ?: return@LaunchedEffect
        val (l, r) = slot.left to slot.right
        state.top = slot.top
        state.bottom = slot.bottom
        if (state.left.value.isNaN()) {
            state.left.snapTo(l)
            state.right.snapTo(r)
            return@LaunchedEffect
        }
        val movingRight = l > state.left.value
        coroutineScope {
            launch { state.left.animateTo(l, if (movingRight) Motion.spatial() else Motion.spatialFast()) }
            launch { state.right.animateTo(r, if (movingRight) Motion.spatialFast() else Motion.spatial()) }
        }
    }
}

@Composable
internal fun rememberTabPillState(): TabPillState = remember { TabPillState() }

