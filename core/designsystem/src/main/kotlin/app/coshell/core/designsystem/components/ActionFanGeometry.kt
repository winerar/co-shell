package app.coshell.core.designsystem.components

import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Where the buttons of the fan stand (#623): on an arc over the «+», from the left (130°) to the right (50°): steep enough that the captions clear the bar, the first one on the left; one button
 * stands straight up. Offsets are from the centre of the «+», `y` grows downwards. Pure — tested without a screen.
 */
internal object ActionFanGeometry {
    private const val FROM = 130.0
    private const val TO = 50.0

    fun center(index: Int, count: Int, radius: Float): Offset {
        val angle = if (count <= 1) 90.0 else FROM - index * (FROM - TO) / (count - 1)
        val rad = angle * PI / 180.0
        return Offset((radius * cos(rad)).toFloat(), (-radius * sin(rad)).toFloat())
    }

    /** The button under the finger at [at] (offset from the «+» centre): the nearest one within [reach], or `null`. */
    fun hit(at: Offset, count: Int, radius: Float, reach: Float): Int? =
        (0 until count)
            .map { it to (center(it, count, radius) - at).getDistance() }
            .filter { it.second <= reach }
            .minByOrNull { it.second }
            ?.first
}
