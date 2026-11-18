package app.coshell.core.designsystem.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.components.fillEdge
import app.coshell.core.designsystem.theme.progressColors
import app.coshell.core.designsystem.tokens.Spacing
import kotlin.math.roundToInt

/** One point of [CoShellLine]: the [label] under it («н»), its [value] (any unit, e.g. minor units of one currency). */
@Immutable
data class LinePoint(val label: String, val value: Long)

/**
 * A line over time (board `Rp189NwB`, «Капитал»): the points joined in the accent, a faint fill under them, two dotted
 * guides and a dot on the [selected] point. The scale is fitted to the values (not from zero: the change matters, not
 * the level); [description] — what TalkBack says for the whole chart. The line draws from the left the first time.
 * [onSelect] — a tap picks the nearest point (#373, «тап — другой месяц»); `null` — the line is a picture.
 */
@Composable
fun CoShellLine(
    points: List<LinePoint>,
    description: String,
    modifier: Modifier = Modifier,
    selected: Int = points.lastIndex,
    height: Dp = LINE_HEIGHT,
    onSelect: ((Int) -> Unit)? = null,
) {
    val entrance = rememberChartEntrance()
    val accent = progressColors().indicator
    val edge = fillEdge(accent)?.brush
    val guide = MaterialTheme.colorScheme.outlineVariant
    val marker = MaterialTheme.colorScheme.outline
    val ring = MaterialTheme.colorScheme.surfaceContainer
    Column(modifier.fillMaxWidth().semantics { contentDescription = description }) {
        val tap = if (onSelect == null || points.isEmpty()) Modifier else Modifier.pointerInput(points.size, onSelect) {
            detectTapGestures { offset -> onSelect(nearestPoint(offset.x - DOT_RADIUS.toPx(), size.width - 2 * DOT_RADIUS.toPx(), points.size)) }
        }
        Canvas(Modifier.fillMaxWidth().height(height).then(tap).padding(horizontal = DOT_RADIUS)) {
            if (points.isEmpty()) return@Canvas
            val ys = lineFractions(points.map { it.value })
            val step = if (points.size > 1) size.width / (points.size - 1) else 0f
            val at = points.indices.map { Offset(it * step, size.height * (1 - ys[it])) }
            for (g in 1..2) {
                val y = size.height * g / 3f
                drawLine(guide, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx(), pathEffect = DOTS)
            }
            val line = Path().apply { at.forEachIndexed { i, p -> if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) } }
            val area = Path().apply {
                addPath(line)
                lineTo(at.last().x, size.height)
                lineTo(at.first().x, size.height)
                close()
            }
            clipRect(right = size.width * entrance.value + DOT_RADIUS.toPx()) {
                drawPath(area, accent.copy(alpha = FILL_ALPHA))
                edge?.let { drawPath(line, it, style = Stroke(LINE_WIDTH.toPx() + 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)) }
                drawPath(line, accent, style = Stroke(LINE_WIDTH.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                at.getOrNull(selected)?.let { p ->
                    drawLine(marker, Offset(p.x, 0f), Offset(p.x, size.height), strokeWidth = 1.dp.toPx())
                    drawCircle(ring, DOT_RADIUS.toPx() + 2.dp.toPx(), p)
                    drawCircle(accent, DOT_RADIUS.toPx(), p)
                    edge?.let { drawCircle(it, DOT_RADIUS.toPx(), p, style = Stroke(1.dp.toPx())) }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = Spacing.xs)) {
            points.forEachIndexed { i, point ->
                Text(
                    point.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (i == selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** The point of [count] spread evenly over [width] nearest to [x] (both from the first point). */
fun nearestPoint(x: Float, width: Float, count: Int): Int {
    if (count <= 1 || width <= 0f) return 0
    return (x / (width / (count - 1))).roundToInt().coerceIn(0, count - 1)
}

/**
 * Heights 0..1 of [values] on a scale fitted to them with a margin (15% of the range below, 10% above), so a flat line
 * sits low and a change is visible; all equal — the middle.
 */
fun lineFractions(values: List<Long>): List<Float> {
    if (values.isEmpty()) return emptyList()
    val lo = values.min()
    val hi = values.max()
    if (hi == lo) return values.map { 0.5f }
    val range = (hi - lo).toDouble()
    val bottom = lo - range * BELOW
    val top = hi + range * ABOVE
    return values.map { ((it - bottom) / (top - bottom)).toFloat() }
}

private val LINE_HEIGHT = 100.dp
private val LINE_WIDTH = 2.5.dp
private val DOT_RADIUS = 5.dp
private const val FILL_ALPHA = 0.12f
private const val BELOW = 0.15
private const val ABOVE = 0.10
private val DOTS = PathEffect.dashPathEffect(floatArrayOf(3f, 6f))
