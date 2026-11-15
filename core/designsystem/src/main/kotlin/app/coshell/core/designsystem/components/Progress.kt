package app.coshell.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
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
