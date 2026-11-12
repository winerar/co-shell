package app.coshell.categories.edit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.categories.labelRes
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellSection
import app.coshell.core.designsystem.components.CoShellSegmented
import app.coshell.core.model.CategoryType

/** «Расходы | Доходы» segments (К3); locked (with a hint) when the category is inside a folder or has children. */
@Composable
internal fun CategoryTypeSection(selected: CategoryType, locked: Boolean, onSelect: (CategoryType) -> Unit) {
    CoShellSection(stringResource(R.string.category_type)) {
        val types = CategoryType.entries
        CoShellSegmented(
            labels = types.map { stringResource(it.labelRes) },
            selected = types.indexOf(selected),
            onSelect = { onSelect(types[it]) },
            enabled = !locked,
        )
        if (locked) CoShellHint(stringResource(R.string.category_type_locked))
    }
}
