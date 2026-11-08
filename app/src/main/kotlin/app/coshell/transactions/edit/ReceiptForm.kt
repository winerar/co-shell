package app.coshell.transactions.edit

import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyParser
import app.coshell.core.model.Rate
import app.coshell.core.model.Transaction
import app.coshell.transactions.edit.TransactionEditUiState.ReceiptCurrencyError

/** Receipt amount check; `null` money with no errors means the block is off. */
internal data class ReceiptResult(
    val money: Money? = null,
    val currencyError: ReceiptCurrencyError? = null,
    val amountError: TransactionEditUiState.AmountError? = null,
) {
    val isValid: Boolean get() = currencyError == null && amountError == null
}

internal fun validateReceipt(form: TransactionEditUiState, account: Account?): ReceiptResult {
    if (!form.hasReceipt) return ReceiptResult()
    val currency = CurrencyCode.parseOrNull(form.receiptCurrency)
        ?: return ReceiptResult(currencyError = ReceiptCurrencyError.INVALID)
    if (currency == account?.currency) return ReceiptResult(currencyError = ReceiptCurrencyError.SAME_AS_ACCOUNT)
    val amount = parsePositiveAmount(form.receiptAmount, currency)
    return ReceiptResult(money = amount.money, amountError = amount.error)
}

/**
 * The real rate of the purchase from the two typed amounts, e.g. "1 USDT = 1 049,69 ARS".
 * Shown in the direction where the number is ≥ 1; `null` until both amounts are valid.
 */
internal fun receiptRateText(form: TransactionEditUiState): String? {
    if (!form.hasReceipt) return null
    val account = form.account ?: return null
    val accountCurrency = CurrencyCode(account.currency)
    val receiptCurrency = CurrencyCode.parseOrNull(form.receiptCurrency) ?: return null
    if (receiptCurrency == accountCurrency) return null
    val paid = parsePositiveAmount(form.amount, accountCurrency).money ?: return null
    val receipt = parsePositiveAmount(form.receiptAmount, receiptCurrency).money ?: return null
    val rate = Rate.between(paid, receipt)
    return if (rate >= Rate.ONE) {
        "1 ${accountCurrency.value} = ${formatRate(rate)} ${receiptCurrency.value}"
    } else {
        "1 ${receiptCurrency.value} = ${formatRate(rate.inverse())} ${accountCurrency.value}"
    }
}

/** Receipt chips: currencies of earlier receipts (newest first), then of the accounts. */
internal fun receiptCurrencySuggestions(recent: List<Transaction>, accounts: List<Account>): List<String> =
    (recent.mapNotNull { it.originalAmount?.currency?.value } + accounts.map { it.currency.value }).distinct()

/** Ru-style: "1 049,69", "0,0067"; 2 fraction digits from 100, otherwise 4 significant-ish. */
internal fun formatRate(rate: Rate): String {
    val plain = rate.rounded(if (rate >= Rate.parse("100")) 2 else 4).toPlainString()
    val integer = plain.substringBefore('.')
    val fraction = plain.substringAfter('.', "")
    val grouped = integer.reversed().chunked(3).joinToString(" ").reversed()
    return if (fraction.isEmpty()) grouped else "$grouped,$fraction"
}

internal data class AmountParse(val money: Money?, val error: TransactionEditUiState.AmountError?)

/** Amounts are always positive: the direction comes from the transaction type. */
internal fun parsePositiveAmount(text: String, currency: CurrencyCode): AmountParse =
    when (val parsed = MoneyParser.parse(text, currency)) {
        is MoneyParser.Result.Success -> if (parsed.money.isPositive) {
            AmountParse(parsed.money, null)
        } else {
            AmountParse(null, TransactionEditUiState.AmountError.NOT_POSITIVE)
        }
        is MoneyParser.Result.Error -> AmountParse(
            null,
            when (parsed.reason) {
                MoneyParser.Reason.EMPTY -> TransactionEditUiState.AmountError.REQUIRED
                MoneyParser.Reason.TOO_MANY_FRACTION_DIGITS -> TransactionEditUiState.AmountError.TOO_MANY_FRACTION_DIGITS
                MoneyParser.Reason.INVALID_CHARACTERS, MoneyParser.Reason.TOO_LARGE -> TransactionEditUiState.AmountError.INVALID
            },
        )
    }
