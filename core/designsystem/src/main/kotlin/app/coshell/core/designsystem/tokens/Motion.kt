package app.coshell.core.designsystem.tokens

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

/**
 * Motion tokens: every animation in the app takes its duration and curve from here
 * (rule in docs/agents/performance.md). Short durations keep the UI responsive; animate only
 * cheap properties (alpha, translation, scale) so frames fit into 8 ms at 120 Hz.
 */
object Motion {
    /** Small state changes: color, alpha, icon swap. */
    const val SHORT_MS = 150
    /** Elements appearing or moving inside a screen. */
    const val MEDIUM_MS = 250
    /** Screen transitions and sheets. */
    const val LONG_MS = 350

    /** Default curve for movement that starts and ends on screen. */
    val Standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    /** Elements entering the screen: quick start, soft landing. */
    val Decelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
    /** Elements leaving the screen. */
    val Accelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    fun <T> short(): FiniteAnimationSpec<T> = tween(SHORT_MS, easing = Standard)
    fun <T> medium(): FiniteAnimationSpec<T> = tween(MEDIUM_MS, easing = Standard)
    fun <T> enter(): FiniteAnimationSpec<T> = tween(LONG_MS, easing = Decelerate)
    fun <T> exit(): FiniteAnimationSpec<T> = tween(SHORT_MS, easing = Accelerate)

    /**
     * Size changes (issue #64): a block that grows or shrinks follows a soft spring with a hint of
     * overshoot — the same "physical" feel as the press springs. Settles in about 300 ms.
     */
    val Resize: FiniteAnimationSpec<IntSize> =
        spring(dampingRatio = RESIZE_DAMPING, stiffness = Spring.StiffnessMediumLow * 2, visibilityThreshold = IntSize.VisibilityThreshold)
    /** Rows moving to their new place in a list (an item above appeared or left). */
    val Placement: FiniteAnimationSpec<IntOffset> =
        spring(dampingRatio = RESIZE_DAMPING, stiffness = Spring.StiffnessMediumLow * 2, visibilityThreshold = IntOffset.VisibilityThreshold)
    /** Rotation of an "expand" arrow and other small turns, in degrees. */
    val Turn: FiniteAnimationSpec<Float> = spring(dampingRatio = RESIZE_DAMPING, stiffness = Spring.StiffnessMediumLow * 2)
    // Press feedback (depth, tilt, light, springs) lives in `motion/Tactile` — one system for every element.

    private const val RESIZE_DAMPING = 0.8f
}
