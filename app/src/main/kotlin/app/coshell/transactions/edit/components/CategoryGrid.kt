package app.coshell.transactions.edit.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryBadge
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.motion.CoShellExpandable
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.selectionPop
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.edit.TransactionEditUiState.CategoryGroup
import app.coshell.transactions.edit.TransactionEditUiState.CategoryItem

private const val COLUMNS = 4

/**
 * Categories of the entry sheet as a 4-column grid of badges (recently used first). A folder unfolds its
 * subcategories in a second grid under it; the selected one gets a ring in its colour and pops.
 */
@Composable
internal fun CategoryGrid(
    groups: List<CategoryGroup>,
    selectedId: String?,
    openFolderId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (groups.isEmpty()) {
        Text(
            stringResource(R.string.transaction_no_categories),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
        return
    }
    val folder = groups.firstOrNull { it.item.id == openFolderId }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Grid(groups.map { it.item }, selectedId = selectedId, openId = openFolderId, onSelect = onSelect)
        CoShellExpandable(folder != null) {
            if (folder == null) return@CoShellExpandable
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(
                    stringResource(R.string.transaction_category_folder, folder.item.name),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.s),
                )
                Grid(folder.children, selectedId = selectedId, openId = null, onSelect = onSelect, small = true)
            }
        }
    }
}

@Composable
private fun Grid(items: List<CategoryItem>, selectedId: String?, openId: String?, onSelect: (String) -> Unit, small: Boolean = false) {
    items.chunked(COLUMNS).forEach { line ->
        Row {
            line.forEach { item ->
                Cell(item, item.id == selectedId || item.id == openId, small, Modifier.weight(1f)) { onSelect(item.id) }
            }
            repeat(COLUMNS - line.size) { Box(Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun Cell(item: CategoryItem, selected: Boolean, small: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val target = when {
        !selected -> Color.Transparent
        item.color != null -> categoryColor(item.color)
        else -> accentInk()
    }
    val ring by animateColorAsState(target, Motion.effects(), label = "categoryRing")
    Column(
        modifier = modifier
            .tactileClickable(Tactile.Control, MaterialTheme.shapes.medium, onClick = onClick)
            .padding(vertical = Spacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Box(Modifier.selectionPop(selected).border(Sizes.selectionRing, ring, CircleShape).padding(Spacing.xs)) {
            CategoryBadge(item.icon, item.color, size = if (small) Sizes.badgeSmall else Sizes.badge)
        }
        Text(
            item.name,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
