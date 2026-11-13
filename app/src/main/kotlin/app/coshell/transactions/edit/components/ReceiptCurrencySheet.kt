package app.coshell.transactions.edit.components

import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.designsystem.components.CodeKeyboard
import app.coshell.core.designsystem.components.CoShellTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import app.coshell.core.designsystem.components.CoShellSheet
import app.coshell.core.designsystem.components.CoShellSheetDialogProperties
import app.coshell.core.designsystem.components.CoShellSheetLayout
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.transactions.edit.TransactionEditActions
import app.coshell.transactions.edit.TransactionEditUiState
import app.coshell.transactions.edit.TransactionEditUiState.ReceiptCurrencyError

/**
 * "Receipt in another currency": e.g. a 15 000 ARS purchase paid from a USDT card. The amount and the rate live
 * on the «Чек» line of [AmountBlock]; here — the receipt currency: chips of the used ones and a code field.
 * Choosing a chip closes the sheet.
 */
@Composable
internal fun ReceiptCurrencySheet(state: TransactionEditUiState, actions: TransactionEditActions, onClosed: () -> Unit) {
    Dialog(onDismissRequest = onClosed, properties = CoShellSheetDialogProperties) {
        CoShellSheet(onClosed = onClosed) { close ->
            CoShellSheetLayout(
                title = stringResource(R.string.transaction_receipt_currency),
                actionLabel = stringResource(R.string.keypad_done),
                onAction = close,
            ) {
                ReceiptCurrencyContent(state) { code, pick ->
                    actions.onReceiptCurrencyChange(code)
                    if (pick) close()
                }
            }
        }
    }
}

/** Inside of [ReceiptCurrencySheet]: [onChange] gets the code and whether it was a chip (then the sheet closes). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ReceiptCurrencyContent(state: TransactionEditUiState, onChange: (code: String, pick: Boolean) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        val choices = state.receiptCurrencyChoices
        if (choices.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                choices.forEach { code ->
                    CoShellFilterChip(
                        selected = state.receiptCurrency == code,
                        onClick = { onChange(code, true) },
                        label = { Text(code) },
                    )
                }
            }
        }
        CoShellTextField(
            value = state.receiptCurrency,
            onValueChange = { onChange(it, false) },
            label = stringResource(R.string.transaction_receipt_currency),
            error = state.receiptCurrencyError?.let { stringResource(it.messageRes) },
            keyboardOptions = CodeKeyboard,
        )
        Text(
            stringResource(R.string.transaction_receipt_explain),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

internal val ReceiptCurrencyError.messageRes: Int
    get() = when (this) {
        ReceiptCurrencyError.INVALID -> R.string.transaction_receipt_currency_invalid
        ReceiptCurrencyError.SAME_AS_ACCOUNT -> R.string.transaction_receipt_currency_same
    }
