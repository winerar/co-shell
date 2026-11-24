package app.coshell.accounts.edit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.accounts.edit.AccountEditUiState
import app.coshell.accounts.edit.AccountEditUiState.BalanceError
import app.coshell.core.designsystem.components.CoShellAmountField

/**
 * «Исправить баланс»: the real balance of the account. The hint says the current one, then the difference that
 * «Сохранить баланс» records as a «Корректировка баланса» operation (not spending or income) right away (#581).
 */
@Composable
internal fun ActualBalanceField(state: AccountEditUiState, onValueChange: (String) -> Unit) {
    CoShellAmountField(
        value = state.actualBalance,
        onValueChange = onValueChange,
        label = stringResource(if (state.prepaid) R.string.account_balance_on_card else R.string.account_balance_actual),
        currency = state.currency,
        error = when (state.actualBalanceError) {
            BalanceError.INVALID -> stringResource(R.string.account_initial_balance_error)
            BalanceError.TOO_MANY_FRACTION_DIGITS -> stringResource(R.string.account_initial_balance_fraction_error)
            null -> null
        },
        hint = state.adjustment?.let { stringResource(R.string.account_balance_adjust_hint, it) }
            ?: stringResource(if (state.prepaid) R.string.account_balance_on_card_hint else R.string.account_balance_now_hint, state.currentBalance),
    )
}
