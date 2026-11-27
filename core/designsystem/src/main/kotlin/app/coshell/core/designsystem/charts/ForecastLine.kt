package app.coshell.core.designsystem.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    val futureColor = if (warning) LocalStatusPalette.current.warning else secondaryInk()
    val guide = MaterialTheme.colorScheme.outlineVariant
    val ring = MaterialTheme.colorScheme.surfaceContainer
    val entrance = rememberChartEntrance()
    Column(modifier.fillMaxWidth().semantics { contentDescription = description }) {
        Canvas(Modifier.fillMaxWidth().height(HEIGHT).padding(horizontal = DOT)) {
            if (future.isEmpty()) return@Canvas
            val all = past + future
            val low = minOf(0L, all.min())
            val span = (maxOf(0L, all.max()) - low).coerceAtLeast(1L)
            val y = { v: Long -> size.height * (1f - (v - low).toFloat() / span) }
            val step = size.width / (all.size - 1).coerceAtLeast(1)
            val zeroY = y(0L)
            drawLine(guide, Offset(0f, zeroY), Offset(size.width, zeroY), strokeWidth = 1.dp.toPx(), pathEffect = DOTS)
            val todayX = (past.size) * step
            drawLine(guide, Offset(todayX, 0f), Offset(todayX, size.height), strokeWidth = 1.dp.toPx(), pathEffect = DOTS)
            fun path(values: List<Long>, from: Int) = Path().apply {
                values.forEachIndexed { i, v -> (Offset((from + i) * step, y(v))).let { if (i == 0) moveTo(it.x, it.y) else lineTo(it.x, it.y) } }
            }
            val shown = entrance.value
            if (past.isNotEmpty()) {
                drawPath(path(past + future.first(), 0), accent, style = Stroke(WIDTH.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round), alpha = shown)
            }
            drawPath(
                path(future, past.size), futureColor, alpha = shown,
                style = Stroke(WIDTH.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round, pathEffect = DASHES),
            )
            val at = Offset((past.size + lowest.coerceIn(0, future.lastIndex)) * step, y(future[lowest.coerceIn(0, future.lastIndex)]))
            drawCircle(ring, DOT.toPx() + 2.dp.toPx(), at, alpha = shown)
            drawCircle(futureColor, DOT.toPx(), at, alpha = shown)
        }
        Row(Modifier.fillMaxWidth().padding(top = Spacing.xs)) {
            val style = MaterialTheme.typography.labelSmall
            val color = MaterialTheme.colorScheme.onSurfaceVariant
            Text(startLabel, style = style, color = color, modifier = Modifier.weight(1f))
            Text(todayLabel, style = style, color = color, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            Text(endLabel, style = style, color = color, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        }
    }
}

private val HEIGHT = 120.dp
private val WIDTH = 2.5.dp
private val DOT = 5.dp
private val DOTS = PathEffect.dashPathEffect(floatArrayOf(3f, 6f))
private val DASHES = PathEffect.dashPathEffect(floatArrayOf(14f, 12f))
