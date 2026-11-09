package app.coshell.core.designsystem.components

import androidx.compose.animation.core.animate
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import app.coshell.core.designsystem.tokens.Motion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// Motion of a bottom sheet (owner 2026-11-09: «шторка всё ещё слишком дёрганая»). The old sheet ran three
// movements at once (Material's slide, growth from 30 % on its own curve, the dialog window) — they pulled the top
// edge different ways. Now there is ONE source: [SheetMotionState.hidden]. Position, scale, scrim and the opener's
// colour are all derived from it, it follows the finger 1:1 and every animation is the same `Motion.spatial` spring
// starting from the finger's velocity.

/** Size the sheet grows from while it rises (around the tap horizontally, from the bottom edge). */
internal const val OPEN_FROM_SCALE = 0.96f
/** How much the predictive back gesture shrinks the sheet at full progress. */
internal const val BACK_SHRINK = 0.08f
/** Released lower than this share of its height (and not flung up) → the sheet closes. */
internal const val CLOSE_AT = 0.35f
/** Fling speed, in sheet heights per second, that decides on its own: down → close, up → stay open. */
internal const val FLING_HEIGHTS_PER_SECOND = 2f

/** Where a drag of [deltaPx] (down is positive) moves a sheet [heightPx] tall that is [hidden] (0 open … 1 gone). Pure. */
internal fun draggedHidden(hidden: Float, deltaPx: Float, heightPx: Float): Float =
    if (heightPx <= 0f) hidden else (hidden + deltaPx / heightPx).coerceIn(0f, 1f)

/** After a release at [hidden] with [velocity] (heights per second, down positive): close or spring back open? Pure. */
internal fun shouldClose(hidden: Float, velocity: Float): Boolean = when {
    velocity > FLING_HEIGHTS_PER_SECOND -> true
    velocity < -FLING_HEIGHTS_PER_SECOND -> false
    else -> hidden > CLOSE_AT
}

/** Scale of the sheet: grows 0.96 → 1 as it rises, shrinks with the predictive back gesture ([back] 0 … 1). Pure. */
internal fun sheetScale(hidden: Float, back: Float): Float {
    val shown = (1f - hidden).coerceIn(0f, 1f)
    return (OPEN_FROM_SCALE + (1f - OPEN_FROM_SCALE) * shown) * (1f - BACK_SHRINK * back.coerceIn(0f, 1f))
}

/** Strength of the scrim: follows the sheet's position, so it never jumps when the sheet is dragged. Pure. */
internal fun scrimFraction(hidden: Float): Float = (1f - hidden).coerceIn(0f, 1f)

/** The sheet's single source of motion. All reads happen in the layer and draw phases (no recomposition per frame). */
@Stable
internal class SheetMotionState(private val scope: CoroutineScope, private val onHidden: () -> Unit) {
    /** 0 — fully open, 1 — just below the screen. */
    var hidden by mutableFloatStateOf(1f)
        private set
    /** Progress of the predictive back gesture, 0 … 1. */
    var back by mutableFloatStateOf(0f)
    var closing by mutableStateOf(false)
        private set
    /** The opener's colour (the «+») still dissolves: only while the sheet rises untouched. */
    var tinting by mutableStateOf(true)
        private set
    var heightPx = 0f

    private var job: Job? = null

    fun open() = animateTo(0f, 0f)

    fun close(velocity: Float = 0f) {
        if (closing) return
        closing = true
        tinting = false
        animateTo(1f, velocity)
    }

    /** Moves the sheet with the finger; returns the pixels it took. */
    fun dragBy(deltaPx: Float): Float {
        if (closing || heightPx <= 0f) return 0f
        job?.cancel()
        tinting = false
        val old = hidden
        hidden = draggedHidden(old, deltaPx, heightPx)
        return (hidden - old) * heightPx
    }

    /** The finger let go with [velocityPx] (px/s, down positive). */
    fun settle(velocityPx: Float) {
        if (closing) return
        val velocity = if (heightPx > 0f) velocityPx / heightPx else 0f
        if (shouldClose(hidden, velocity)) close(velocity) else animateTo(0f, velocity)
    }

    fun cancelBack() {
        scope.launch { animate(back, 0f, animationSpec = Motion.spatial()) { value, _ -> back = value } }
    }

    private fun animateTo(target: Float, velocity: Float) {
        job?.cancel()
        job = scope.launch {
            animate(hidden, target, velocity, Motion.spatial()) { value, _ -> hidden = value }
            if (target == 1f) onHidden()
        }
    }
}

/**
 * Lets the form inside scroll and the sheet move as one surface: pulling down at the top of the form drags the
 * sheet, pushing up first opens it back, and a fling decides like a release.
 */
internal class SheetNestedScroll(private val motion: SheetMotionState) : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset =
        if (source == NestedScrollSource.UserInput && available.y < 0f && motion.hidden > 0f) {
            Offset(0f, motion.dragBy(available.y))
        } else {
            Offset.Zero
        }

    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset =
        if (source == NestedScrollSource.UserInput && available.y > 0f) Offset(0f, motion.dragBy(available.y)) else Offset.Zero

    override suspend fun onPreFling(available: Velocity): Velocity = settleIfMoved(available)

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity = settleIfMoved(available)

    private fun settleIfMoved(available: Velocity): Velocity {
        if (motion.hidden <= 0f) return Velocity.Zero
        motion.settle(available.y)
        return available
    }
}
