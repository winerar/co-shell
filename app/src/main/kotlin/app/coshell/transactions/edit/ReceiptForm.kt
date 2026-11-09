package app.coshell.transactions.edit

import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyParser
import app.coshell.core.model.Rate
import app.coshell.core.model.Transaction
import app.coshell.transactions.edit.TransactionEditUiState.ReceiptCurrencyError
import app.coshell.ui.format.formatRate

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

internal data class AmountParse(val money: Money?, val error: TransactionEditUiState.AmountError?)

/**
 * Amounts are always positive: the direction comes from the transaction type. The keypad's `+` makes a sum
 * ("32000 + 1500"): every term is parsed on its own and the first error wins.
 */
internal fun parsePositiveAmount(text: String, currency: CurrencyCode): AmountParse {
    val terms = text.split('+').map { it.trim() }.filter { it.isNotEmpty() }
    if (terms.size <= 1) return parseOneAmount(terms.firstOrNull().orEmpty(), currency)
    var total = 0L
    for (term in terms) {
        val part = parseOneAmount(term, currency)
        val money = part.money ?: return part
        total = runCatching { Math.addExact(total, money.amountMinor) }
            .getOrElse { return AmountParse(null, TransactionEditUiState.AmountError.INVALID) }
    }
    return AmountParse(Money(total, currency), null)
}

private fun parseOneAmount(text: String, currency: CurrencyCode): AmountParse =
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
