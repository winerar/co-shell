package app.coshell.core.designsystem.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.unit.IntSize

/**
 * Records the content into [layer] at [size] and does not draw it on screen — for an off-screen picture (`layer.toImageBitmap()`),
 * e.g. «Поделиться» on the month recap (#554). A modifier, not a component: nothing visible, so no board card.
 */
fun Modifier.recordInto(layer: GraphicsLayer, size: IntSize): Modifier = drawWithContent {
    layer.record(size = size) { this@drawWithContent.drawContent() }
}
