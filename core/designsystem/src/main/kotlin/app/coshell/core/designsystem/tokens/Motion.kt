package app.coshell.core.designsystem.tokens

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

/**
 * Motion tokens (owner 2026-11-09: «скорость анимации унифицировать»): every animation in the app is one of
 * these springs — no durations, no easing curves. Two families, like the Material 3 Expressive motion scheme:
 *
 * - **spatial** — position, size, scale, rotation: slightly under-damped, so movement lands softly;
 * - **effects** — alpha and colour: critically damped, never overshoots;
 * - **bouncy** — the one spring with a visible overshoot: a released press, a "click into place".
 *
 * A spring starts from the current velocity, so an interrupted animation or a released gesture continues
 * smoothly instead of jumping. Which token goes where — docs/agents/motion.md §«Темп». Animate only cheap
 * properties (alpha, translation, scale) so frames fit into 8 ms at 120 Hz (performance.md).
 */
object Motion {
    const val SPATIAL_DAMPING = 0.9f
    const val SPATIAL_FAST = 1400f
    const val SPATIAL_DEFAULT = 700f
    const val SPATIAL_SLOW = 300f

    const val EFFECTS_DAMPING = 1f
    const val EFFECTS_FAST = 3800f
    const val EFFECTS_DEFAULT = 1600f
    const val EFFECTS_SLOW = 800f

    const val BOUNCY_DAMPING = 0.55f
    const val BOUNCY_STIFFNESS = 420f

    /** Small moves that follow a finger: press depth, a switch thumb, the tab pill, a segment knob. */
    fun <T> spatialFast(threshold: T? = null): SpringSpec<T> = spring(SPATIAL_DAMPING, SPATIAL_FAST, threshold)

    /** Most movement: sheets, screens, unfolding blocks, size changes, rows finding their place. */
    fun <T> spatial(threshold: T? = null): SpringSpec<T> = spring(SPATIAL_DAMPING, SPATIAL_DEFAULT, threshold)

    /** Large, calm changes: a whole screen re-arranging, the theme switch. */
    fun <T> spatialSlow(threshold: T? = null): SpringSpec<T> = spring(SPATIAL_DAMPING, SPATIAL_SLOW, threshold)

    /** Instant-feeling effects: the light under a finger, an icon swap, something fading out. */
    fun <T> effectsFast(): SpringSpec<T> = spring(EFFECTS_DAMPING, EFFECTS_FAST)

    /** Fading in and colour changes. */
    fun <T> effects(): SpringSpec<T> = spring(EFFECTS_DAMPING, EFFECTS_DEFAULT)

    /** Slow fades: the press light dying away. */
    fun <T> effectsSlow(): SpringSpec<T> = spring(EFFECTS_DAMPING, EFFECTS_SLOW)

    /** Release of a press and a selected element clicking into place: the overshoot makes it feel alive. */
    fun <T> bouncy(): SpringSpec<T> = spring(BOUNCY_DAMPING, BOUNCY_STIFFNESS)

    /** Size changes (issue #64): a block that grows or shrinks. */
    val Resize: FiniteAnimationSpec<IntSize> = spatial(IntSize.VisibilityThreshold)

    /** Pixel moves: rows sliding to their new place, slide-in/out of screens and digits. */
    val Placement: FiniteAnimationSpec<IntOffset> = spatial(IntOffset.VisibilityThreshold)

    /** Rotation of an "expand" arrow and other small turns, in degrees. */
    val Turn: FiniteAnimationSpec<Float> = spatial()
    // Press feedback (depth, tilt, light) lives in `motion/Tactile` and takes its springs from here.
}
