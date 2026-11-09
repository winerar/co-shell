package app.coshell.categories.list.components

import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.designsystem.tokens.Sizes
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.categories.list.CategoriesActions
import app.coshell.categories.list.CategoriesUiState
import app.coshell.core.designsystem.components.CoShellTextButton
import app.coshell.navigation.CategoryEditDestination

/** Top-level categories; an expanded folder adds its children and "Add to folder". */
internal fun LazyListScope.groupItems(state: CategoriesUiState, actions: CategoriesActions) {
    val type = state.type.name
    state.groups.forEach { group ->
        item(key = group.item.id) {
            GroupRow(
                group = group,
                onClick = { actions.onOpen(CategoryEditDestination(categoryId = group.item.id)) },
                onToggle = { actions.onToggleFolder(group.item.id) },
            )
        }
        if (!group.isFolder || !group.expanded) return@forEach
        items(group.children, key = { it.id }) { child ->
            CategoryRow(
                item = child,
                indent = true,
                onClick = { actions.onOpen(CategoryEditDestination(categoryId = child.id)) },
            )
        }
        item(key = "add-to-${group.item.id}") {
            CoShellTextButton(
                onClick = { actions.onOpen(CategoryEditDestination(type = type, parentId = group.item.id)) },
                modifier = Modifier.padding(start = Spacing.l + Sizes.badge),
            ) { Text(stringResource(R.string.categories_add_to_folder)) }
        }
    }
}

/** "Show archived (N)" toggle and, when open, the archived categories. */
internal fun LazyListScope.archivedItems(state: CategoriesUiState, actions: CategoriesActions) {
    if (state.archived.isEmpty()) return
    item(key = "archived-toggle") {
        CoShellTextButton(onClick = actions.onToggleArchived, modifier = Modifier.padding(horizontal = Spacing.s)) {
            Text(
                stringResource(
                    if (state.showArchived) R.string.categories_hide_archived else R.string.categories_show_archived,
                    state.archived.size,
                ),
            )
        }
    }
    if (!state.showArchived) return
    items(state.archived, key = { "archived-${it.id}" }) { item ->
        CategoryRow(item, indent = false, onClick = { actions.onOpen(CategoryEditDestination(categoryId = item.id)) })
    }
}
