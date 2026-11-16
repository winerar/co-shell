package app.coshell.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.tokens.PillShape
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * «‹ Октябрь 2026 ›» (board `CompChoice`, «Сегменты» family, #367 А): a capsule track [Sizes.rowDenseMinHeight] high, the
 * label in the middle and a round button at each end that moves the period [onEarlier] / [onLater].
 */
@Composable
fun CoShellStepper(
    label: String,
    onEarlier: () -> Unit,
    onLater: () -> Unit,
    earlierLabel: String,
    laterLabel: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth().height(Sizes.rowDenseMinHeight).background(MaterialTheme.colorScheme.surfaceContainer, PillShape).padding(horizontal = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoShellIconButton(onClick = onEarlier) {
            Icon(painterResource(R.drawable.ic_chevron_right), earlierLabel, Modifier.rotate(180f))
        }
        Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        CoShellIconButton(onClick = onLater) {
            Icon(painterResource(R.drawable.ic_chevron_right), laterLabel)
        }
    }
}
