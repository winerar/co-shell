package app.coshell.transactions.edit.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import app.coshell.R
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.transactions.edit.TransactionEditUiState.AmountError

/** Big amount input with the account's currency symbol as a suffix. */
@Composable
internal fun AmountField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    currencySymbol: String?,
    error: AmountError?,
    modifier: Modifier = Modifier,
    hint: String? = null,
) {
    val supporting = error?.let { stringResource(it.messageRes) } ?: hint
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text("0") },
        suffix = currencySymbol?.let { { Text(it) } },
        isError = error != null,
        supportingText = supporting?.let { { Text(it) } },
        textStyle = AmountTextStyle.merge(MaterialTheme.typography.headlineSmall),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
        modifier = modifier,
    )
}

private val AmountError.messageRes: Int
    get() = when (this) {
        AmountError.REQUIRED -> R.string.transaction_amount_required
        AmountError.INVALID -> R.string.transaction_amount_invalid
        AmountError.TOO_MANY_FRACTION_DIGITS -> R.string.transaction_amount_fraction
        AmountError.NOT_POSITIVE -> R.string.transaction_amount_not_positive
    }
