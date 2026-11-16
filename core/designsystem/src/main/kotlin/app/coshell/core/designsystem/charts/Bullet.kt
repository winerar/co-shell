package app.coshell.core.designsystem.charts

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.core.designsystem.theme.LocalStatusPalette
import app.coshell.core.designsystem.theme.progressColors

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
) {
    CoShellMeter(fraction, bulletColor(tone), modifier, mark)
}

/** The fill colour of [tone], also for text that speaks about the same state. */
@Composable
fun bulletColor(tone: BulletTone): Color = when (tone) {
    BulletTone.NORMAL -> progressColors().indicator
    BulletTone.WARNING -> LocalStatusPalette.current.warning
    BulletTone.DANGER -> LocalAmountPalette.current.expense
    BulletTone.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant
}

