package app.coshell.core.designsystem.motion

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onPlaced

/**
 * The element grows out of [from] (a tap in window coordinates) while [progress] goes 0 → 1: it scales up
 * from [startScale] around the tap, and when [from] carries a colour (the «+») it starts painted in it and
 * the colour dissolves into the element's own look ([shape] — its outline). Read in the layer and draw
 * phases only: no recomposition per frame (performance.md).
 */
fun Modifier.growFrom(from: PressPoint, progress: () -> Float, shape: Shape, startScale: Float = GROW_START_SCALE): Modifier {
    var bounds = Rect.Zero
    return onPlaced { bounds = it.boundsInWindow() }
        .graphicsLayer {
            val scale = startScale + (1f - startScale) * progress()
            scaleX = scale
            scaleY = scale
            // Horizontally — where the finger was; vertically — the bottom edge the sheet rises from.
            transformOrigin = TransformOrigin(transformOriginIn(bounds, from.position).pivotFractionX, 1f)
        }
        .drawWithContent {
            drawContent()
            if (!from.color.isSpecified) return@drawWithContent
            val tint = colorTintAlpha(progress())
            if (tint > 0f) drawOutline(shape.createOutline(size, layoutDirection, this), from.color.copy(alpha = tint))
        }
}

/** Opacity of the opener's colour over the growing element: solid at first, gone by [TINT_GONE_AT]. Pure. */
fun colorTintAlpha(progress: Float): Float {
    val t = ((progress - TINT_HOLD_UNTIL) / (TINT_GONE_AT - TINT_HOLD_UNTIL)).coerceIn(0f, 1f)
    return 1f - t * t * (3f - 2f * t) // smoothstep: no visible edge where the fade starts or ends
}

/** Size the sheet starts from: small enough to read as "out of the button", big enough not to flicker. */
const val GROW_START_SCALE = 0.3f
private const val TINT_HOLD_UNTIL = 0.15f
private const val TINT_GONE_AT = 0.75f

