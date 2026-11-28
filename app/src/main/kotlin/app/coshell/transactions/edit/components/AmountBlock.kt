package app.coshell.transactions.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.amount.fitAmountAutoSize
import app.coshell.core.designsystem.components.CoShellAmountSlot
import app.coshell.core.designsystem.components.CoShellFormSection
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.components.CoShellTagMoreChip
import app.coshell.core.designsystem.components.CoShellSectionLink
import app.coshell.core.designsystem.motion.CoShellExpandable
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.model.TransactionType
import app.coshell.transactions.edit.TransactionEditActions
import app.coshell.transactions.edit.TransactionEditUiState
import app.coshell.transactions.edit.TransactionEditUiState.KeypadTarget
import app.coshell.transactions.edit.receiptRateText
import app.coshell.ui.format.formatDayMonth

/**
 * «Сумма» of the entry sheet (#290, board `Fb290A`): the big amount; for a transfer between currencies the arriving
 * amount under it; with a receipt in another currency the caption says «· списано» and one line «Покупка 13 300 ARS ▾
 * · курс · ✕» closes the card (#818, poll «А»). On the right of the caption: «Возврат» of an expense (#818: was a tile
 * in «Детали»), «+ Чаевые» of a new expense (#322), «+ Сумма покупки»; for a payment from «Скоро» — «Пропустить этот раз» (#404).
 */
@Composable
internal fun AmountBlock(state: TransactionEditUiState, actions: TransactionEditActions, amountColor: Color) {
    val receiptLink: (@Composable () -> Unit)? = if (state.payment != null) {
        // A payment from «Скоро» (board `Rec143Post`): the caption offers to skip it instead.
        { CoShellSectionLink(stringResource(R.string.payment_skip), onClick = actions.onPaymentSkip) }
    } else {
        val refund = state.type == TransactionType.EXPENSE
        val receipt = !state.isTransfer && !state.hasReceipt
        val tip = state.tipAvailable
        if (refund || receipt || tip) {
            {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s), verticalAlignment = Alignment.CenterVertically) {
                    if (refund) {
                        CoShellSectionLink(
                            stringResource(if (state.refund) R.string.entry_refund_on else R.string.entry_refund_link),
                            onClick = { actions.onRefundToggle(!state.refund) },
                        )
                    }
                    if (tip) CoShellSectionLink(stringResource(R.string.entry_tip_add), onClick = actions.onTipOpen)
                    if (receipt) CoShellSectionLink(stringResource(R.string.entry_receipt_add), onClick = { actions.onReceiptToggle(true) })
                }
            }
        } else {
            null
        }
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
 * «Покупка 13 300 ARS ▾ · 1 USDT = 1 015 ARS · ✕» in one line (#818): the compact amount (a tap — the keypad types
 * here) with its currency pill (a tap — the currency sheet), the rate of the purchase or the error next to it, «✕» drops it.
 */
@Composable
private fun ReceiptLine(state: TransactionEditUiState, actions: TransactionEditActions) {
    var currencyOpen by rememberSaveable { mutableStateOf(false) }
    val error = state.receiptAmountError?.let { stringResource(it.messageRes) }
        ?: state.receiptCurrencyError?.let { stringResource(it.messageRes) }
    Row(Modifier.padding(top = Spacing.xxs), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        CoShellAmountSlot(
            text = state.receiptAmount,
            onClick = { actions.onKeypadTarget(KeypadTarget.RECEIPT) },
            label = stringResource(R.string.entry_receipt_short),
            trailing = {
                CoShellTagMoreChip(
                    state.receiptCurrency.ifEmpty { stringResource(R.string.transaction_receipt_currency) } + " ▾",
                    onClick = { currencyOpen = true },
                )
            },
            active = state.keypadTarget == KeypadTarget.RECEIPT,
            isError = error != null,
            compact = true,
            // Up to ¾ of the line: the rate keeps a quarter, the number shrinks on a narrow screen instead.
            modifier = Modifier.weight(3f, fill = false),
        )
        Text(
            error ?: receiptRateText(state).orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            // «1 USDT = / 1 015,27 ARS» in the quarter left by the amount: smaller letters rather than a cut code.
            autoSize = fitAmountAutoSize(min = MinRateSize, max = MaterialTheme.typography.bodySmall.fontSize),
            modifier = Modifier.weight(1f),
        )
        CoShellIconButton(onClick = { actions.onReceiptToggle(false) }) {
            Icon(painterResource(DesignR.drawable.ic_close), stringResource(R.string.entry_receipt_remove))
        }
    }
    if (currencyOpen) ReceiptCurrencySheet(state, actions, onClosed = { currencyOpen = false })
}

private val MinRateSize = 9.sp
