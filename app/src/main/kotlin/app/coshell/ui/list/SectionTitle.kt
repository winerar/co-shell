package app.coshell.ui.list

import app.coshell.core.designsystem.components.CoShellSectionLabel
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.coshell.core.designsystem.tokens.Spacing

/** Small heading over a group of rows (import wizard steps, settings). */
@Composable
fun SectionTitle(text: String) {
    CoShellSectionLabel(text, Modifier.padding(start = Spacing.l, end = Spacing.l, top = Spacing.l, bottom = Spacing.xs))
}
