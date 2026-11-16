package app.coshell.transactions.edit.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellAmountSlot
import app.coshell.transactions.edit.TransactionEditUiState.AmountError

/** The amount of the entry sheet on [CoShellAmountSlot]: [big] — the main one; [error] wins over [hint] under the field. */
@Composable
internal fun AmountSlot(
    label: String?,
    text: String,
    currency: String?,
    active: Boolean,
    error: AmountError?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    big: Boolean = false,
    hint: String? = null,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    CoShellAmountSlot(
        text = text,
        onClick = onClick,
        modifier = modifier,
        label = label,
        currency = currency,
        active = active,
        isError = error != null,
        message = error?.let { stringResource(it.messageRes) } ?: hint,
        main = big,
        color = color,
    )
}

internal val AmountError.messageRes: Int
    get() = when (this) {
        AmountError.REQUIRED -> R.string.transaction_amount_required
        AmountError.INVALID -> R.string.transaction_amount_invalid
        AmountError.TOO_MANY_FRACTION_DIGITS -> R.string.transaction_amount_fraction
        AmountError.NOT_POSITIVE -> R.string.transaction_amount_not_positive
    }
