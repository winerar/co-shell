package app.coshell.ui.list

import app.coshell.core.designsystem.components.CoShellSectionLabel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.coshell.core.designsystem.tokens.Spacing

/** Small heading over a group of rows (import wizard steps, settings); [mark] — the «новое» pill right after it (`newMarkPill`). */
@Composable
fun SectionTitle(text: String, mark: (@Composable () -> Unit)? = null) {
    val padding = Modifier.padding(start = Spacing.l, end = Spacing.l, top = Spacing.l, bottom = Spacing.xs)
    if (mark == null) {
        CoShellSectionLabel(text, padding)
    } else {
        Row(padding, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            CoShellSectionLabel(text)
            mark()
        }
    }
}
