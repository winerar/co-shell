package app.coshell.accounts.edit.components

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
import app.coshell.accounts.edit.AccountEditActions
import app.coshell.accounts.edit.AccountEditUiState
import app.coshell.core.designsystem.components.CoShellDetailTile
import app.coshell.core.designsystem.components.CoShellFormSection
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.model.Account
import app.coshell.ui.form.NumberSheet
import app.coshell.core.designsystem.R as DesignR

/**
 * «Для чеков» (#577, board `AccountEdit` + `AccountDigits`): the last digits of the card or account as a bank receipt prints them.
 * A receipt with these digits chooses this account by itself; «Какой это счёт?» in the entry sheet fills them too.
 */
@Composable
internal fun ReceiptDigitsSection(state: AccountEditUiState, actions: AccountEditActions) {
    var open by rememberSaveable { mutableStateOf(false) }
    CoShellFormSection(stringResource(R.string.account_digits_section)) {
        CoShellDetailTile(
            icon = painterResource(DesignR.drawable.ic_account_balance_wallet),
            label = stringResource(R.string.account_digits_tile),
            value = if (state.cardDigits.isBlank()) stringResource(R.string.account_digits_empty) else "…${state.cardDigits}",
            onClick = { open = true },
            modifier = Modifier.fillMaxWidth(),
            empty = state.cardDigits.isBlank(),
        )
    }
    if (open) {
        NumberSheet(
            title = stringResource(R.string.account_digits_sheet_title),
            initial = state.cardDigits,
            maxWhole = Account.CARD_DIGITS,
            onDone = actions.onCardDigitsChange,
            onClosed = { open = false },
            placeholder = "····",
            // None (not linked) or all four — what a bank prints; anything between would not match a receipt.
            accept = { it.isEmpty() || it.length == Account.CARD_DIGITS },
        ) {
            CoShellHint(stringResource(R.string.account_digits_field_hint))
            CoShellHint(stringResource(R.string.account_digits_sheet_sub))
        }
    }
}
