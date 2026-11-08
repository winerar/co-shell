package app.coshell.categories.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.coshell.R
import app.coshell.categories.list.CategoriesUiState
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
        leadingContent = {
            Icon(
                painterResource(DesignR.drawable.ic_folder),
                contentDescription = null,
                tint = group.item.color?.let(::Color) ?: MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
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

/** A category with its colour dot; [indent] for subcategories inside an open folder. */
@Composable
internal fun CategoryRow(item: CategoriesUiState.Item, indent: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(item.name) },
        leadingContent = {
            Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(12.dp)
                        .background(item.color?.let(::Color) ?: MaterialTheme.colorScheme.outline, CircleShape),
                )
            }
        },
        modifier = Modifier.clickable(onClick = onClick).padding(start = if (indent) 40.dp else 0.dp),
    )
}
