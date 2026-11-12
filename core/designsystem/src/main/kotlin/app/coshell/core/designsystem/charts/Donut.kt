package app.coshell.core.designsystem.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** A slice of [CoShellDonut]: a plain magnitude (minor units of one currency) and the category colour. */
@Immutable
data class DonutSlice(val value: Long, val color: Color)

/** Where one slice is drawn: degrees clockwise from 12 o'clock. */
@Immutable
data class DonutArc(val start: Float, val sweep: Float)

/**
 * A ring of shares (structure of spending): at most [DONUT_MAX_SLICES] categories plus «Другое» — the caller groups the
 * tail (`spendingBreakdown`), more slices would not be told apart. Slices go clockwise from the top with thin gaps; the
 * ring draws round the first time it shows. [content] sits in the hole (the total). The ring itself says nothing to
 * TalkBack beyond [description]: the list next to it carries the numbers.
 */
@Composable
fun CoShellDonut(
    slices: List<DonutSlice>,
    description: String,
    modifier: Modifier = Modifier,
    size: Dp = DONUT_SIZE,
    thickness: Dp = DONUT_THICKNESS,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val arcs = remember(slices) { donutArcs(slices.map { it.value }) }
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    val entrance = rememberChartEntrance()
    Box(modifier.size(size).semantics { contentDescription = description }, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = thickness.toPx()
            val inset = stroke / 2
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            val topLeft = Offset(inset, inset)
            if (arcs.isEmpty()) {
                drawArc(track, 0f, FULL, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(stroke))
                return@Canvas
            }
            // The whole ring opens clockwise: a slice is drawn up to where the sweep has reached.
            val reached = FULL * entrance.value
            arcs.forEachIndexed { index, arc ->
                val visible = (reached - arc.start).coerceIn(0f, arc.sweep)
                if (visible > 0f) {
                    drawArc(slices[index].color, arc.start + TOP, visible, useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(stroke))
                }
            }
        }
        content()
    }
}

/**
 * Pure: the arcs of [values] (shares of their sum), each [gap] degrees shorter so slices stand apart; a slice too small
 * for that still shows as a sliver of [MIN_SWEEP]. One slice — a full ring without a gap; nothing positive — no arcs.
 */
fun donutArcs(values: List<Long>, gap: Float = GAP): List<DonutArc> {
    val total = values.sumOf { it.coerceAtLeast(0) }
    if (total <= 0) return emptyList()
    if (values.count { it > 0 } == 1) return values.map { if (it > 0) DonutArc(0f, FULL) else DonutArc(0f, 0f) }
    var start = 0f
    return values.map { value ->
        val whole = FULL * value.coerceAtLeast(0) / total
        val arc = DonutArc(start + gap / 2, if (whole <= 0f) 0f else (whole - gap).coerceAtLeast(MIN_SWEEP))
        start += whole
        arc
    }
}

/** Categories on a ring before the rest goes to «Другое» (`docs/research/reports-and-charts.md`). */
const val DONUT_MAX_SLICES = 6

private const val FULL = 360f
/** Canvas angles start at 3 o'clock; ours at 12. */
private const val TOP = -90f
private const val GAP = 2f
private const val MIN_SWEEP = 0.8f
private val DONUT_SIZE = 140.dp
private val DONUT_THICKNESS = 16.dp
