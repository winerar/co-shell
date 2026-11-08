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
import app.coshell.categories.labelRes
import app.coshell.core.model.CategoryType
import app.coshell.ui.form.FormHint
import app.coshell.ui.form.FormSection

/** Expense/income chips; locked (with a hint) when the category is inside a folder or has children. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CategoryTypeSection(selected: CategoryType, locked: Boolean, onSelect: (CategoryType) -> Unit) {
    FormSection(stringResource(R.string.category_type)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CategoryType.entries.forEach { type ->
                FilterChip(
                    selected = selected == type,
                    onClick = { onSelect(type) },
                    enabled = !locked,
                    label = { Text(stringResource(type.labelRes)) },
                )
            }
        }
        if (locked) FormHint(stringResource(R.string.category_type_locked))
    }
}
