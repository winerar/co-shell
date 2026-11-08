package app.coshell.categories.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.coshell.R
import app.coshell.categories.edit.CategoryEditUiState.Option
import app.coshell.ui.form.FormHint
import app.coshell.ui.form.FormSection

/** Which folder the category is in: "No folder" plus one chip per folder. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FolderSection(folders: List<Option>, parentId: String?, onSelect: (String?) -> Unit) {
    FormSection(stringResource(R.string.category_folder)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = parentId == null,
                onClick = { onSelect(null) },
                label = { Text(stringResource(R.string.category_folder_none)) },
            )
            folders.forEach { folder ->
                FilterChip(
                    selected = parentId == folder.id,
                    onClick = { onSelect(folder.id) },
                    label = { Text(folder.name) },
                )
            }
        }
    }
}

/** Categories to put inside this one; choosing any turns it into a folder. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ChildrenSection(candidates: List<Option>, childIds: Set<String>, onToggle: (String) -> Unit) {
    FormSection(stringResource(R.string.category_children)) {
        FormHint(stringResource(R.string.category_children_hint))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            candidates.forEach { candidate ->
                FilterChip(
                    selected = candidate.id in childIds,
                    onClick = { onToggle(candidate.id) },
                    label = { Text(candidate.name) },
                )
            }
        }
    }
}
