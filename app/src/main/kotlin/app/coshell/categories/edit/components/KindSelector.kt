package app.coshell.categories.edit.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellSegmented

/** «Категория | Папка» at the top of the editor: one «+» creates both (owner 2026-11-09). */
@Composable
internal fun KindSelector(folder: Boolean, onSelect: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val kinds = listOf(false to R.string.category_kind_category, true to R.string.category_kind_folder)
    CoShellSegmented(
        labels = kinds.map { stringResource(it.second) },
        selected = kinds.indexOfFirst { it.first == folder },
        onSelect = { onSelect(kinds[it].first) },
        modifier = modifier.fillMaxWidth(),
    )
}
