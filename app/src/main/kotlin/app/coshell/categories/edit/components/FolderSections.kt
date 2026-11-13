package app.coshell.categories.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.categories.edit.CategoryEditUiState.Option
import app.coshell.core.designsystem.components.CoShellDetailTile
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.components.CoShellFormSection
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.form.ChoiceDialog
import app.coshell.ui.form.ChoiceOption
import app.coshell.ui.sheet.FormSheet
import app.coshell.core.designsystem.R as DesignR

/** «Папка» (#290): one tile «Внутри папки · Машина»; a tap opens the list of folders with «Без папки» first. */
@Composable
internal fun FolderSection(folders: List<Option>, parentId: String?, onSelect: (String?) -> Unit) {
    var open by rememberSaveable { mutableStateOf(false) }
    val none = stringResource(R.string.category_folder_none)
    CoShellFormSection(stringResource(R.string.category_folder)) {
        CoShellDetailTile(
            icon = painterResource(DesignR.drawable.ic_folder),
            label = stringResource(R.string.category_folder_inside),
            value = folders.firstOrNull { it.id == parentId }?.name ?: none,
            onClick = { open = true },
            modifier = Modifier.fillMaxWidth(),
            empty = parentId == null,
        )
    }
    if (open) {
        ChoiceDialog(
            title = stringResource(R.string.category_folder),
            options = listOf(ChoiceOption<String?>(null, none)) + folders.map { ChoiceOption<String?>(it.id, it.name) },
            selected = parentId,
            onPick = {
                onSelect(it)
                open = false
            },
            onDismiss = { open = false },
        )
    }
}

/**
 * «Категории в папке» (#290): one tile with the chosen names; a tap opens a sheet of chips to tick — the form
 * stays the same height with 3 categories or 30.
 */
@Composable
internal fun ChildrenSection(candidates: List<Option>, childIds: Set<String>, onToggle: (String) -> Unit) {
    var open by rememberSaveable { mutableStateOf(false) }
    val chosen = candidates.filter { it.id in childIds }
    CoShellFormSection(stringResource(R.string.category_children)) {
        CoShellDetailTile(
            icon = painterResource(DesignR.drawable.ic_folder_open),
            label = stringResource(R.string.category_children_pick),
            value = if (chosen.isEmpty()) stringResource(R.string.category_children_none) else chosen.joinToString { it.name },
            onClick = { open = true },
            modifier = Modifier.fillMaxWidth(),
            empty = chosen.isEmpty(),
        )
    }
    if (open) {
        FormSheet(stringResource(R.string.category_children), onClosed = { open = false }) {
            ChildrenChips(candidates, childIds, onToggle)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ChildrenChips(candidates: List<Option>, childIds: Set<String>, onToggle: (String) -> Unit) {
    CoShellHint(stringResource(R.string.category_children_hint))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
        candidates.forEach { candidate ->
            CoShellFilterChip(
                selected = candidate.id in childIds,
                onClick = { onToggle(candidate.id) },
                label = { Text(candidate.name) },
            )
        }
    }
}
