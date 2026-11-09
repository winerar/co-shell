package app.coshell.transactions.edit

import app.coshell.core.domain.rates.CurrencyConverter
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.MoneyFormat

/**
 * "Receipt in another currency": fills "debited from the account" from the receipt amount by the
 * known rate on the operation date. Only while the field is empty or still holds our own
 * suggestion — a typed amount (the real one from the bank) is never overwritten.
 */
internal fun TransactionEditUiState.withDebitSuggestion(
    converter: CurrencyConverter?,
    format: MoneyFormat,
): TransactionEditUiState {
    if (!amountSuggested && amount.isNotBlank()) return this
    val cleared = if (amountSuggested) copy(amount = "", amountSuggested = false, suggestedRateDate = null) else this
    if (!hasReceipt || converter == null) return cleared
    val accountCurrency = account?.currency?.let(CurrencyCode::parseOrNull) ?: return cleared
    val receiptCurrency = CurrencyCode.parseOrNull(receiptCurrency) ?: return cleared
    if (receiptCurrency == accountCurrency) return cleared
    val receipt = parsePositiveAmount(receiptAmount, receiptCurrency).money ?: return cleared
    val conversion = converter.convert(receipt, accountCurrency, date) ?: return cleared
    if (!conversion.money.isPositive) return cleared
    return copy(
        amount = format.formatForEditing(conversion.money),
        amountSuggested = true,
        suggestedRateDate = conversion.rate.date,
        amountError = null,
    )
}
