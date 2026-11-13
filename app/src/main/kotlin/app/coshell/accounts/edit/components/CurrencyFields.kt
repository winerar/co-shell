package app.coshell.accounts.edit.components

import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.designsystem.components.CodeKeyboard
import app.coshell.core.designsystem.components.CoShellTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.accounts.edit.AccountEditUiState
import app.coshell.accounts.edit.AccountEditUiState.CurrencyError
import app.coshell.core.designsystem.components.CoShellFilterChip

/** Currency sheet body: suggestion chips plus a code field; both disabled once the account has transactions. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CurrencyFields(state: AccountEditUiState, onCurrencyChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        if (!state.currencyLocked) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                state.suggestedCurrencies.forEach { code ->
                    CoShellFilterChip(
                        selected = state.currency == code,
                        onClick = { onCurrencyChange(code) },
                        label = { Text(code) },
                    )
                }
            }
        }
        CoShellTextField(
            value = state.currency,
            onValueChange = onCurrencyChange,
            label = stringResource(R.string.account_currency_code),
            enabled = !state.currencyLocked,
            error = when (state.currencyError) {
                CurrencyError.INVALID -> stringResource(R.string.account_currency_error)
                CurrencyError.LOCKED -> stringResource(R.string.account_currency_locked)
                null -> null
            },
            hint = if (state.currencyLocked) stringResource(R.string.account_currency_locked) else null,
            keyboardOptions = CodeKeyboard,
        )
    }
}
