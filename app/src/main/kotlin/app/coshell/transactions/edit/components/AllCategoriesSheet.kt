package app.coshell.transactions.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellSearchField
import app.coshell.core.designsystem.components.CoShellSectionLabel
import app.coshell.core.designsystem.components.CoShellSheet
import app.coshell.core.designsystem.components.CoShellSheetDialogProperties
import app.coshell.core.designsystem.components.CoShellSheetLayout
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.edit.CategoryShelf
import app.coshell.transactions.edit.TransactionEditUiState.CategoryGroup
import app.coshell.transactions.edit.categoryShelves
import app.coshell.transactions.edit.leafCount

/**
 * «Все категории · 24» over the entry sheet (#290, board `Fb290ACats`): «Найти категорию» and every category by
 * folder — the same badges as the frequent grid. A tap picks the category and closes the sheet.
 */
@Composable
internal fun AllCategoriesSheet(groups: List<CategoryGroup>, selectedId: String?, onSelect: (String) -> Unit, onClosed: () -> Unit) {
    Dialog(onDismissRequest = onClosed, properties = CoShellSheetDialogProperties) {
        CoShellSheet(onClosed = onClosed) { close ->
            CoShellSheetLayout(
                title = stringResource(R.string.transaction_categories_all_title, groups.leafCount),
                actionLabel = "",
                onAction = close,
                showAction = false,
            ) {
                AllCategoriesContent(groups, selectedId) { id ->
                    onSelect(id)
                    close()
                }
            }
        }
    }
}

/** Inside of [AllCategoriesSheet]; apart for the screenshot test. */
@Composable
internal fun ColumnScope.AllCategoriesContent(groups: List<CategoryGroup>, selectedId: String?, onSelect: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    CoShellSearchField(
        value = query,
        onValueChange = { query = it },
        placeholder = stringResource(R.string.transaction_categories_search),
        clearLabel = stringResource(R.string.search_clear),
        modifier = Modifier.fillMaxWidth(),
    )
    val shelves = remember(groups, query) { categoryShelves(groups, query) }
    if (shelves.isEmpty()) {
        Text(
            stringResource(R.string.transaction_categories_nothing),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    shelves.forEach { shelf -> Shelf(shelf, selectedId, onSelect) }
}

@Composable
private fun Shelf(shelf: CategoryShelf, selectedId: String?, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        CoShellSectionLabel(shelf.title ?: stringResource(R.string.transaction_categories_no_folder))
        CategoryGrid(shelf.items.map { GridPlace.Category(it) }, selectedId, onSelect)
    }
}
