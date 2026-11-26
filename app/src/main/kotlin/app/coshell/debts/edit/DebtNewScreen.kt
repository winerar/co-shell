package app.coshell.debts.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellDatePicker
import app.coshell.core.designsystem.components.CoShellDetailTile
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellSectionLink
import app.coshell.core.designsystem.components.CoShellSegmented
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.core.designsystem.components.rememberDatePick
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.form.AmountSheetField
import app.coshell.ui.form.ChoiceSheet
import app.coshell.ui.sheet.EditSheetLayout
import app.coshell.ui.sheet.ModalSheet
import app.coshell.core.designsystem.R as DesignR

/** What the sheet can ask for. */
data class DebtNewActions(
    val onDirection: (Int) -> Unit = {},
    val onPerson: (String) -> Unit = {},
    val onAmount: (String) -> Unit = {},
    val onAccount: (String) -> Unit = {},
    val onDue: (Long?) -> Unit = {},
    val onNote: (String) -> Unit = {},
    val onSave: () -> Unit = {},
)

/** «Новый долг» (#552, board `DebtNew`): the form of one sheet; «Сохранить» pinned at the bottom. */
@Composable
fun DebtNewScreen(state: DebtNewUiState, actions: DebtNewActions) {
    var accountOpen by rememberSaveable { mutableStateOf(false) }
    var dueOpen by rememberSaveable { mutableStateOf(false) }
    val lent = state.direction == 0
    EditSheetLayout(title = stringResource(R.string.debts_new_title), onSave = actions.onSave) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            CoShellSegmented(
                labels = listOf(stringResource(R.string.debts_tab_lent), stringResource(R.string.debts_tab_borrowed)),
                selected = state.direction,
                onSelect = actions.onDirection,
                modifier = Modifier.fillMaxWidth(),
            )
            CoShellTextField(
                value = state.person,
                onValueChange = actions.onPerson,
                label = stringResource(R.string.debts_field_person),
                error = if (state.personError) stringResource(R.string.debts_error_person) else null,
                modifier = Modifier.fillMaxWidth(),
            )
            AmountSheetField(
                value = state.amount,
                onValueChange = actions.onAmount,
                label = stringResource(R.string.debts_field_amount),
                currency = state.currency,
                error = if (state.amountError) stringResource(R.string.debts_error_amount) else null,
                modifier = Modifier.fillMaxWidth(),
            )
            CoShellDetailTile(
                icon = painterResource(DesignR.drawable.ic_account_balance_wallet),
                label = stringResource(if (lent) R.string.debts_field_account_lent else R.string.debts_field_account_borrowed),
                value = state.accountName ?: stringResource(R.string.debts_error_account),
                onClick = { accountOpen = true },
                empty = state.accountName == null,
            )
            CoShellDetailTile(
                icon = painterResource(DesignR.drawable.ic_calendar_today),
                label = stringResource(R.string.debts_field_due),
                value = state.dueText ?: stringResource(R.string.debts_due_none),
                onClick = { dueOpen = true },
                empty = state.dueText == null,
            )
            if (state.dueText != null) CoShellSectionLink(stringResource(R.string.debts_due_none), onClick = { actions.onDue(null) })
            CoShellTextField(
                value = state.note,
                onValueChange = actions.onNote,
                label = stringResource(R.string.debts_field_note),
                modifier = Modifier.fillMaxWidth(),
            )
            CoShellHint(stringResource(R.string.debts_new_hint))
        }
    }
    if (accountOpen) {
        ChoiceSheet(
            title = stringResource(if (lent) R.string.debts_field_account_lent else R.string.debts_field_account_borrowed),
            options = state.accounts,
            selected = state.accountId,
            onPick = { accountOpen = false; actions.onAccount(it) },
            onDismiss = { accountOpen = false },
        )
    }
    if (dueOpen) {
        val pick = rememberDatePick(state.dueMillisUtc ?: System.currentTimeMillis())
        ModalSheet(
            title = stringResource(R.string.debts_field_due),
            actionLabel = stringResource(R.string.keypad_done),
            onAction = { close -> pick.millis?.let(actions.onDue); close() },
            onClosed = { dueOpen = false },
        ) { CoShellDatePicker(pick) }
    }
}
