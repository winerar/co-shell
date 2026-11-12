package app.coshell.core.designsystem.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.RateTextStyle
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * One bar of a horizontal-bars chart (board `RepA`, «На что ушли деньги»): [leading] (a category badge), the [label],
 * «290 · 38%» on the right, under them the bar of [fraction] (of the chart's [ChartScale]) in the category [color] and
 * the thin grey [ghost] — where the past period was. Bars grow from the left the first time they show and slide when
 * the numbers change. TalkBack reads the texts, then [ghostDescription] («в октябре 300»). A tap — [onClick].
 */
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
    onClick: (() -> Unit)? = null,
) {
    val clickable = if (onClick != null) Modifier.tactileClickable(onClick = onClick) else Modifier
    Row(
        modifier
            .fillMaxWidth()
            .then(clickable)
            .semantics(mergeDescendants = true) { ghostDescription?.let { stateDescription = it } }
            .padding(vertical = Spacing.s),
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading?.invoke()
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                ValueAndShare(value, share)
            }
            ChartBar(fraction, color, ghost, Modifier.padding(top = Spacing.xs))
        }
    }
}

/** «290 · 38%»: the value in the main colour, the share grey. */
@Composable
private fun ValueAndShare(value: String, share: String?) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Text(
        buildAnnotatedString {
            append(value)
            share?.let { withStyle(SpanStyle(color = muted)) { append(" · $it") } }
        },
        style = RateTextStyle.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize),
        maxLines = 1,
        modifier = Modifier.padding(start = Spacing.s),
    )
}

/** The bar itself: no track (the length speaks), the ghost sticks out above and below like the bullet's mark. */
@Composable
private fun ChartBar(fraction: Float, color: Color, ghost: Float?, modifier: Modifier) {
    val ghostColor = MaterialTheme.colorScheme.outline
    val entrance = rememberChartEntrance()
    val shown = animateFloatAsState(fraction.coerceIn(0f, 1f), Motion.spatial(), label = "bar")
    val overhang = GHOST_OVERHANG
    Canvas(modifier.fillMaxWidth().padding(vertical = overhang).height(Sizes.bulletLarge)) {
        val radius = CornerRadius(size.height / 2)
        val length = size.width * shown.value * entrance.value
        if (length > 0f) {
            // A sliver narrower than the bar is tall would draw as a dot; keep it at least round.
            drawRoundRect(color, size = Size(length.coerceAtLeast(size.height), size.height), cornerRadius = radius)
        }
        ghost?.coerceIn(0f, 1f)?.let { at ->
            val stroke = GHOST_WIDTH.toPx()
            val x = (size.width * at).coerceIn(stroke / 2, size.width - stroke / 2)
            val out = overhang.toPx()
            drawLine(ghostColor, Offset(x, -out), Offset(x, size.height + out), strokeWidth = stroke)
        }
    }
}

private val GHOST_OVERHANG = 3.dp
private val GHOST_WIDTH = 2.dp
