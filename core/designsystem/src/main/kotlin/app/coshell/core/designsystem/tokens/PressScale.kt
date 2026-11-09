package app.coshell.core.designsystem.tokens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Shrinks the element while it is pressed and springs it back ([Motion.press]): a small "alive"
 * reaction for main buttons (the "+", «Сохранить»). Pass the same [interactionSource] as the button.
 * Scale only, in the graphics layer — no relayout (performance.md).
 */
fun Modifier.pressScale(interactionSource: InteractionSource): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) Motion.PRESSED_SCALE else 1f, Motion.press(), label = "press")
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
