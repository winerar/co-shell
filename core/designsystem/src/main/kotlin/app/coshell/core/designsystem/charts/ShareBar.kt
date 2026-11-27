package app.coshell.core.designsystem.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.components.drawFillEdge
import app.coshell.core.designsystem.components.fillEdge
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/** One part of a [CoShellShareBar]: [fraction] 0..1 of the whole, [value] — its words in the legend («45 %»). */
data class ShareSlice(val label: String, val value: String, val fraction: Float, val color: Color)

/**
 * Shares of a whole as one capsule cut into pieces (#192, board `Idea5A`: the currencies of the total balance) with a
 * legend under it — a dot, the [ShareSlice.label] and its [ShareSlice.value]. Pieces are separated by a small gap and
 * start at the left; [description] is the whole thing for TalkBack (the legend is hidden from it).
 */
@Composable
fun CoShellShareBar(slices: List<ShareSlice>, description: String, modifier: Modifier = Modifier) {
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    val edges = slices.map { fillEdge(it.color, track) }
    Column(modifier.fillMaxWidth().semantics(mergeDescendants = true) { contentDescription = description }) {
        Canvas(Modifier.fillMaxWidth().height(Sizes.bullet)) {
            val gap = GAP.toPx()
            val usable = size.width - gap * (slices.size - 1).coerceAtLeast(0)
            val radius = CornerRadius(size.height / 2)
            var x = 0f
            slices.forEachIndexed { i, slice ->
                val width = (usable * slice.fraction.coerceIn(0f, 1f)).coerceAtLeast(size.height)
                val fill = Size(width, size.height)
                drawRoundRect(slice.color, Offset(x, 0f), fill, radius)
                drawFillEdge(edges[i], Offset(x, 0f), fill, radius.x)
                x += width + gap
            }
        }
        FlowRow(
            Modifier.fillMaxWidth().clearAndSetSemantics { },
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            slices.forEach { slice ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    Canvas(Modifier.size(DOT)) { drawCircle(slice.color) }
                    Text(
                        slice.label + " " + slice.value,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private val GAP = 3.dp
private val DOT = 8.dp
