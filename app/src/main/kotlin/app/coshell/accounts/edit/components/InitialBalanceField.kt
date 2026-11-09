package app.coshell.accounts.edit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.accounts.edit.AccountEditUiState.BalanceError
import app.coshell.core.designsystem.components.CoShellAmountField

/** Initial balance as typed; empty means zero. The hint turns into the error when input is bad. */
@Composable
internal fun InitialBalanceField(value: String, error: BalanceError?, onValueChange: (String) -> Unit) {
    CoShellAmountField(
        value = value,
        onValueChange = onValueChange,
        label = stringResource(R.string.account_initial_balance),
        error = when (error) {
            BalanceError.INVALID -> stringResource(R.string.account_initial_balance_error)
            BalanceError.TOO_MANY_FRACTION_DIGITS -> stringResource(R.string.account_initial_balance_fraction_error)
            null -> null
        },
        hint = stringResource(R.string.account_initial_balance_hint),
    )
}
