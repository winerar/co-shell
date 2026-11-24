package app.coshell.core.designsystem.charts

import app.coshell.core.designsystem.theme.secondaryInk
import app.coshell.core.designsystem.amount.shownAmount
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.RateTextStyle
import app.coshell.core.designsystem.tokens.Spacing

/**
 * One bar of a horizontal-bars chart (board `RepA`, «На что ушли деньги»): [leading] (a category badge), the [label],
 * «290 · 38%» on the right, under them the bar of [fraction] (of the chart's [ChartScale]) in the category [color] and
 * the thin grey [ghost] — where the past period was; [badge] — a pill before the value («↑ 45%», #238). Bars grow from
 * the left the first time they show and slide when the numbers change. TalkBack reads the texts, then
 * [badgeDescription] («на 45 процентов больше обычного») and [ghostDescription] («в октябре 300»); [caption] — a grey
 * line under the bar («Дом 262 · Продукты 70», #189). A tap — [onClick].
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CoShellBarRow(
    label: String,
    value: String,
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
    share: String? = null,
    ghost: Float? = null,
    ghostDescription: String? = null,
    leading: (@Composable () -> Unit)? = null,
    badge: (@Composable () -> Unit)? = null,
    badgeDescription: String? = null,
    caption: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val state = listOfNotNull(badgeDescription, ghostDescription).joinToString(", ").ifEmpty { null }
    val clickable = if (onClick != null) Modifier.tactileClickable(onClick = onClick) else Modifier
    Row(
        modifier
            .fillMaxWidth()
            .then(clickable)
            .semantics(mergeDescendants = true) { state?.let { stateDescription = it } }
            .padding(vertical = Spacing.s),
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading?.invoke()
        Column(Modifier.weight(1f)) {
            LabelAndValue(
                label = {
                    Text(label, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                trailing = {
                    // A flow: when even the value alone is wide, the pill goes above it rather than squeezing it.
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s, Alignment.End), itemVerticalAlignment = Alignment.CenterVertically) {
                        badge?.invoke()
                        ValueAndShare(value, share)
                    }
                },
            )
            ChartBar(fraction, color, ghost, Modifier.padding(top = Spacing.xs))
            caption?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * The name on the left, the value (with the pill) on the right. The name keeps at least [LABEL_MIN_WIDTH] — when the
 * value would leave it less, the value moves under the name (aligned to the end) instead of squeezing the name to «Ко…».
 */
@Composable
private fun LabelAndValue(label: @Composable () -> Unit, trailing: @Composable () -> Unit) {
    Layout(content = { label(); trailing() }) { measurables, constraints ->
        val max = constraints.maxWidth
        val loose = Constraints(maxWidth = max)
        val trailingNatural = measurables[1].measure(loose)
        val gap = Spacing.m.roundToPx()
        val sideBySide = max - trailingNatural.width - gap >= LABEL_MIN_WIDTH.roundToPx()
        val labelPlaceable = measurables[0].measure(
            Constraints(maxWidth = if (sideBySide) max - trailingNatural.width - gap else max),
        )
        if (sideBySide) {
            val height = maxOf(labelPlaceable.height, trailingNatural.height)
            layout(max, height) {
                labelPlaceable.placeRelative(0, (height - labelPlaceable.height) / 2)
                trailingNatural.placeRelative(max - trailingNatural.width, (height - trailingNatural.height) / 2)
            }
        } else {
            layout(max, labelPlaceable.height + trailingNatural.height) {
                labelPlaceable.placeRelative(0, 0)
                trailingNatural.placeRelative(max - trailingNatural.width, labelPlaceable.height)
            }
        }
    }
}

/** «290 · 38%»: the value in the main colour, the share grey. */
@Composable
private fun ValueAndShare(value: String, share: String?) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val shown = shownAmount(value)
    Text(
        buildAnnotatedString {
            append(shown)
            share?.let { withStyle(SpanStyle(color = muted)) { append(" · $it") } }
        },
        style = RateTextStyle.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize),
        maxLines = 1,
        softWrap = false,
    )
}

/** The bar itself: the shared [CoShellMeter] on its track; the ghost (past period) is its mark, in the second colour (#442 Б). */
@Composable
private fun ChartBar(fraction: Float, color: Color, ghost: Float?, modifier: Modifier) {
    CoShellMeter(fraction, color, modifier, mark = ghost, markColor = secondaryInk(), grow = true)
}

private val LABEL_MIN_WIDTH = 96.dp
