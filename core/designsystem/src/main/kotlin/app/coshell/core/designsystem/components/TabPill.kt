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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.tokens.Motion
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

// The active tab's pill flows between tabs (design-refactor Step 4): one pill for the whole bar instead of
// one per tab. Its edges move on different springs — the leading edge runs ahead, the trailing one catches
// up — so the pill stretches like a drop on the way and settles round. Drawn behind the bar: no layout.

/** Where the tab icons are, and the pill's animated edges, in the bar's coordinates. */
@Stable
internal class TabPillState {
    private val icons = mutableStateMapOf<Int, LayoutCoordinates>()
    internal var bar: LayoutCoordinates? by mutableStateOf(null)
    internal val left = Animatable(Float.NaN)
    internal val right = Animatable(Float.NaN)
    internal var centerY by mutableStateOf(Float.NaN)

    fun place(index: Int, icon: LayoutCoordinates) {
        icons[index] = icon
    }

    /** Centre of tab [index]'s icon in the bar; read in composition, so it updates once both are placed. */
    fun centerOf(index: Int): Offset? {
        val barCoords = bar?.takeIf { it.isAttached } ?: return null
        val icon = icons[index]?.takeIf { it.isAttached } ?: return null
        val local = barCoords.localPositionOf(icon, Offset.Zero)
        return Offset(local.x + icon.size.width / 2f, local.y + icon.size.height / 2f)
    }
}

/** Records a tab icon's place for the pill; put it on the icon's outer box, before any scale. */
internal fun Modifier.tabPillAnchor(state: TabPillState, index: Int): Modifier = onPlaced { state.place(index, it) }

/** The bar side: remembers its coordinates and draws the pill under the [selected] tab's icon. */
internal fun Modifier.tabPill(state: TabPillState, color: Color): Modifier =
    onPlaced { state.bar = it }.drawBehind {
        val l = state.left.value
        val r = state.right.value
        if (l.isNaN() || r.isNaN() || state.centerY.isNaN()) return@drawBehind
        val h = PILL_HEIGHT.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(l, state.centerY - h / 2f),
            size = Size(r - l, h),
            cornerRadius = CornerRadius(h / 2f),
        )
    }

/** Moves the pill to [selected]'s icon: the first time it just appears there, later it flows. */
@Composable
internal fun AnimateTabPill(state: TabPillState, selected: Int) {
    val target = state.centerOf(selected)
    val halfWidth = with(LocalDensity.current) { PILL_WIDTH.toPx() / 2f }
    LaunchedEffect(target, halfWidth) {
        val center = target ?: return@LaunchedEffect
        val (l, r) = center.x - halfWidth to center.x + halfWidth
        state.centerY = center.y
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

/** Material 3 navigation indicator size: the pill under the active tab's icon. */
private val PILL_WIDTH = 64.dp
private val PILL_HEIGHT = 32.dp
