package app.coshell.transactions.edit.components

import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.designsystem.components.CodeKeyboard
import app.coshell.core.designsystem.components.CoShellTextField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.motion.CoShellExpandable
import app.coshell.core.model.CurrencyCode
import app.coshell.transactions.edit.TransactionEditActions
import app.coshell.transactions.edit.TransactionEditUiState
import app.coshell.transactions.edit.TransactionEditUiState.ReceiptCurrencyError
import app.coshell.transactions.edit.receiptRateText

/**
 * "Receipt in another currency": e.g. a 15 000 ARS purchase paid from a USDT card.
 * The main amount stays what left the account; here go the receipt currency and amount. The chip opens
 * the fields as a drop-down: they unfold under it and fold back when it is switched off.
 */
@Composable
internal fun ReceiptSection(state: TransactionEditUiState, actions: TransactionEditActions, modifier: Modifier = Modifier) {
    Column(modifier) {
        CoShellFilterChip(
            selected = state.receiptEnabled,
            onClick = { actions.onReceiptToggle(!state.receiptEnabled) },
            label = { Text(stringResource(R.string.transaction_receipt_toggle)) },
        )
        CoShellExpandable(state.receiptEnabled) {
            // The gap under the chip is inside: it folds together with the fields.
            ReceiptFields(state, actions, Modifier.padding(top = Spacing.l))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReceiptFields(state: TransactionEditUiState, actions: TransactionEditActions, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        val choices = state.receiptCurrencyChoices
        if (choices.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                choices.forEach { code ->
                    CoShellFilterChip(
                        selected = state.receiptCurrency == code,
                        onClick = { actions.onReceiptCurrencyChange(code) },
                        label = { Text(code) },
                    )
                }
            }
        }
        CoShellTextField(
            value = state.receiptCurrency,
            onValueChange = actions.onReceiptCurrencyChange,
            label = stringResource(R.string.transaction_receipt_currency),
            error = state.receiptCurrencyError?.let { stringResource(it.messageRes) },
            keyboardOptions = CodeKeyboard,
        )
        AmountField(
            value = state.receiptAmount,
            onValueChange = actions.onReceiptAmountChange,
            label = stringResource(R.string.transaction_receipt_amount),
            currencyCode = CurrencyCode.parseOrNull(state.receiptCurrency)?.value,
            error = state.receiptAmountError,
            hint = receiptRateText(state)?.let { stringResource(R.string.transaction_receipt_rate, it) }
                ?: stringResource(R.string.transaction_receipt_hint),
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            stringResource(R.string.transaction_receipt_explain),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private val ReceiptCurrencyError.messageRes: Int
    get() = when (this) {
        ReceiptCurrencyError.INVALID -> R.string.transaction_receipt_currency_invalid
        ReceiptCurrencyError.SAME_AS_ACCOUNT -> R.string.transaction_receipt_currency_same
    }
