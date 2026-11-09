package app.coshell.transactions.edit.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellAmountField
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
    CoShellAmountField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        modifier = modifier,
        currency = currencySymbol,
        error = error?.let { stringResource(it.messageRes) },
        hint = hint,
    )
}

private val AmountError.messageRes: Int
    get() = when (this) {
        AmountError.REQUIRED -> R.string.transaction_amount_required
        AmountError.INVALID -> R.string.transaction_amount_invalid
        AmountError.TOO_MANY_FRACTION_DIGITS -> R.string.transaction_amount_fraction
        AmountError.NOT_POSITIVE -> R.string.transaction_amount_not_positive
    }
