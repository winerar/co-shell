package app.coshell.categories.list.components

import app.coshell.core.designsystem.tokens.Sizes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.categories.list.CategoriesUiState
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.category.CategoryBadge
import app.coshell.core.designsystem.components.CoShellExpandIcon
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.tokens.Spacing

/** A top-level row: a plain category, or a folder with child count and expand arrow. */
@Composable
internal fun GroupRow(
    group: CategoriesUiState.Group,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!group.isFolder) {
        CategoryRow(group.item, indent = false, onClick = onClick, modifier = modifier)
        return
    }
    CoShellListRow(
        title = group.item.name,
        subtitle = pluralStringResource(R.plurals.categories_in_folder, group.children.size, group.children.size),
        leading = { CategoryBadge(group.item.icon, group.item.color) },
        trailing = {
            CoShellIconButton(onClick = onToggle) {
                // The arrow turns over while the folder's rows unfold under it.
                CoShellExpandIcon(
                    expanded = group.expanded,
                    contentDescription = stringResource(
                        if (group.expanded) R.string.categories_collapse else R.string.categories_expand,
                    ),
                )
            }
        },
        onClick = onClick,
        modifier = modifier,
    )
}

/** A category with its icon badge; [indent] for subcategories inside an open folder. */
@Composable
internal fun CategoryRow(item: CategoriesUiState.Item, indent: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    CoShellListRow(
        title = item.name,
        leading = {
            // Subcategories: a smaller badge, shifted under the folder's badge.
            if (indent) CategoryBadge(item.icon, item.color, Modifier.padding(start = Spacing.xl), size = Sizes.badgeSmall)
            else CategoryBadge(item.icon, item.color)
        },
        onClick = onClick,
        modifier = modifier,
    )
}

