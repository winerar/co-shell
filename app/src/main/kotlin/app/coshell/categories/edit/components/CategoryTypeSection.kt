package app.coshell.categories.edit.components

import app.coshell.core.designsystem.tokens.Spacing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.categories.labelRes
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.model.CategoryType
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellSection

/** Expense/income chips; locked (with a hint) when the category is inside a folder or has children. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CategoryTypeSection(selected: CategoryType, locked: Boolean, onSelect: (CategoryType) -> Unit) {
    CoShellSection(stringResource(R.string.category_type)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            CategoryType.entries.forEach { type ->
                CoShellFilterChip(
                    selected = selected == type,
                    onClick = { onSelect(type) },
                    enabled = !locked,
                    label = { Text(stringResource(type.labelRes)) },
                )
            }
        }
        if (locked) CoShellHint(stringResource(R.string.category_type_locked))
    }
}
