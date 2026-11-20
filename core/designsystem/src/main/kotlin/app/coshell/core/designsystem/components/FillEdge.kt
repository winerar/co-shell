package app.coshell.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import app.coshell.core.designsystem.theme.contrastRatio
import app.coshell.core.designsystem.theme.deepen
import app.coshell.core.designsystem.theme.mixToContrast
import app.coshell.core.designsystem.tokens.Sizes

/**
 * Thin edge for any coloured fill that melts into its background (#442; owner 2026-11-20 — everywhere contrast is short):
 * a hairline in the deep shade of the fill itself, so the fill does not change. `null` when the fill already
 * stands out (≥ 3:1 to [over]) — in the dark theme the fills are bright enough and get no edge.
 */
@Composable
fun fillEdge(fill: Color, over: Color = MaterialTheme.colorScheme.surface): BorderStroke? =
    fillEdgeColor(fill, over)?.let { BorderStroke(Sizes.hairline, it) }

/** The colour of [fillEdge] (for Material parts that take a border colour, a switch track); `null` — no edge needed. */
fun fillEdgeColor(fill: Color, over: Color): Color? =
    if (contrastRatio(fill, over) >= EDGE_AT) null else deepen(fill, over, EDGE_CONTRAST)

/**
 * [fillEdgeColor] for a fill of the user's own colour (an account card, owner 2026-11-20): a dark fill on a dark page (a
 * black bank colour in the dark theme) cannot be deepened enough, so its edge is lightened instead. `AccountCardContrastTest`.
 */
internal fun accountEdgeColor(fill: Color, over: Color): Color? {
    val deep = fillEdgeColor(fill, over) ?: return null
    return if (contrastRatio(deep, over) >= EDGE_AT) deep else mixToContrast(fill, Color.White, over, EDGE_CONTRAST)
}

/** [fillEdge] as a modifier for a custom-drawn fill (keys, tracks); put it after the shape's clip/background. */
@Composable
fun Modifier.fillEdge(fill: Color, shape: Shape, over: Color = MaterialTheme.colorScheme.surface): Modifier =
    fillEdge(fill, over)?.let { border(it, shape) } ?: this

/** [fillEdge] for a pill drawn by hand (segment, tab): a hairline round-rect inside the given rectangle. */
fun DrawScope.drawFillEdge(edge: BorderStroke?, topLeft: Offset, size: Size, radius: Float) {
    edge ?: return
    val w = edge.width.toPx()
    drawRoundRect(
        brush = edge.brush,
        topLeft = Offset(topLeft.x + w / 2, topLeft.y + w / 2),
        size = Size(size.width - w, size.height - w),
        cornerRadius = CornerRadius(radius - w / 2),
        style = Stroke(w),
    )
}

private const val EDGE_AT = 3f
private const val EDGE_CONTRAST = 3.2f
