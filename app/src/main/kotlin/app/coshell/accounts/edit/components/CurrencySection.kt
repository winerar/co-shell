package app.coshell.accounts.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.coshell.R
import app.coshell.accounts.edit.AccountEditUiState
import app.coshell.accounts.edit.AccountEditUiState.CurrencyError
import app.coshell.ui.form.FormSection

/** Currency: suggestion chips plus a code field; both disabled once the account has transactions. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CurrencySection(state: AccountEditUiState, onCurrencyChange: (String) -> Unit) {
    FormSection(stringResource(R.string.account_currency)) {
        if (!state.currencyLocked) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.suggestedCurrencies.forEach { code ->
                    FilterChip(
                        selected = state.currency == code,
                        onClick = { onCurrencyChange(code) },
                        label = { Text(code) },
                    )
                }
            }
        }
        val supportingRes = when {
            state.currencyError == CurrencyError.INVALID -> R.string.account_currency_error
            state.currencyError == CurrencyError.LOCKED || state.currencyLocked -> R.string.account_currency_locked
            else -> null
        }
        OutlinedTextField(
            value = state.currency,
            onValueChange = onCurrencyChange,
            label = { Text(stringResource(R.string.account_currency_code)) },
            enabled = !state.currencyLocked,
            isError = state.currencyError != null,
            supportingText = supportingRes?.let { res -> { Text(stringResource(res)) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Ascii,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
