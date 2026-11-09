package app.coshell.transactions.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryIcons
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.transactions.edit.TransactionEditUiState.CategoryGroup
import app.coshell.transactions.edit.TransactionEditUiState.CategoryItem

/**
 * Top-level categories as chips with their icons. When a folder (or one of its
 * subcategories) is selected, its subcategories appear in a second row below.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CategoryChips(
    groups: List<CategoryGroup>,
    selectedId: String?,
    openFolderId: String?,
    onSelect: (String) -> Unit,
) {
    if (groups.isEmpty()) {
        Text(
            stringResource(R.string.transaction_no_categories),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            groups.forEach { group ->
                CoShellFilterChip(
                    selected = group.item.id == selectedId || group.item.id == openFolderId,
                    onClick = { onSelect(group.item.id) },
                    label = { Text(group.item.name) },
                    leadingIcon = { ChipIcon(group.item) },
                )
            }
        }
        val folder = groups.firstOrNull { it.item.id == openFolderId } ?: return@Column
        Text(
            stringResource(R.string.transaction_category_folder, folder.item.name),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
            folder.children.forEach { child ->
                CoShellFilterChip(
                    selected = child.id == selectedId,
                    onClick = { onSelect(child.id) },
                    label = { Text(child.name) },
                    leadingIcon = { ChipIcon(child) },
                )
            }
        }
    }
}

/** The category's own icon in its colour, chip-sized. */
@Composable
private fun ChipIcon(item: CategoryItem) {
    Icon(
        painterResource(CategoryIcons.drawableFor(item.icon)),
        contentDescription = null,
        tint = item.color?.let(::Color) ?: MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(FilterChipDefaults.IconSize),
    )
}
