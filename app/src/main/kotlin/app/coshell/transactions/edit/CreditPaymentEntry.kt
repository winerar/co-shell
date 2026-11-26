package app.coshell.transactions.edit

import app.coshell.core.model.Account
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.TransactionType

/**
 * A new transfer into a credit [card] (#686, «Провести» in «Скоро»): the minimum payment is the amount. The source is the account the sheet
 * opened with if it is in the card's currency, else the first other account of that currency, else the opened one (never the card itself).
 * When the source is in another currency, the minimum goes to «Сумма зачисления» and the debited sum is typed.
 */
internal fun TransactionEditUiState.withCreditPayment(
    card: Account,
    minimum: Money,
    accounts: List<Account>,
    moneyFormat: MoneyFormat,
): TransactionEditUiState {
    val others = accounts.filter { it.id != card.id }
    val from = others.firstOrNull { it.id == accountId && it.currency == card.currency }
        ?: others.firstOrNull { it.currency == card.currency }
        ?: others.firstOrNull { it.id == accountId }
        ?: others.firstOrNull()
    val sameCurrency = from == null || from.currency == card.currency
    val typed = moneyFormat.formatForEditing(minimum)
    return copy(
        type = TransactionType.TRANSFER,
        refund = false,
        accountId = from?.id,
        toAccountId = card.id,
        amount = if (sameCurrency) typed else "",
        toAmount = if (sameCurrency) "" else typed,
        amountError = null,
        toAmountError = null,
    )
}

/** «Пополнить» a savings goal (#768): the same transfer as a card payment, into the goal's account; no amount when [amount] is zero. */
internal fun TransactionEditUiState.withGoalTopUp(target: Account, amount: Money, accounts: List<Account>, moneyFormat: MoneyFormat): TransactionEditUiState {
    val filled = withCreditPayment(target, amount, accounts, moneyFormat)
    return if (amount.amountMinor > 0) filled else filled.copy(amount = "", toAmount = "")
}
