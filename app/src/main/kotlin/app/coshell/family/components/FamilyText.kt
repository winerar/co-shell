package app.coshell.family.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.tokens.Spacing

/** Explanation paragraph used by every block of the family screen. */
@Composable
internal fun FamilyHint(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = Spacing.l, vertical = Spacing.s),
    )
}

/** Section title inside the family screen. */
@Composable
internal fun FamilySectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = accentInk(),
        modifier = Modifier.padding(start = Spacing.l, end = Spacing.l, top = Spacing.l),
    )
}
