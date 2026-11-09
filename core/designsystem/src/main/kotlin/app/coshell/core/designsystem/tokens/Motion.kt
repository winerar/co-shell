package app.coshell.core.designsystem.tokens

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

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

    /** "Living" press (owner's choice, design-questions §6): a soft bouncy spring, settles in ~300 ms. */
    fun <T> press(): FiniteAnimationSpec<T> = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)

    /** How far a pressed button shrinks. */
    const val PRESSED_SCALE = 0.92f
}
