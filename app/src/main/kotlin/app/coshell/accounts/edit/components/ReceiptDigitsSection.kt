package app.coshell.accounts.edit.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import app.coshell.R
import app.coshell.accounts.edit.AccountEditActions
import app.coshell.accounts.edit.AccountEditUiState
import app.coshell.core.designsystem.components.CoShellDetailTile
import app.coshell.core.designsystem.components.CoShellFormSection
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.ui.sheet.FormSheet
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
        FormSheet(
            title = stringResource(R.string.account_digits_sheet_title),
            onClosed = { open = false },
        ) {
            CoShellTextField(
                value = state.cardDigits,
                onValueChange = actions.onCardDigitsChange,
                label = stringResource(R.string.account_digits_field),
                hint = stringResource(R.string.account_digits_field_hint),
                error = if (state.cardDigitsError) stringResource(R.string.account_digits_error) else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            )
            CoShellHint(stringResource(R.string.account_digits_sheet_sub))
        }
    }
}
