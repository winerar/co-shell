package app.coshell.accounts.list.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.accounts.labelRes
import app.coshell.accounts.list.AccountsUiState
import app.coshell.core.designsystem.category.AccountBadge
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellReorderRow
import app.coshell.core.designsystem.components.rememberReorderState
import app.coshell.ui.order.DragHandle
import app.coshell.ui.order.OrderScreen

/** «Порядок счетов» (#338, board `Fb338Acc`): active accounts in one list; [onMove] — from → to, indices in [rows]. */
@Composable
internal fun AccountsOrder(rows: List<AccountsUiState.Row>, onMove: (Int, Int) -> Unit, onDone: () -> Unit) {
    val state = rememberReorderState()
    OrderScreen(
        title = stringResource(R.string.accounts_order_title),
        hint = stringResource(R.string.accounts_order_hint),
        state = state,
        keys = rows.map { it.id },
        onDrop = { drop -> onMove(rows.indexOfFirst { it.id == drop.key }, drop.index) },
        onDone = onDone,
    ) {
        rows.forEachIndexed { i, row ->
            CoShellReorderRow(
                state = state,
                key = row.id,
                moveUpLabel = stringResource(R.string.order_move_up),
                onMoveUp = if (i > 0) ({ onMove(i, i - 1) }) else null,
                moveDownLabel = stringResource(R.string.order_move_down),
                onMoveDown = if (i < rows.lastIndex) ({ onMove(i, i + 1) }) else null,
            ) {
                CoShellListRow(
                    title = { Text(row.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    supporting = { Text(stringResource(if (row.prepaid) R.string.account_type_prepaid else row.type.labelRes), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leading = { AccountBadge(row.name, row.color) },
                    trailing = { DragHandle() },
                )
            }
        }
    }
}
