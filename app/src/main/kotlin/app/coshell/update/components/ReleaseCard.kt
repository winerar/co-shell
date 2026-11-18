package app.coshell.update.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.charts.CoShellChangePill
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.theme.secondaryInk
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.update.NoteKind
import app.coshell.core.domain.update.noteEntries

/**
 * Notes of a release: «Что нового в 0.37» before the update, «Что в этой версии» when it is installed. Every point has a
 * label — «Новое» (the primary colour) or «Исправлено» (the second one), poll 2026-11-18.
 */
@Composable
internal fun ReleaseCard(title: String, notes: String, modifier: Modifier = Modifier) {
    CoShellCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = secondaryInk())
            ReleaseNotesList(notes)
        }
    }
}

/** The points of [notes], each with its label; also in the «Обновлено» window. */
@Composable
internal fun ReleaseNotesList(notes: String, modifier: Modifier = Modifier) {
    val entries = remember(notes) { noteEntries(notes) }
    val colors = MaterialTheme.colorScheme
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        entries.forEach { entry ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s), verticalAlignment = Alignment.Top) {
                when (entry.kind) {
                    NoteKind.New -> CoShellChangePill(stringResource(R.string.update_note_new), colors.primary)
                    NoteKind.Fixed -> CoShellChangePill(stringResource(R.string.update_note_fixed), colors.secondary)
                }
                Text(entry.text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            }
        }
    }
}
