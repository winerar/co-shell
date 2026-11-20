package app.coshell.transactions.edit.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.transactions.edit.TransactionEditUiState.CategoryGroup
import app.coshell.transactions.edit.categoryChoiceOptions
import app.coshell.transactions.edit.leafCount
import app.coshell.ui.form.ChoiceSheet

/**
 * «Все категории · 24» over the entry sheet (#439, poll Б): the one category list of the app — folders with their
 * subcategories under a caption, badges, a search; a tap picks the category and closes the sheet.
 */
@Composable
internal fun AllCategoriesSheet(groups: List<CategoryGroup>, selectedId: String?, onSelect: (String) -> Unit, onClosed: () -> Unit) {
    val noGroup = stringResource(R.string.transaction_categories_no_folder)
    val parentSubtitle = stringResource(R.string.category_kind_folder)
    val options = remember(groups, noGroup, parentSubtitle) { categoryChoiceOptions(groups, noGroup, parentSubtitle) }
    ChoiceSheet(
        title = stringResource(R.string.transaction_categories_all_title, groups.leafCount),
        options = options,
        selected = selectedId,
        // ChoiceSheet's onPick must close the sheet itself: onDismiss is not called after a pick (#439).
        onPick = { id ->
            onSelect(id)
            onClosed()
        },
        onDismiss = onClosed,
        searchPlaceholder = stringResource(R.string.transaction_categories_search),
        alwaysSearch = true,
    )
}
