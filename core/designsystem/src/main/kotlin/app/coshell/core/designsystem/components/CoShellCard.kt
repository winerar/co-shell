package app.coshell.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A block on the screen (owner's choice, design-questions §4): background of the screen, thin
 * `outlineVariant` border, 16 dp corners, no shadow. [accent] — the main block of a screen (total on
 * «Обзор»): the border takes the primary neon. [onClick] makes the whole card a tap target.
 */
@Composable
fun CoShellCard(
    modifier: Modifier = Modifier,
    accent: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val border = BorderStroke(
        1.dp,
        if (accent) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant,
    )
    val colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    val shape = MaterialTheme.shapes.large
    if (onClick != null) {
        OutlinedCard(onClick = onClick, modifier = modifier, shape = shape, colors = colors, border = border, content = content)
    } else {
        OutlinedCard(modifier = modifier, shape = shape, colors = colors, border = border, content = content)
    }
}
