package app.coshell.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.core.designsystem.theme.LocalStatusPalette
import app.coshell.core.designsystem.theme.progressColors
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Sizes

/** Colour of a [CoShellBullet] fill: in plan — the palette accent, ahead of pace — warning, over — expense, nothing yet — grey. */
enum class BulletTone { NORMAL, WARNING, DANGER, IDLE }

/**
 * Bullet graph (budgets, mockup `BudA`): the fill is [fraction] of the track (spent / limit, capped at the
 * end), the thin vertical [mark] is where the fill "should be" today (0…1, `null` — no mark). The mark
 * sticks out above and below the track and has a halo, so it stays visible over the fill. Purely visual: the screen says
 * the same in words next to it.
 */
@Composable
fun CoShellBullet(
    fraction: Float,
    tone: BulletTone,
    modifier: Modifier = Modifier,
    mark: Float? = null,
    height: Dp = Sizes.bullet,
) {
    // Neutral in both themes: the light theme's progress track is the neon accent, an empty envelope would look full.
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    val halo = MaterialTheme.colorScheme.surface
    val fill = bulletColor(tone)
    val markColor = MaterialTheme.colorScheme.onSurface
    val shown by animateFloatAsState(fraction.coerceIn(0f, 1f), Motion.spatial(), label = "bullet")
    val overhang = MARK_OVERHANG
    Canvas(modifier.fillMaxWidth().padding(vertical = overhang).height(height)) {
        val radius = CornerRadius(size.height / 2)
        drawRoundRect(track, cornerRadius = radius)
        if (shown > 0f) {
            // A sliver narrower than the bar is tall would draw as a dot; keep it at least round.
            val width = (size.width * shown).coerceAtLeast(size.height)
            drawRoundRect(fill, size = Size(width, size.height), cornerRadius = radius)
        }
        mark?.coerceIn(0f, 1f)?.let { at ->
            val stroke = MARK_WIDTH.toPx()
            val x = (size.width * at).coerceIn(stroke / 2, size.width - stroke / 2)
            val out = overhang.toPx()
            // A halo of the surface keeps the mark visible where the fill of the same ink runs under it.
            drawLine(halo, Offset(x, -out), Offset(x, size.height + out), strokeWidth = stroke * 2)
            drawLine(markColor, Offset(x, -out), Offset(x, size.height + out), strokeWidth = stroke)
        }
    }
}

/** The fill colour of [tone], also for text that speaks about the same state. */
@Composable
fun bulletColor(tone: BulletTone): Color = when (tone) {
    BulletTone.NORMAL -> progressColors().indicator
    BulletTone.WARNING -> LocalStatusPalette.current.warning
    BulletTone.DANGER -> LocalAmountPalette.current.expense
    BulletTone.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant
}

private val MARK_OVERHANG = 3.dp
private val MARK_WIDTH = 2.dp
