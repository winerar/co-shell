package app.coshell.transactions.edit

import app.coshell.core.model.Account
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyParser
import app.coshell.core.model.TransactionType
import app.coshell.transactions.edit.TransactionEditUiState.AmountError
import app.coshell.transactions.edit.TransactionEditUiState.ToAccountError

/** What the form describes once it is valid; ids and dates are added by the ViewModel. */
internal data class ValidForm(
    val type: TransactionType,
    val account: Account,
    val amount: Money,
    val toAccount: Account?,
    val toAmount: Money?,
    val categoryId: String?,
    val note: String,
)

/** Field errors shown when the form is invalid. */
internal data class FormErrors(
    val accountError: Boolean = false,
    val amountError: AmountError? = null,
    val toAccountError: ToAccountError? = null,
    val toAmountError: AmountError? = null,
    val categoryError: Boolean = false,
)

internal sealed interface FormResult {
    data class Valid(val form: ValidForm) : FormResult
    data class Invalid(val errors: FormErrors) : FormResult
}

/**
 * Checks the typed form against the known accounts. Pure: no repositories, no clock.
 * A transfer between accounts in one currency reuses [TransactionEditUiState.amount] as the arriving amount.
 */
internal fun validateForm(form: TransactionEditUiState, accountsById: Map<String, Account>): FormResult {
    val account = form.accountId?.let { accountsById[it] }
    val toAccount = form.toAccountId?.let { accountsById[it] }
    val amount = account?.let { parseAmount(form.amount, it) }

    var toAccountError: ToAccountError? = null
    var toAmount: AmountResult? = null
    if (form.isTransfer) {
        toAccountError = when {
            toAccount == null -> ToAccountError.REQUIRED
            toAccount.id == account?.id -> ToAccountError.SAME_ACCOUNT
            else -> null
        }
        if (account != null && toAccount != null && toAccountError == null) {
            toAmount = if (account.currency == toAccount.currency) amount else parseAmount(form.toAmount, toAccount)
        }
    }
    val categoryId = form.categoryId.takeIf { !form.isTransfer }
    val categoryError = !form.isTransfer && categoryId == null

    val money = amount?.money
    val toMoney = toAmount?.money
    val transferOk = !form.isTransfer || (toAccountError == null && toMoney != null)
    if (account == null || money == null || categoryError || !transferOk) {
        return FormResult.Invalid(
            FormErrors(
                accountError = account == null,
                amountError = amount?.error,
                toAccountError = toAccountError,
                toAmountError = toAmount?.error?.takeIf { form.needsToAmount },
                categoryError = categoryError,
            ),
        )
    }
    return FormResult.Valid(
        ValidForm(
            type = form.type,
            account = account,
            amount = money,
            toAccount = toAccount.takeIf { form.isTransfer },
            toAmount = toMoney.takeIf { form.isTransfer },
            categoryId = categoryId,
            note = form.note.trim(),
        ),
    )
}

private data class AmountResult(val money: Money?, val error: AmountError?)

/** Amounts are always positive: the direction comes from the transaction type. */
private fun parseAmount(text: String, account: Account): AmountResult =
    when (val parsed = MoneyParser.parse(text, account.currency)) {
        is MoneyParser.Result.Success ->
            if (parsed.money.isPositive) AmountResult(parsed.money, null) else AmountResult(null, AmountError.NOT_POSITIVE)
        is MoneyParser.Result.Error -> AmountResult(
            null,
            when (parsed.reason) {
                MoneyParser.Reason.EMPTY -> AmountError.REQUIRED
                MoneyParser.Reason.TOO_MANY_FRACTION_DIGITS -> AmountError.TOO_MANY_FRACTION_DIGITS
                MoneyParser.Reason.INVALID_CHARACTERS, MoneyParser.Reason.TOO_LARGE -> AmountError.INVALID
            },
        )
    }
