package app.coshell.update.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.tokens.Spacing

/** Notes of a release: «Что нового в 0.37» before the update, «Что в этой версии» when it is installed. */
@Composable
internal fun ReleaseCard(title: String, notes: String, modifier: Modifier = Modifier) {
    CoShellCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = accentInk())
            Text(notes, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
