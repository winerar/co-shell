package app.coshell.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import app.coshell.core.designsystem.components.TabDrag
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Sizes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Tabs flip with a horizontal swipe (issue #112, mockup 19В): the screen leans after the finger, and past
// [SWITCH_AT] the neighbouring tab opens with the usual shared-X-axis transition. Rows and chip strips that
// scroll sideways win: their drag is consumed first and the swipe gives up. The bar's pill follows the finger
// ([TabDrag], poll 2026-11-16 «В»): halfway to the threshold×2 it sits between the tabs, on release it flows on.

/** How far the screen follows the finger: a lean, not a page — the neighbour is drawn only after the switch. */
private const val FOLLOW = 0.25f
/** At the first and last tab there is nowhere to go: a stiff rubber band. */
private const val FOLLOW_EDGE = 0.08f
/** After a switch, how long the pill may stay pulled if the tab never changes. */
private const val PILL_RELEASE_MS = 600L

/**
 * Tab to open after a horizontal drag of [drag] px on tab [current] of [count]: a drag to the left opens
 * the next tab, to the right — the previous one; shorter than [threshold] or past the ends — `null`. Pure.
 */
internal fun tabSwipeTarget(current: Int, drag: Float, threshold: Float, count: Int): Int? {
    if (kotlin.math.abs(drag) < threshold) return null
    val target = if (drag < 0) current + 1 else current - 1
    return target.takeIf { it in 0 until count }
}

/** How far the bar's pill is pulled towards the neighbour: −1…1 (positive — the next tab); a swipe over the ends or no drag — 0. Pure. */
internal fun tabDragFraction(current: Int, drag: Float, threshold: Float, count: Int): Float {
    if (tabSwipeTarget(current, drag, 0f, count) == null) return 0f
    return (-drag / (2f * threshold)).coerceIn(-1f, 1f)
}

/**
 * Swipes between the bar's tabs while [enabled] (on a tab, no selection bar); [current] — the shown tab;
 * [tabDrag] reports the finger's progress to the bar's pill.
 */
internal fun Modifier.tabSwipe(tabDrag: TabDrag, enabled: Boolean, tabs: List<TopLevel>, current: TopLevel?, onTab: (TopLevel) -> Unit): Modifier = composed {
    val lean = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val threshold = with(LocalDensity.current) { Sizes.tabSwipe.toPx() }
    val open by rememberUpdatedState(onTab)
    // A switch keeps the pill pulled until the new tab is selected; then it is released.
    LaunchedEffect(current) { tabDrag.fraction = 0f }
    val gesture = if (!enabled || current == null) Modifier else Modifier.pointerInput(current, tabs) {
        val at = tabs.indexOf(current)
        var drag = 0f
        val settle = { scope.launch { lean.animateTo(0f, Motion.spatial()) } }
        detectHorizontalDragGestures(
            onDragStart = { drag = 0f },
            onDragEnd = {
                settle()
                val target = tabSwipeTarget(at, drag, threshold, tabs.size)
                if (target == null) {
                    tabDrag.fraction = 0f
                } else {
                    open(tabs[target])
                    // Safety: if the tab did not change, do not leave the pill pulled.
                    scope.launch { delay(PILL_RELEASE_MS); tabDrag.fraction = 0f }
                }
            },
            onDragCancel = { settle(); tabDrag.fraction = 0f },
        ) { change, dx ->
            change.consume()
            drag += dx
            val edge = tabSwipeTarget(at, drag, 0f, tabs.size) == null
            tabDrag.fraction = tabDragFraction(at, drag, threshold, tabs.size)
            scope.launch { lean.snapTo(drag * if (edge) FOLLOW_EDGE else FOLLOW) }
        }
    }
    graphicsLayer { translationX = lean.value }.then(gesture)
}
