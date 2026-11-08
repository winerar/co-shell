package app.coshell.transactions.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.coshell.R
import app.coshell.transactions.edit.TransactionEditUiState.CategoryGroup
import app.coshell.core.designsystem.R as DesignR

/**
 * Top-level categories as chips; folders carry a folder icon. When a folder (or one of its
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
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            groups.forEach { group ->
                FilterChip(
                    selected = group.item.id == selectedId || group.item.id == openFolderId,
                    onClick = { onSelect(group.item.id) },
                    label = { Text(group.item.name) },
                    leadingIcon = if (group.isFolder) {
                        {
                            Icon(
                                painterResource(DesignR.drawable.ic_folder),
                                contentDescription = null,
                                modifier = Modifier.size(FilterChipDefaults.IconSize),
                            )
                        }
                    } else {
                        null
                    },
                )
            }
        }
        val folder = groups.firstOrNull { it.item.id == openFolderId } ?: return@Column
        Text(
            stringResource(R.string.transaction_category_folder, folder.item.name),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            folder.children.forEach { child ->
                FilterChip(
                    selected = child.id == selectedId,
                    onClick = { onSelect(child.id) },
                    label = { Text(child.name) },
                )
            }
        }
    }
}
