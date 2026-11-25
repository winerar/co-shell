package app.coshell.transactions.edit.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.components.CoShellNoticePill
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.edit.TransactionEditActions
import app.coshell.transactions.edit.TransactionEditUiState
import app.coshell.core.designsystem.R as DesignR

/**
 * «Вставить из буфера» (#321, board `Clip321Row`): a round icon in the type row next to the QR and «Шаблоны». The clipboard is read
 * only on the tap (Android 12+ shows its own notice at every read); no amount in the text — [ClipboardNonePill] under the row.
 */
@Composable
internal fun PasteButton(actions: TransactionEditActions) {
    CoShellIconButton(onClick = actions.onPasteClipboard) {
        Icon(painterResource(DesignR.drawable.ic_content_paste), contentDescription = stringResource(R.string.entry_paste_clipboard))
    }
}

/** «В буфере нет суммы. Скопируйте текст с ней.» — under the type row after a paste found nothing. */
@Composable
internal fun ClipboardNonePill(state: TransactionEditUiState, actions: TransactionEditActions) {
    CoShellNoticePill(
        visible = state.clipboardNone,
        icon = DesignR.drawable.ic_content_paste,
        text = stringResource(R.string.entry_clipboard_none),
        onClick = actions.onClipboardNoneDismiss,
        dismissLabel = stringResource(R.string.entry_clipboard_none_close),
        onDismiss = actions.onClipboardNoneDismiss,
        modifier = Modifier.fillMaxWidth().padding(start = Spacing.l, end = Spacing.l, top = Spacing.xs),
    )
}
