package app.coshell.core.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import app.coshell.core.designsystem.theme.progressColors

/** Indeterminate progress line, full width of its slot. Screens use this, not the Material one. */
@Composable
fun CoShellLinearProgress(modifier: Modifier = Modifier) {
    val colors = progressColors()
    LinearProgressIndicator(
        modifier = modifier.fillMaxWidth(),
        color = colors.indicator,
        trackColor = colors.track,
        strokeCap = StrokeCap.Round,
    )
}
