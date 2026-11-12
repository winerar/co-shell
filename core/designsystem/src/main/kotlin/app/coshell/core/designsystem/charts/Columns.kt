package app.coshell.core.designsystem.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.LocalDarkTheme
import app.coshell.core.designsystem.theme.RateTextStyle
import app.coshell.core.designsystem.theme.progressColors
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Spacing

/**
 * One column of [CoShellColumns]: the [label] under it («окт»), the [valueLabel] over it («1 350»), its [fraction] of the
 * chart's [ChartScale] and the [ghost] of the past period (the same month a year ago), `null` — none.
 * [description] — what TalkBack says («октябрь: 1 350 USD»); `null` — «label: valueLabel».
 */
@Immutable
data class ChartColumn(
    val label: String,
    val valueLabel: String,
    val fraction: Float,
    val ghost: Float? = null,
    val description: String? = null,
)

/**
 * Columns by month (board `RepA`, «По месяцам»): bars from zero, the [selected] one (the shown month) in the accent, the
 * others quiet; the value sits on top of each bar. A ghost is an outline behind its column; [average] (share of the
 * scale) — a dashed line across. Columns grow from the axis the first time they show; a tap — [onSelect].
 */
@Composable
fun CoShellColumns(
    columns: List<ChartColumn>,
    modifier: Modifier = Modifier,
    selected: Int = columns.lastIndex,
    average: Float? = null,
    height: Dp = COLUMNS_HEIGHT,
    onSelect: ((Int) -> Unit)? = null,
) {
    val entrance = rememberChartEntrance()
    val lineColor = MaterialTheme.colorScheme.outline
    Row(
        modifier.fillMaxWidth().drawBehind {
            val at = average?.coerceIn(0f, 1f) ?: return@drawBehind
            // The bars' area ends a label row above the bottom; the line marks the average within it.
            val bottom = size.height - LABEL_ROW.toPx()
            val y = bottom - height.toPx() * at * entrance.value
            drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx(), pathEffect = DASH)
        },
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        columns.forEachIndexed { index, column ->
            val click = onSelect?.let { select -> Modifier.tactileClickable(onClick = { select(index) }) } ?: Modifier
            ColumnBar(
                column,
                on = index == selected,
                height = height,
                entrance = { entrance.value },
                modifier = Modifier.weight(1f).then(click).semantics(mergeDescendants = true) {
                    contentDescription = column.description ?: "${column.label}: ${column.valueLabel}"
                    this.selected = index == selected
                },
            )
        }
    }
}

@Composable
private fun ColumnBar(column: ChartColumn, on: Boolean, height: Dp, entrance: () -> Float, modifier: Modifier) {
    // The quiet columns: on the dark card the container tone is lost, the outline tone still reads as a bar.
    val quiet = if (LocalDarkTheme.current) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.surfaceContainerHighest
    val fill = if (on) progressColors().indicator else quiet
    val ghostColor = MaterialTheme.colorScheme.outline
    val ink = if (on) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    val shown = animateFloatAsState(column.fraction.coerceIn(0f, 1f), Motion.spatial(), label = "column")
    // The texts are drawn but not read one by one: the column's description on [modifier] says it all.
    Box(modifier) { Column(Modifier.clearAndSetSemantics {}, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.fillMaxWidth().height(height + VALUE_ROW), contentAlignment = Alignment.BottomCenter) {
            Canvas(Modifier.fillMaxWidth(BAR_SHARE).height(height)) {
                val radius = CornerRadius(TOP_RADIUS.toPx())
                column.ghost?.coerceIn(0f, 1f)?.takeIf { it > 0f }?.let { ghost ->
                    val h = size.height * ghost * entrance()
                    drawRoundRect(ghostColor, Offset(0f, size.height - h), Size(size.width, h), radius, style = Stroke(1.dp.toPx(), pathEffect = DASH))
                }
                val h = (size.height * shown.value * entrance()).coerceAtLeast(MIN_BAR.toPx())
                drawRoundRect(fill, Offset(0f, size.height - h), Size(size.width, h), radius)
            }
            // Rides on the bar's top; the offset is read at placement, so a growing bar does not recompose the text.
            Text(
                column.valueLabel,
                style = RateTextStyle.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize),
                color = ink,
                maxLines = 1,
                modifier = Modifier.offset {
                    val bar = (height.toPx() * shown.value * entrance()).coerceAtLeast(MIN_BAR.toPx())
                    IntOffset(0, -(bar + Spacing.xs.toPx()).toInt())
                },
            )
        }
        Text(
            column.label,
            style = MaterialTheme.typography.labelMedium,
            color = ink,
            maxLines = 1,
            modifier = Modifier.height(LABEL_ROW).padding(top = Spacing.xs),
        )
    } }
}

private val COLUMNS_HEIGHT = 96.dp
/** Room above the tallest bar for its value. */
private val VALUE_ROW = 20.dp
private val LABEL_ROW = 22.dp
private val TOP_RADIUS = 6.dp
/** A zero month is still a visible stub on the axis. */
private val MIN_BAR = 3.dp
private const val BAR_SHARE = 0.7f
private val DASH = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
