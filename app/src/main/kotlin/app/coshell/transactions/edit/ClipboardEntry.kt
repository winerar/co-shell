package app.coshell.transactions.edit

import app.coshell.core.domain.quickentry.TextAmount
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.MoneyFormat
import app.coshell.transactions.edit.TransactionEditUiState.KeypadTarget

/**
 * The amount found in the clipboard text (#321, board `Clip321Row`): in the account's currency it is the amount, in another one it is
 * «Сумма покупки» (the debited sum follows from the rate, as for a receipt or a draft). The rest of the text fills an empty note.
 */
internal fun TransactionEditUiState.withClipboardAmount(
    found: TextAmount,
    accountCurrency: CurrencyCode?,
    moneyFormat: MoneyFormat,
): TransactionEditUiState {
    val same = accountCurrency == null || accountCurrency == found.money.currency
    val typed = moneyFormat.formatForEditing(found.money)
    return copy(
        amount = if (same) typed else "",
        amountSuggested = false,
        suggestedRateDate = null,
        amountError = null,
        receiptEnabled = !same,
        receiptCurrency = if (same) receiptCurrency else found.money.currency.value,
        receiptAmount = if (same) "" else typed,
        receiptAmountError = null,
        receiptCurrencyError = null,
        keypadTarget = if (same) KeypadTarget.AMOUNT else KeypadTarget.RECEIPT,
        note = note.ifBlank { found.note },
        clipboardNone = false,
    )
}

/** The row «Вставить из буфера» is offered while a new operation has no amount yet. */
internal val TransactionEditUiState.showsPasteRow: Boolean
    get() = isNew && !isCopy && !isTransfer && scan == null && payment == null && templateName == null && amount.isBlank() && receiptAmount.isBlank()
