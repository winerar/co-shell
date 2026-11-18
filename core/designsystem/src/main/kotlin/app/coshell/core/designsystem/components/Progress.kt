package app.coshell.core.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp

import app.coshell.core.designsystem.charts.CoShellMeter
import app.coshell.core.designsystem.theme.progressColors
import app.coshell.core.designsystem.tokens.Sizes

/** Indeterminate progress line, full width of its slot, 8 dp like every bar (board Style7). Screens use this, not the Material one. */
@Composable
fun CoShellLinearProgress(modifier: Modifier = Modifier) {
    val colors = progressColors()
    LinearProgressIndicator(
        modifier = modifier.fillMaxWidth().height(Sizes.bullet).fillEdge(colors.track, CircleShape),
        color = colors.indicator,
        trackColor = colors.track,
        strokeCap = StrokeCap.Round,
    )
}

/** Determinate line ([progress] 0…1, e.g. how full a text is); the fill glides to a new value on the spatial spring. */
@Composable
fun CoShellLinearProgress(progress: () -> Float, modifier: Modifier = Modifier) {
    CoShellMeter(progress(), progressColors().indicator, modifier)
}

/**
 * Round spinner (board `CompNew`, «Разделитель и загрузка», #367 А): 24 dp in a row, 44 dp for a screen; the line
 * is a tenth of the size. Screens use this, not the Material one.
 */
@Composable
fun CoShellLoading(modifier: Modifier = Modifier, size: Dp = Sizes.loadingRow) {
    CoShellRing(size, size / 10, progress = null, modifier = modifier)
}
