package app.coshell.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.theme.progressColors
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Sizes

/** Indeterminate progress line, full width of its slot, 8 dp like every bar (board Style7). Screens use this, not the Material one. */
@Composable
fun CoShellLinearProgress(modifier: Modifier = Modifier) {
    val colors = progressColors()
    LinearProgressIndicator(
        modifier = modifier.fillMaxWidth().height(Sizes.bullet),
        color = colors.indicator,
        trackColor = colors.track,
        strokeCap = StrokeCap.Round,
    )
}

/** Determinate line ([progress] 0…1, e.g. how full a text is); the fill glides to a new value on the spatial spring. */
@Composable
fun CoShellLinearProgress(progress: () -> Float, modifier: Modifier = Modifier) {
    val colors = progressColors()
    val shown by animateFloatAsState(progress(), Motion.spatial(), label = "progress")
    LinearProgressIndicator(
        progress = { shown },
        modifier = modifier.fillMaxWidth().height(Sizes.bullet),
        color = colors.indicator,
        trackColor = colors.track,
        strokeCap = StrokeCap.Round,
        drawStopIndicator = {},
    )
}

/**
 * Spinner (board `CompNew`, «Разделитель и загрузка», #367 А): a ring of the progress colours — [size] 24 dp in a row,
 * 44 dp alone on a screen (the default). Screens use this, not the Material one.
 */
@Composable
fun CoShellLoading(modifier: Modifier = Modifier, size: Dp = 44.dp) {
    val colors = progressColors()
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = colors.indicator,
        trackColor = colors.track,
        strokeWidth = size / RING_RATIO,
        strokeCap = StrokeCap.Round,
    )
}

private const val RING_RATIO = 11

// Not in the screenshot catalog: the ring is animated, so a frame would never be the same twice.
@Preview(name = "Loading · light", showBackground = true)
@Composable
private fun LoadingLightPreview() = CoShellTheme(darkTheme = false) { LoadingSizes() }

@Preview(name = "Loading · dark", showBackground = true)
@Composable
private fun LoadingDarkPreview() = CoShellTheme(darkTheme = true) { LoadingSizes() }

@Composable
private fun LoadingSizes() = Row {
    CoShellLoading(size = 24.dp)
    CoShellLoading()
}
