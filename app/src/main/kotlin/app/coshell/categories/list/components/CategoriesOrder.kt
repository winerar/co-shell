package app.coshell.categories.list.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.categories.list.OrderRow
import app.coshell.categories.list.dropPlace
import app.coshell.core.designsystem.category.CategoryBadge
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellReorderRow
import app.coshell.core.designsystem.components.rememberReorderState
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.domain.order.ManualOrder.Place
import app.coshell.ui.order.DragHandle
import app.coshell.ui.order.OrderScreen

/**
 * «Порядок категорий» (#338, board `Fb338Cat`): parents and their subcategories (indented); drag to reorder, onto the
 * middle of another category to make it a subcategory, out of a group to make it separate. [onMove] — the category and
 * its new [Place]; [canNest] — whether a category may go into another one.
 */
@Composable
internal fun CategoriesOrder(
    rows: List<OrderRow>,
    onMove: (String, Place) -> Unit,
    canNest: (String, String) -> Boolean,
    onDone: () -> Unit,
) {
    val state = rememberReorderState()
    val draggedName = rows.firstOrNull { it.id == state.dragging }?.name
    OrderScreen(
        title = stringResource(R.string.categories_order_title),
        hint = stringResource(R.string.categories_order_hint),
        state = state,
        keys = rows.map { it.id },
        onDrop = { drop -> (drop.key as String).let { key -> onMove(key, dropPlace(rows, key, drop.index, drop.into as String?)) } },
        onDone = onDone,
        canNest = { dragged, into -> canNest(dragged as String, into as String) },
    ) {
        rows.forEach { row ->
            val siblings = rows.filter { it.parentId == row.parentId }
            val at = siblings.indexOf(row)
            CoShellReorderRow(
                state = state,
                key = row.id,
                moveUpLabel = stringResource(R.string.order_move_up),
                onMoveUp = if (at > 0) ({ onMove(row.id, Place(row.parentId, at - 1)) }) else null,
                moveDownLabel = stringResource(if (row.parentId != null && at == siblings.lastIndex) R.string.order_move_out else R.string.order_move_down),
                onMoveDown = when {
                    at < siblings.lastIndex -> ({ onMove(row.id, Place(row.parentId, at + 1)) })
                    row.parentId != null -> ({ onMove(row.id, Place(null, rows.filter { it.parentId == null }.indexOfFirst { it.id == row.parentId } + 1)) })
                    else -> null
                },
            ) {
                val target = state.over == row.id && draggedName != null
                CoShellListRow(
                    title = { Text(row.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    supporting = if (target) ({ Text(stringResource(R.string.order_drop_into, draggedName.orEmpty()), maxLines = 2) }) else null,
                    leading = { CategoryBadge(row.icon, row.color) },
                    trailing = { DragHandle() },
                    modifier = if (row.parentId != null) Modifier.padding(start = Sizes.badge) else Modifier,
                )
            }
        }
    }
}
