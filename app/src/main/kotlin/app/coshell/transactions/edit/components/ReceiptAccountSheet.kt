package app.coshell.transactions.edit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.transactions.edit.ReceiptAccountAsk
import app.coshell.transactions.edit.TransactionEditActions
import app.coshell.transactions.edit.TransactionEditUiState
import app.coshell.ui.form.ChoiceOption
import app.coshell.ui.form.ChoiceSheet

/**
 * «Какой это счёт?» (#577, board `ReceiptWhichAccount`): the bank receipt names a card or an account by its last digits that no
 * account has yet. A tap on an account chooses it and saves the digits in it (shown in «Для чеков» of the account);
 * closing the sheet leaves the account as it is.
 */
@Composable
internal fun ReceiptAccountSheet(state: TransactionEditUiState, ask: ReceiptAccountAsk, actions: TransactionEditActions) {
    ChoiceSheet(
        title = stringResource(R.string.receipt_account_title),
        options = state.accounts.map { ChoiceOption(value = it.id, label = it.name, accountName = it.name, color = it.color) },
        selected = null,
        onPick = actions.onReceiptAccountPick,
        onDismiss = actions.onReceiptAccountSkip,
        hint = stringResource(if (ask.target) R.string.receipt_account_hint_to else R.string.receipt_account_hint, ask.digits),
    )
}
