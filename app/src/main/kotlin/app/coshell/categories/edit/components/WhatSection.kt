package app.coshell.categories.edit.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.categories.labelRes
import app.coshell.core.designsystem.components.CoShellFormSection
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellSegmented
import app.coshell.core.model.CategoryType

/**
 * «Что это» (#290, board `Fb290FA`): «Категория | Папка» (one «+» creates both, owner 2026-11-09) over «Расходы |
 * Доходы» (К3). The type is locked, with a hint, inside a folder or with subcategories.
 */
@Composable
internal fun WhatSection(folder: Boolean, type: CategoryType, typeLocked: Boolean, onKindSelect: (Boolean) -> Unit, onTypeSelect: (CategoryType) -> Unit) {
    CoShellFormSection(stringResource(R.string.category_section_what)) {
        val kinds = listOf(false to R.string.category_kind_category, true to R.string.category_kind_folder)
        CoShellSegmented(
            labels = kinds.map { stringResource(it.second) },
            selected = kinds.indexOfFirst { it.first == folder },
            onSelect = { onKindSelect(kinds[it].first) },
            modifier = Modifier.fillMaxWidth(),
        )
        val types = CategoryType.entries
        CoShellSegmented(
            labels = types.map { stringResource(it.labelRes) },
            selected = types.indexOf(type),
            onSelect = { onTypeSelect(types[it]) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !typeLocked,
        )
        if (typeLocked) CoShellHint(stringResource(R.string.category_type_locked))
    }
}
