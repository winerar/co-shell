package app.coshell.core.designsystem.motion

import androidx.compose.runtime.Immutable

/**
 * One motion language for everything that reacts to a finger (owner's request: "every interactive
 * detail alive, real, three-dimensional — as one system"). A pressed element behaves like a physical
 * object: it sinks ([pressedScale]), tilts towards the finger ([maxTiltDeg]), catches light under the
 * finger ([glow]) and springs back with a little overshoot when released. The tiers differ only in
 * strength — bigger surfaces move less, so the whole app feels like one material.
 */
@Immutable
data class Tactile(
    val pressedScale: Float,
    val maxTiltDeg: Float,
    val glow: Float,
) {
    companion object {
        /** Main action: the "+", «Сохранить». */
        val Primary = Tactile(pressedScale = 0.90f, maxTiltDeg = 10f, glow = 0.30f)
        /** Buttons, chips, segments, icon buttons, swatches, tabs. */
        val Control = Tactile(pressedScale = 0.93f, maxTiltDeg = 8f, glow = 0.24f)
        /** Cards and other blocks. */
        val Surface = Tactile(pressedScale = 0.97f, maxTiltDeg = 3f, glow = 0.16f)
        /** Full-width list rows: no tilt (a wide strip would look broken), only depth and light. */
        val Row = Tactile(pressedScale = 0.985f, maxTiltDeg = 0f, glow = 0.14f)
    }
}

/**
 * Tilt in degrees (rotationX, rotationY) for a press at ([x], [y]) inside a [width]×[height] element:
 * the touched side goes down, like pressing a card on a soft surface. Centre → no tilt. Pure function.
 */
fun tiltFor(x: Float, y: Float, width: Float, height: Float, maxDeg: Float): Pair<Float, Float> {
    if (width <= 0f || height <= 0f || maxDeg == 0f) return 0f to 0f
    val dx = ((x / width) * 2f - 1f).coerceIn(-1f, 1f)
    val dy = ((y / height) * 2f - 1f).coerceIn(-1f, 1f)
    // A wide button barely rocks sideways, a tall element barely nods: long sides would swing too far.
    val aspect = width / height
    val nod = aspect.coerceAtMost(1f).coerceAtLeast(MIN_AXIS)          // rotationX, from vertical offset
    val rock = (1f / aspect).coerceAtMost(1f).coerceAtLeast(MIN_AXIS)  // rotationY, from horizontal offset
    // "+ 0f" turns −0 into 0, so a centre press is exactly "no tilt".
    return (-dy * maxDeg * nod + 0f) to (dx * maxDeg * rock + 0f)
}

private const val MIN_AXIS = 0.35f
