package app.coshell.core.designsystem.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

/** How much a just-selected element swells before settling. */
internal const val POP_SCALE = 1.08f

/**
 * Second half of the [Tactile] language: when an element becomes selected (chip, tab, segment,
 * colour, icon) it gives a short springy "pop", as if it clicked into place. Not on first show.
 */
fun Modifier.selectionPop(selected: Boolean): Modifier = composed {
    val scale = remember { Animatable(1f) }
    val first = remember { booleanArrayOf(true) }
    LaunchedEffect(selected) {
        if (first[0]) {
            first[0] = false
            return@LaunchedEffect
        }
        if (!selected) return@LaunchedEffect
        scale.snapTo(1f)
        scale.animateTo(POP_SCALE, spring(stiffness = Spring.StiffnessHigh))
        scale.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium))
    }
    graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}
