package app.coshell.core.designsystem.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.tokens.Motion
import kotlin.math.sqrt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Small per-element motions of the design-refactor catalogue (Step 4). All of them are alpha / translation
// only (performance.md), run once per event and are idle otherwise.

/**
 * Entrance of the blocks of a screen: fades in and rises [STAGGER_RISE] into place, block [index] a beat
 * ([STAGGER_STEP_MS]) after the previous one. Only the first time the screen shows: coming back to a tab
 * (its state is saved) shows the blocks in place at once.
 */
fun Modifier.staggeredEntrance(index: Int): Modifier = composed {
    val shown = rememberSaveable { booleanArrayOf(false) }
    val progress = remember { Animatable(if (shown[0]) 1f else 0f) }
    val rise = with(LocalDensity.current) { STAGGER_RISE.toPx() }
    LaunchedEffect(Unit) {
        if (shown[0]) return@LaunchedEffect
        delay(staggerDelayMs(index))
        shown[0] = true
        launch { progress.animateTo(1f, Motion.spatial()) }
    }
    graphicsLayer {
        val p = progress.value
        alpha = p.coerceIn(0f, 1f)
        translationY = (1f - p) * rise
    }
}

/** Delay of block [index] in a [staggeredEntrance]; capped so a long screen does not keep the user waiting. */
internal fun staggerDelayMs(index: Int): Long = (index.coerceIn(0, MAX_STAGGERED) * STAGGER_STEP_MS)

/**
 * A field with a new error shakes sideways (twice, settling): a `bouncy` spring kicked off with a velocity
 * that peaks at [SHAKE_AMPLITUDE]. [error] — the message (or any key of the error state); null — no error.
 */
fun Modifier.errorShake(error: Any?): Modifier = composed {
    val offset = remember { Animatable(0f) }
    val amplitude = with(LocalDensity.current) { SHAKE_AMPLITUDE.toPx() }
    val first = remember { booleanArrayOf(true) }
    LaunchedEffect(error) {
        if (first[0]) {
            first[0] = false
            return@LaunchedEffect
        }
        if (error == null) return@LaunchedEffect
        offset.snapTo(0f)
        offset.animateTo(0f, Motion.bouncy(), initialVelocity = shakeVelocity(amplitude))
    }
    graphicsLayer { translationX = offset.value }
}

/** Starting velocity for a peak of [amplitudePx] on the bouncy spring (≈ amplitude × natural frequency). */
internal fun shakeVelocity(amplitudePx: Float): Float = amplitudePx * sqrt(Motion.BOUNCY_STIFFNESS)

private val STAGGER_RISE = 12.dp
private const val STAGGER_STEP_MS = 30L
private const val MAX_STAGGERED = 8
private val SHAKE_AMPLITUDE = 8.dp
