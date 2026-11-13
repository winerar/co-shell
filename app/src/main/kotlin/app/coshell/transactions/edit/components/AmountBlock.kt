package app.coshell.transactions.edit.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.CoShellFormSection
import app.coshell.core.designsystem.components.CoShellSectionLink
import app.coshell.core.designsystem.components.keypad.AmountKeys
import app.coshell.core.designsystem.amount.RollingAmountText
import app.coshell.core.designsystem.motion.CoShellExpandable
import app.coshell.core.designsystem.motion.Tactile
import app.coshell.core.designsystem.motion.tactileClickable
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.transactions.edit.TransactionEditActions
import app.coshell.transactions.edit.TransactionEditUiState
import app.coshell.transactions.edit.TransactionEditUiState.KeypadTarget
import app.coshell.transactions.edit.receiptRateText
import app.coshell.ui.format.formatDayMonth

/**
 * «Сумма» of the entry sheet (#290, board `Fb290A`): the big amount; for a transfer between currencies the arriving
 * amount under it; with a receipt in another currency the caption says «· списано» and a dashed line «Чек 13 300 ARS
 * · курс · ✕» closes the card. «+ Чек» on the right of the caption switches the receipt on.
 */
@Composable
internal fun AmountBlock(state: TransactionEditUiState, actions: TransactionEditActions, amountColor: Color) {
    val receiptLink: (@Composable () -> Unit)? = if (!state.isTransfer && !state.hasReceipt) {
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
 * «Чек 13 300 ARS ▾ ··· 1 USDT = 1 015 ARS ✕»: the receipt amount (a tap — the keypad types here), its currency
 * (a tap — the currency sheet), the rate of the purchase and «✕» that drops the receipt.
 */
@Composable
private fun ReceiptLine(state: TransactionEditUiState, actions: TransactionEditActions) {
    var currencyOpen by rememberSaveable { mutableStateOf(false) }
    val active = state.keypadTarget == KeypadTarget.RECEIPT
    val line by animateColorAsState(
        when {
            state.receiptAmountError != null -> MaterialTheme.colorScheme.error
            active -> accentInk()
            else -> Color.Transparent
        },
        Motion.effects(),
        label = "receiptLine",
    )
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
        Box(Modifier.fillMaxWidth().height(Sizes.hairline).background(MaterialTheme.colorScheme.outlineVariant))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(stringResource(R.string.entry_receipt), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(
                Modifier.tactileClickable(Tactile.Control, MaterialTheme.shapes.small) { actions.onKeypadTarget(KeypadTarget.RECEIPT) }
                    .padding(vertical = Spacing.xxs),
            ) {
                RollingAmountText(
                    AmountKeys.display(AmountKeys.normalize(state.receiptAmount)).ifEmpty { "0" },
                    style = AmountTitleStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Box(Modifier.width(Sizes.currencyChipWidth).height(Sizes.selectionRing).background(line, MaterialTheme.shapes.small))
            }
            Text(
                state.receiptCurrency.ifEmpty { stringResource(R.string.transaction_receipt_currency) } + " ▾",
                style = MaterialTheme.typography.labelLarge,
                color = accentInk(),
                modifier = Modifier.tactileClickable(Tactile.Control, MaterialTheme.shapes.small) { currencyOpen = true }.padding(Spacing.xs),
            )
            Spacer(Modifier.weight(1f))
            receiptRateText(state)?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            Icon(
                painterResource(DesignR.drawable.ic_close),
                contentDescription = stringResource(R.string.entry_receipt_remove),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.tactileClickable(Tactile.Control, CircleShape) { actions.onReceiptToggle(false) }
                    .padding(Spacing.xs).size(Sizes.searchIcon),
            )
        }
        val error = state.receiptAmountError?.let { stringResource(it.messageRes) }
            ?: state.receiptCurrencyError?.let { stringResource(it.messageRes) }
        if (error != null) {
            Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
    if (currencyOpen) ReceiptCurrencySheet(state, actions, onClosed = { currencyOpen = false })
}
