package app.coshell.accounts.edit.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import app.coshell.R
import app.coshell.accounts.edit.AccountEditUiState.BalanceError

/** Initial balance as typed; empty means zero. The hint turns into the error when input is bad. */
@Composable
internal fun InitialBalanceField(value: String, error: BalanceError?, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.account_initial_balance)) },
        placeholder = { Text("0") },
        isError = error != null,
        supportingText = {
            Text(
                stringResource(
                    when (error) {
                        BalanceError.INVALID -> R.string.account_initial_balance_error
                        BalanceError.TOO_MANY_FRACTION_DIGITS -> R.string.account_initial_balance_fraction_error
                        null -> R.string.account_initial_balance_hint
                    },
                ),
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
        modifier = Modifier.fillMaxWidth(),
    )
}
