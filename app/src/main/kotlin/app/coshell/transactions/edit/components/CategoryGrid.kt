package app.coshell.transactions.edit.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.core.designsystem.category.CategoryBadge
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.components.dashedBorder
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.selectionPop
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.edit.TransactionEditUiState.CategoryItem

private const val COLUMNS = 4

/** Something to put in a grid place: a category or the dashed «Все 24». */
internal sealed interface GridPlace {
    data class Category(val item: CategoryItem) : GridPlace
    data class All(val count: Int, val label: String, val onClick: () -> Unit) : GridPlace
}

/**
 * Categories as a 4-column grid of small badges (#290, board `Fb290A`): the selected one gets a ring in its colour
 * and pops. The frequent grid of the sheet ends with «Все 24» ([GridPlace.All]); the «Все категории» sheet uses
 * the same grid per folder.
 */
@Composable
internal fun CategoryGrid(places: List<GridPlace>, selectedId: String?, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        places.chunked(COLUMNS).forEach { line ->
            Row {
                line.forEach { place ->
                    val cell = Modifier.weight(1f)
                    when (place) {
                        is GridPlace.Category -> Cell(place.item, place.item.id == selectedId, cell) { onSelect(place.item.id) }
                        is GridPlace.All -> AllCell(place, cell)
                    }
                }
                repeat(COLUMNS - line.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun Cell(item: CategoryItem, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val target = when {
        !selected -> Color.Transparent
        item.color != null -> categoryColor(item.color)
        else -> accentInk()
    }
    val ring by animateColorAsState(target, Motion.effects(), label = "categoryRing")
    CellColumn(modifier, onClick) {
        Box(Modifier.selectionPop(selected).border(Sizes.selectionRing, ring, CircleShape).padding(Spacing.xxs)) {
            CategoryBadge(item.icon, item.color, size = Sizes.badgeSmall)
        }
        Label(item.name, selected)
    }
}

@Composable
private fun AllCell(place: GridPlace.All, modifier: Modifier) {
    val ink = accentInk()
    CellColumn(modifier, place.onClick) {
        Box(
            Modifier.padding(Spacing.xxs).size(Sizes.badgeSmall).dashedBorder(MaterialTheme.colorScheme.outline, Sizes.badgeSmall / 2),
            contentAlignment = Alignment.Center,
        ) {
            Text(place.count.toString(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ink)
        }
        Text(place.label, style = MaterialTheme.typography.labelMedium, color = ink, maxLines = 1)
    }
}

@Composable
private fun CellColumn(modifier: Modifier, onClick: () -> Unit, content: @Composable () -> Unit) {
    Column(
        modifier = modifier.tactileClickable(Tactile.Control, MaterialTheme.shapes.medium, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
    ) { content() }
}

@Composable
private fun Label(text: String, selected: Boolean) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(horizontal = Spacing.xxs),
    )
}
