package app.coshell.core.designsystem.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import app.coshell.core.designsystem.theme.secondaryInk
import app.coshell.core.designsystem.components.drawFillEdge
import app.coshell.core.designsystem.components.fillEdge
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Sizes

/**
 * The one bar of the app (owner's poll 2026-11-16 #456 «Б»): an 8 dp capsule on a neutral track, the fill is
 * [fraction] of it in [color], the thin vertical [mark] (0…1, `null` — none) sticks out above and below with a halo in
 * the surface colour: the budget's «norm for today» or a chart row's «last period». [grow] — the fill grows from the
 * left the first time it shows (charts). Purely visual: the screen says the same in words next to it.
 * [CoShellBullet] and the bars of [CoShellBarRow] are this component.
 */
@Composable
fun CoShellMeter(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
    mark: Float? = null,
    markColor: Color = secondaryInk(),
    grow: Boolean = false,
) {
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    // Neon on a pale card melts into it: a thin deep edge round the fill (#442), none when the fill stands out.
    val edge = fillEdge(color, track)
    val halo = MaterialTheme.colorScheme.surface
    val entrance = if (grow) rememberChartEntrance() else null
    val shown by animateFloatAsState(fraction.coerceIn(0f, 1f), Motion.spatial(), label = "meter")
    // The mark (the past period, the pace) moves with the fill when the numbers change (owner 2026-11-27).
    val markAt by animateFloatAsState(mark?.coerceIn(0f, 1f) ?: 0f, Motion.spatial(), label = "meterMark")
    val markShown by animateFloatAsState(if (mark == null) 0f else 1f, Motion.effects(), label = "meterMarkShown")
    val overhang = MARK_OVERHANG
    Canvas(modifier.fillMaxWidth().padding(vertical = overhang).height(Sizes.bullet)) {
        val radius = CornerRadius(size.height / 2)
        drawRoundRect(track, cornerRadius = radius)
        val length = size.width * shown * (entrance?.value ?: 1f)
        if (length > 0f) {
            // A sliver narrower than the bar is tall would draw as a dot; keep it at least round.
            val fill = Size(length.coerceAtLeast(size.height), size.height)
            drawRoundRect(color, size = fill, cornerRadius = radius)
            drawFillEdge(edge, Offset.Zero, fill, radius.x)
        }
        if (markShown > 0f) markAt.let { at ->
            val stroke = MARK_WIDTH.toPx()
            val x = (size.width * at).coerceIn(stroke / 2, size.width - stroke / 2)
            val out = overhang.toPx()
            // A halo of the surface keeps the mark visible where the fill of the same ink runs under it.
            drawLine(halo, Offset(x, -out), Offset(x, size.height + out), strokeWidth = stroke * 2, alpha = markShown)
            drawLine(markColor, Offset(x, -out), Offset(x, size.height + out), strokeWidth = stroke, alpha = markShown)
        }
    }
}

private val MARK_OVERHANG = 3.dp
private val MARK_WIDTH = 2.dp
