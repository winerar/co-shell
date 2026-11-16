package app.coshell.transactions.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.CoShellAmountSlot
import app.coshell.core.designsystem.components.CoShellDivider
import app.coshell.core.designsystem.components.CoShellFormSection
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.components.CoShellTagMoreChip
import app.coshell.core.designsystem.components.CoShellSectionLink
import app.coshell.core.designsystem.motion.CoShellExpandable
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.edit.TransactionEditActions
import app.coshell.transactions.edit.TransactionEditUiState
import app.coshell.transactions.edit.TransactionEditUiState.KeypadTarget
import app.coshell.transactions.edit.receiptRateText
import app.coshell.ui.format.formatDayMonth

/**
 * «Сумма» of the entry sheet (#290, board `Fb290A`): the big amount; for a transfer between currencies the arriving
 * amount under it; with a receipt in another currency the caption says «· списано» and a dashed line «Чек 13 300 ARS
 * · курс · ✕» closes the card. «+ Чек» on the right of the caption switches the receipt on; for a payment from
 * «Скоро» it is «Пропустить этот раз» (#404).
 */
@Composable
internal fun AmountBlock(state: TransactionEditUiState, actions: TransactionEditActions, amountColor: Color) {
    val receiptLink: (@Composable () -> Unit)? = if (state.payment != null) {
        // A payment from «Скоро» (board `Rec143Post`): the caption offers to skip it instead.
        { CoShellSectionLink(stringResource(R.string.payment_skip), onClick = actions.onPaymentSkip) }
    } else if (!state.isTransfer && !state.hasReceipt) {
        { CoShellSectionLink(stringResource(R.string.entry_receipt_add), onClick = { actions.onReceiptToggle(true) }) }
    } else {
        null
    }
    CoShellFormSection(
        title = stringResource(if (state.hasReceipt) R.string.entry_section_amount_debited else R.string.entry_section_amount),
        trailing = receiptLink,
    ) {
        AmountSlot(
            label = null,
            text = state.amount,
            currency = state.account?.currency,
            active = state.keypadTarget == KeypadTarget.AMOUNT,
            error = state.amountError,
            onClick = { actions.onKeypadTarget(KeypadTarget.AMOUNT) },
            big = true,
            hint = state.suggestedRateDate?.takeIf { state.amountSuggested }?.let {
                stringResource(R.string.transaction_amount_suggested, formatDayMonth(it))
            } ?: templateHint(state),
            color = amountColor,
        )
        CoShellExpandable(state.needsToAmount) {
            AmountSlot(
                label = stringResource(R.string.transaction_to_amount),
                text = state.toAmount,
                currency = state.toAccount?.currency,
                active = state.keypadTarget == KeypadTarget.TO_AMOUNT,
                error = state.toAmountError,
                onClick = { actions.onKeypadTarget(KeypadTarget.TO_AMOUNT) },
                hint = state.toAccount?.let { stringResource(R.string.transaction_to_amount_hint, it.name) },
            )
        }
        CoShellExpandable(state.hasReceipt) { ReceiptLine(state, actions) }
    }
}

/**
 * «Чек 13 300 ARS ▾ ··· 1 USDT = 1 015 ARS ✕»: the receipt amount (a tap — the keypad types here) with its currency
 * pill (a tap — the currency sheet), the rate of the purchase under it and «✕» that drops the receipt.
 */
@Composable
private fun ReceiptLine(state: TransactionEditUiState, actions: TransactionEditActions) {
    var currencyOpen by rememberSaveable { mutableStateOf(false) }
    val error = state.receiptAmountError?.let { stringResource(it.messageRes) }
        ?: state.receiptCurrencyError?.let { stringResource(it.messageRes) }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
        CoShellDivider()
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            CoShellAmountSlot(
                text = state.receiptAmount,
                onClick = { actions.onKeypadTarget(KeypadTarget.RECEIPT) },
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.entry_receipt),
                trailing = {
                    CoShellTagMoreChip(
                        state.receiptCurrency.ifEmpty { stringResource(R.string.transaction_receipt_currency) } + " ▾",
                        onClick = { currencyOpen = true },
                    )
                },
                active = state.keypadTarget == KeypadTarget.RECEIPT,
                isError = state.receiptAmountError != null || state.receiptCurrencyError != null,
                message = error ?: receiptRateText(state),
            )
            CoShellIconButton(onClick = { actions.onReceiptToggle(false) }) {
                Icon(painterResource(DesignR.drawable.ic_close), stringResource(R.string.entry_receipt_remove))
            }
        }
    }
    if (currencyOpen) ReceiptCurrencySheet(state, actions, onClosed = { currencyOpen = false })
}
