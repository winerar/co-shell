package app.coshell.transactions.edit.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellTagChip
import app.coshell.core.designsystem.components.CoShellTagMoreChip
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.edit.TransactionEditActions
import app.coshell.transactions.edit.TransactionEditUiState

/**
 * Tags under the categories (#293, board `Fb129A`): the recent tags as pills — a tap switches one on or off —
 * and «Все 34» that opens the «Теги» sheet (search, every tag, a new one). No tags yet — only «＋ Тег».
 */
@Composable
internal fun TagRow(state: TransactionEditUiState, actions: TransactionEditActions, modifier: Modifier = Modifier) {
    var sheetOpen by rememberSaveable { mutableStateOf(false) }
    Row(
        modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        state.tagRow.forEach { tag ->
            CoShellTagChip(tag.name, tag.color, selected = tag.id in state.tagIds, onClick = { actions.onTagToggle(tag.id) })
        }
        val total = state.tags.options.size
        CoShellTagMoreChip(
            label = if (total == 0) stringResource(R.string.tags_add) else stringResource(R.string.tags_all, total),
            onClick = { sheetOpen = true },
        )
    }
    if (sheetOpen) TagPickerSheet(state, actions, onClosed = { sheetOpen = false })
}
