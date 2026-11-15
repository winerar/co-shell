package app.coshell.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * Short labels [columns] to a row (board `RepPeriod`: months «янв … дек», quarters, years): rounded cells on the
 * container tone, the [selected] one filled with the accent. One of them is always the choice — like radio buttons.
 */
@Composable
fun CoShellLabelGrid(labels: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier, columns: Int = 4) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        labels.indices.chunked(columns).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                row.forEach { index ->
                    val on = index == selected
                    Box(
                        Modifier
                            .weight(1f)
                            .height(Sizes.pillHeight)
                            .background(if (on) colors.primaryContainer else colors.surfaceContainerHigh, shape)
                            .semantics { this.selected = on }
                            .tactileClickable(Tactile.Control, shape, role = Role.RadioButton) { onSelect(index) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            labels[index],
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (on) colors.onPrimaryContainer else colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                repeat(columns - row.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}
