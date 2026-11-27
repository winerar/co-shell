package app.coshell.core.designsystem.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.Alignment
import app.coshell.core.designsystem.motion.dataSwap
import app.coshell.core.designsystem.tokens.Motion
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.round
import kotlin.math.floor
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.theme.LocalStatusPalette
import app.coshell.core.designsystem.theme.progressColors
import app.coshell.core.designsystem.theme.secondaryInk
import app.coshell.core.designsystem.tokens.Spacing

/**
 * A balance with a look ahead (#770, board `Forecast770`): [past] — the days behind, a solid line in the accent; [future] —
 * the days to come, dashed in the second colour; the first of [future] is today and joins the end of [past]. A dotted line
 * marks zero (it is always on the scale), a dot marks [lowest] (an index into [future]). [warning] — the money runs out:
 * the future and the dot take the attention colour (never red, never «превышено»). Under the line — [startLabel], [todayLabel],
 * [endLabel]; [description] is what TalkBack says. Values — any unit, minor units of one currency.
 */
@Composable
fun CoShellForecastLine(
    past: List<Long>,
    future: List<Long>,
    lowest: Int,
    warning: Boolean,
    description: String,
    startLabel: String,
    todayLabel: String,
    endLabel: String,
    modifier: Modifier = Modifier,
) {
    val accent = progressColors().indicator
    val futureColor by animateColorAsState(if (warning) LocalStatusPalette.current.warning else secondaryInk(), Motion.effects(), label = "forecastColor")
    val guide = MaterialTheme.colorScheme.outlineVariant
    val ring = MaterialTheme.colorScheme.surfaceContainer
    val entrance = rememberChartEntrance()
    val shape = remember(past, future, lowest) { forecastShape(past, future, lowest) }
    // 30 → 60 days or new numbers: the line stretches and the marks slide (owner 2026-11-27), nothing redraws from scratch.
    val morph = rememberChartMorph(shape.heights)
    val zero = animateFloatAsState(shape.zero, Motion.spatial(), label = "forecastZero")
    val today = animateFloatAsState(shape.today, Motion.spatial(), label = "forecastToday")
    val low = animateFloatAsState(shape.lowest, Motion.spatial(), label = "forecastLow")
    Column(modifier.fillMaxWidth().semantics { contentDescription = description }) {
        Canvas(Modifier.fillMaxWidth().height(HEIGHT).padding(horizontal = DOT)) {
            val n = morph.size
            if (n == 0) return@Canvas
            val step = size.width / (n - 1).coerceAtLeast(1)
            val last = (n - 1).toFloat()
            fun point(x: Float) = Offset(x * step, size.height * (1f - morph.atFraction(x)))
            val zeroY = size.height * (1f - zero.value)
            drawLine(guide, Offset(0f, zeroY), Offset(size.width, zeroY), strokeWidth = 1.dp.toPx(), pathEffect = DOTS)
            val split = snapToPoint(today.value * last).coerceIn(0f, last)
            val todayX = split * step
            drawLine(guide, Offset(todayX, 0f), Offset(todayX, size.height), strokeWidth = 1.dp.toPx(), pathEffect = DOTS)
            val shown = entrance.value
            val stroke = Stroke(WIDTH.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            if (split > 0f) {
                val path = Path().apply {
                    moveTo(point(0f).x, point(0f).y)
                    (1..floor(split).toInt()).forEach { point(it.toFloat()).let { p -> lineTo(p.x, p.y) } }
                    if (split > floor(split)) point(split).let { lineTo(it.x, it.y) }
                }
                drawPath(path, accent, style = stroke, alpha = shown)
            }
            val ahead = Path().apply {
                point(split).let { moveTo(it.x, it.y) }
                (ceil(split).toInt()..(n - 1)).forEach { i -> if (i > split) point(i.toFloat()).let { lineTo(it.x, it.y) } }
            }
            drawPath(ahead, futureColor, alpha = shown, style = Stroke(WIDTH.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round, pathEffect = DASHES))
            val at = point(snapToPoint(low.value * last).coerceIn(0f, last))
            drawCircle(ring, DOT.toPx() + 2.dp.toPx(), at, alpha = shown)
            drawCircle(futureColor, DOT.toPx(), at, alpha = shown)
        }
        Row(Modifier.fillMaxWidth().padding(top = Spacing.xs)) {
            Label(startLabel, Alignment.TopStart, Modifier.weight(1f))
            Label(todayLabel, Alignment.TopCenter, Modifier.weight(1f))
            Label(endLabel, Alignment.TopEnd, Modifier.weight(1f))
        }
    }
}

@Composable
private fun Label(text: String, alignment: Alignment, modifier: Modifier) {
    // The box takes the weight; AnimatedContent itself is only as wide as the words.
    Box(modifier, contentAlignment = alignment) {
        AnimatedContent(text, transitionSpec = { dataSwap() }, contentAlignment = alignment, label = "forecastLabel") {
            Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

/** A position a hair off a point (float round-off of a share × count) is that point: no sliver segment shifts the dashes. */
internal fun snapToPoint(x: Float): Float {
    val near = round(x)
    return if (abs(x - near) < SNAP) near else x
}

private const val SNAP = 0.001f

/** Where the marks of [CoShellForecastLine] sit, all as shares 0..1: heights of the points, the zero line, today and the lowest dot across. */
internal class ForecastShape(val heights: List<Float>, val zero: Float, val today: Float, val lowest: Float)

/** Pure: the scale always holds zero; with no future there is nothing to draw. */
internal fun forecastShape(past: List<Long>, future: List<Long>, lowest: Int): ForecastShape {
    if (future.isEmpty()) return ForecastShape(emptyList(), 0f, 0f, 0f)
    val all = past + future
    val low = minOf(0L, all.min())
    val span = (maxOf(0L, all.max()) - low).coerceAtLeast(1L).toFloat()
    val last = (all.size - 1).coerceAtLeast(1).toFloat()
    return ForecastShape(
        heights = all.map { (it - low) / span },
        zero = -low / span,
        today = past.size / last,
        lowest = (past.size + lowest.coerceIn(0, future.lastIndex)) / last,
    )
}

private val HEIGHT = 120.dp
private val WIDTH = 2.5.dp
private val DOT = 5.dp
private val DOTS = PathEffect.dashPathEffect(floatArrayOf(3f, 6f))
private val DASHES = PathEffect.dashPathEffect(floatArrayOf(14f, 12f))
