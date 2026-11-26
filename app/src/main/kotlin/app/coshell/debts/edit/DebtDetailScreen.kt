package app.coshell.debts.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.charts.CoShellMeter
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellDetailTile
import app.coshell.core.designsystem.components.CoShellFormSection
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellOutlinedButton
import app.coshell.core.designsystem.theme.progressColors
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.form.AmountSheetField
import app.coshell.ui.form.ChoiceSheet
import app.coshell.ui.form.ConfirmDeleteDialog
import app.coshell.ui.form.ConfirmDialog
import app.coshell.ui.sheet.ModalSheet
import app.coshell.ui.sheet.EditSheetLayout
import app.coshell.core.designsystem.R as DesignR

/** What the card can ask for. */
data class DebtDetailActions(
    val onReturnAccount: (String) -> Unit = {},
    val onReturnAmount: (String) -> Unit = {},
    val onSaveReturn: () -> Unit = {},
    val onResetReturn: () -> Unit = {},
    val onClose: () -> Unit = {},
    val onDelete: () -> Unit = {},
)

/**
 * A debt's card (#552, board `DebtDetail`): what is left with a meter of what came back, the days and the note, the returns,
 * «Вернули часть» (a small sheet) and «Закрыть долг»; «Удалить» is asked twice.
 */
@Composable
fun DebtDetailScreen(state: DebtDetailUiState, actions: DebtDetailActions) {
    var returnOpen by rememberSaveable { mutableStateOf(false) }
    var closeAsk by rememberSaveable { mutableStateOf(false) }
    var deleteAsk by rememberSaveable { mutableStateOf(false) }
    EditSheetLayout(title = state.person, onSave = { returnOpen = true }, showSave = false) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            CoShellFormSection(title = stringResource(if (state.lent) R.string.debts_tab_lent else R.string.debts_tab_borrowed)) {
                Text(state.left, style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(R.string.debts_detail_summary, state.total, state.returned), style = MaterialTheme.typography.bodySmall)
                CoShellMeter(state.fraction, progressColors().indicator, Modifier.fillMaxWidth().padding(vertical = Spacing.s))
                state.dueText?.let { Text(stringResource(R.string.debts_due_date, it), style = MaterialTheme.typography.bodyMedium) }
                if (state.note.isNotEmpty()) Text(state.note, style = MaterialTheme.typography.bodyMedium)
            }
            if (state.returns.isNotEmpty()) {
                CoShellFormSection(title = stringResource(R.string.debts_detail_returns)) {
                    state.returns.forEach { androidx.compose.runtime.key(it.id) { CoShellListRow(title = it.text, compact = true) } }
                }
            }
            if (!state.closed) {
                CoShellButton(onClick = { returnOpen = true }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.debts_return)) }
                CoShellOutlinedButton(onClick = { closeAsk = true }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.debts_close)) }
            }
            CoShellOutlinedButton(onClick = { deleteAsk = true }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.action_delete)) }
        }
    }
    if (returnOpen) ReturnSheet(state, actions, onClosed = { returnOpen = false; actions.onResetReturn() })
    if (closeAsk) {
        ConfirmDialog(
            title = R.string.debts_close_title,
            text = R.string.debts_close_text,
            confirmLabel = R.string.debts_close,
            onConfirm = { closeAsk = false; actions.onClose() },
            onDismiss = { closeAsk = false },
        )
    }
    if (deleteAsk) {
        ConfirmDeleteDialog(
            title = R.string.debts_delete_title,
            text = R.string.debts_delete_text,
            onConfirm = { deleteAsk = false; actions.onDelete() },
            onDismiss = { deleteAsk = false },
        )
    }
}

@Composable
private fun ReturnSheet(state: DebtDetailUiState, actions: DebtDetailActions, onClosed: () -> Unit) {
    var accountOpen by rememberSaveable { mutableStateOf(false) }
    val label = stringResource(if (state.lent) R.string.debts_return_account_lent else R.string.debts_return_account_borrowed)
    ModalSheet(
        title = stringResource(R.string.debts_return),
        actionLabel = stringResource(R.string.debts_return_save),
        onAction = { close -> if (state.returnDone) close() else actions.onSaveReturn() },
        onClosed = onClosed,
    ) { close ->
        if (state.returnDone) androidx.compose.runtime.LaunchedEffect(Unit) { close() }
        AmountSheetField(
            value = state.returnAmount,
            onValueChange = actions.onReturnAmount,
            label = stringResource(R.string.debts_field_amount),
            currency = state.currency,
            error = state.returnError?.let { stringResource(it) },
            modifier = Modifier.fillMaxWidth(),
        )
        CoShellDetailTile(
            icon = painterResource(DesignR.drawable.ic_account_balance_wallet),
            label = label,
            value = state.returnAccountName ?: stringResource(R.string.debts_error_account),
            onClick = { accountOpen = true },
            empty = state.returnAccountName == null,
        )
    }
    if (accountOpen) {
        ChoiceSheet(
            title = label,
            options = state.accounts,
            selected = state.returnAccountId,
            onPick = { accountOpen = false; actions.onReturnAccount(it) },
            onDismiss = { accountOpen = false },
        )
    }
}
