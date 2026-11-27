package app.coshell.core.designsystem.charts

import androidx.compose.ui.graphics.compositeOver
import app.coshell.core.designsystem.components.drawFillEdge
import app.coshell.core.designsystem.components.fillEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.core.designsystem.tokens.Radii
import androidx.compose.foundation.shape.RoundedCornerShape
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Spacing

/**
 * One pair of [CoShellPairedColumns]: the [label] under it («окт»), what came in ([first]) and went out ([second]) as
 * shares of one [ChartScale]; [description] — what TalkBack says («октябрь: пришло 1 620, ушло 1 350»).
 */
@Immutable
data class ChartPair(val label: String, val first: Float, val second: Float, val description: String)

/**
 * Income next to spending by month (board `Rp189IeA`, «Доходы и расходы»): two bars from zero per month — income in
 * the income colour, spending in the expense colour; the [selected] pair is bright on a tinted plate, the others
 * faded. Bars grow from the axis the first time they show; a tap — [onSelect].
 */
@Composable
fun CoShellPairedColumns(
    pairs: List<ChartPair>,
    modifier: Modifier = Modifier,
    selected: Int = pairs.lastIndex,
    height: Dp = PAIRS_HEIGHT,
    onSelect: ((Int) -> Unit)? = null,
) {
    val entrance = rememberChartEntrance()
    val amounts = LocalAmountPalette.current
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
        pairs.forEachIndexed { index, pair ->
            val on = index == selected
            val click = onSelect?.let { select -> Modifier.tactileClickable(onClick = { select(index) }) } ?: Modifier
            // Another month picked: the plate and the brighter bars pass over instead of jumping (owner 2026-11-27).
            val plateColor by animateColorAsState(
                if (on) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0f),
                Motion.effects(),
                label = "pairPlate",
            )
            val plate = Modifier.clip(RoundedCornerShape(Radii.medium)).background(plateColor)
            Box(
                Modifier.weight(1f).then(plate).then(click).semantics(mergeDescendants = true) {
                    contentDescription = pair.description
                    this.selected = on
                },
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = Spacing.xs).clearAndSetSemantics {},
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        Modifier.fillMaxWidth(PAIR_SHARE).widthIn(max = PAIR_MAX).height(height),
                        horizontalArrangement = Arrangement.spacedBy(BAR_GAP),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        PairBar(pair.first, amounts.income, on, height, Modifier.weight(1f)) { entrance.value }
                        PairBar(pair.second, amounts.expense, on, height, Modifier.weight(1f)) { entrance.value }
                    }
                    Text(
                        pair.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = animateColorAsState(
                            if (on) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            Motion.effects(),
                            label = "pairLabel",
                        ).value,
                        maxLines = 1,
                        modifier = Modifier.padding(top = Spacing.xs),
                    )
                }
            }
        }
    }
}

@Composable
private fun PairBar(fraction: Float, color: Color, on: Boolean, height: Dp, modifier: Modifier, entrance: () -> Float) {
    val shown = animateFloatAsState(fraction.coerceIn(0f, 1f), Motion.spatial(), label = "pair")
    val over = if (on) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surface
    val alpha by animateFloatAsState(if (on) 1f else QUIET_ALPHA, Motion.effects(), label = "pairAlpha")
    val fill = color.copy(alpha = alpha)
    val edge = fillEdge(fill.compositeOver(over), over)
    Canvas(modifier.height(height)) {
        val h = (size.height * shown.value * entrance()).coerceAtLeast(MIN_BAR.toPx())
        drawRoundRect(fill, Offset(0f, size.height - h), Size(size.width, h), CornerRadius(TOP_RADIUS.toPx()))
        drawFillEdge(edge, Offset(0f, size.height - h), Size(size.width, h), TOP_RADIUS.toPx())
    }
}

private val PAIRS_HEIGHT = 96.dp
/** Two bars take this share of a month's slot, but no wider than [PAIR_MAX] (six months: 14 dp each). */
private const val PAIR_SHARE = 0.72f
private val PAIR_MAX = 31.dp
private val BAR_GAP = 3.dp
private val TOP_RADIUS = 4.dp
/** A zero month is still a visible stub on the axis. */
private val MIN_BAR = 2.dp
/** The months not shown: the same colours, faded, so the shown one leads. */
private const val QUIET_ALPHA = 0.45f
