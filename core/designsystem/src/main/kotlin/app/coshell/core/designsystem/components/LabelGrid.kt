package app.coshell.core.designsystem.components

import app.coshell.core.designsystem.theme.selectionColors
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
import app.coshell.core.designsystem.tokens.PillShape
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * Short labels [columns] to a row (months «янв … дек», quarters, years): 44 dp capsules on the filled track, the
 * [selected] one — neon + 2 dp frame in ink + bold (board Style1). One of them is always the choice — like radio buttons.
 */
@Composable
fun CoShellLabelGrid(labels: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier, columns: Int = 4) {
    val colors = MaterialTheme.colorScheme
    val sel = selectionColors()
    val shape = PillShape
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        labels.indices.chunked(columns).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                row.forEach { index ->
                    val on = index == selected
                    Box(
                        Modifier
                            .weight(1f)
                            .height(Sizes.cellHeight)
                            .background(if (on) sel.fill else colors.surfaceContainerHigh, shape)
                            .then(if (on) Modifier.fillEdge(sel.fill, shape, colors.surfaceContainerHigh) else Modifier)
                            .semantics { this.selected = on }
                            .tactileClickable(Tactile.Control, shape, role = Role.RadioButton) { onSelect(index) },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            labels[index],
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (on) FontWeight.Bold else FontWeight.Normal,
                            color = if (on) sel.content else colors.onSurfaceVariant,
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
