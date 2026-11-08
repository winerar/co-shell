package app.coshell.categories.list.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.coshell.R
import app.coshell.categories.list.CategoriesUiState
import app.coshell.core.designsystem.category.CategoryBadge
import app.coshell.core.designsystem.R as DesignR

/** A top-level row: a plain category, or a folder with child count and expand arrow. */
@Composable
internal fun GroupRow(group: CategoriesUiState.Group, onClick: () -> Unit, onToggle: () -> Unit) {
    if (!group.isFolder) {
        CategoryRow(group.item, indent = false, onClick = onClick)
        return
    }
    ListItem(
        headlineContent = { Text(group.item.name) },
        supportingContent = {
            Text(pluralStringResource(R.plurals.categories_in_folder, group.children.size, group.children.size))
        },
        leadingContent = { CategoryBadge(group.item.icon, group.item.color) },
        trailingContent = {
            IconButton(onClick = onToggle) {
                Icon(
                    painterResource(DesignR.drawable.ic_keyboard_arrow_down),
                    contentDescription = stringResource(
                        if (group.expanded) R.string.categories_collapse else R.string.categories_expand,
                    ),
                    modifier = Modifier.rotate(if (group.expanded) 180f else 0f),
                )
            }
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

/** A category with its icon badge; [indent] for subcategories inside an open folder. */
@Composable
internal fun CategoryRow(item: CategoriesUiState.Item, indent: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(item.name) },
        leadingContent = { CategoryBadge(item.icon, item.color, size = if (indent) 32.dp else 40.dp) },
        modifier = Modifier.clickable(onClick = onClick).padding(start = if (indent) 40.dp else 0.dp),
    )
}
