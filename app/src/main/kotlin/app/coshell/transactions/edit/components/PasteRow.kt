package app.coshell.transactions.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellNoticePill
import app.coshell.core.designsystem.components.CoShellOutlinedButton
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.edit.TransactionEditActions
import app.coshell.transactions.edit.TransactionEditUiState
import app.coshell.core.designsystem.R as DesignR

/**
 * «Вставить из буфера» (#321, board `Clip321Row`): a quiet row above the accounts while the amount is empty. The clipboard is read only
 * on the tap (Android 12+ shows its own notice at every read); no amount in the text — the pill «В буфере нет суммы» under the row.
 */
@Composable
internal fun PasteRow(state: TransactionEditUiState, actions: TransactionEditActions) {
    Column(Modifier.fillMaxWidth().padding(horizontal = Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        CoShellOutlinedButton(onClick = actions.onPasteClipboard, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                Icon(painterResource(DesignR.drawable.ic_content_paste), contentDescription = null, modifier = Modifier.size(Sizes.tileIcon))
                Text(stringResource(R.string.entry_paste_clipboard))
            }
        }
        CoShellNoticePill(
            visible = state.clipboardNone,
            icon = DesignR.drawable.ic_content_paste,
            text = stringResource(R.string.entry_clipboard_none),
            onClick = actions.onClipboardNoneDismiss,
            dismissLabel = stringResource(R.string.entry_clipboard_none_close),
            onDismiss = actions.onClipboardNoneDismiss,
        )
    }
}
