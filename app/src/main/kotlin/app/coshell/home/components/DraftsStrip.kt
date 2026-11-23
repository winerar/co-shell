package app.coshell.home.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellMenuRow
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.R as DesignR

/**
 * «Быстрые заметки» under the balance (#641, poll 2026-11-23 «Б», board `NbNotesB`; was «Черновики · N» only while drafts wait, #586):
 * always shown; «Записать» opens the note, the row opens the drafts while any wait, otherwise the note too.
 */
@Composable
internal fun DraftsStrip(count: Int, onDrafts: () -> Unit, onNotes: () -> Unit, modifier: Modifier = Modifier) {
    CoShellCard(modifier.fillMaxWidth()) {
        CoShellMenuRow(
            icon = DesignR.drawable.ic_edit_note,
            color = categoryColor(CategoryPalette.colors[STRIP_COLOR]),
            title = stringResource(R.string.notes_title),
            subtitle = if (count > 0) stringResource(R.string.notes_strip_drafts, count) else stringResource(R.string.notes_menu_hint),
            onClick = if (count > 0) onDrafts else onNotes,
            trailing = { CoShellButton(onClick = onNotes) { Text(stringResource(R.string.notes_strip_write), maxLines = 1) } },
        )
    }
}

private const val STRIP_COLOR = 13

@Preview(showBackground = true)
@Composable
private fun DraftsStripPreview() {
    CoShellTheme { DraftsStrip(3, {}, {}) }
}
