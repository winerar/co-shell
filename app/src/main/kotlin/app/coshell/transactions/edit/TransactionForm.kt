package app.coshell.transactions.edit

import app.coshell.core.model.Account
import app.coshell.core.model.Money
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
    /** What the receipt said, when paid in another currency than the account's. */
    val originalAmount: Money? = null,
)

/** Field errors shown when the form is invalid. */
internal data class FormErrors(
    val accountError: Boolean = false,
    val amountError: AmountError? = null,
    val toAccountError: ToAccountError? = null,
    val toAmountError: AmountError? = null,
    val categoryError: Boolean = false,
    val receiptCurrencyError: TransactionEditUiState.ReceiptCurrencyError? = null,
    val receiptAmountError: AmountError? = null,
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
    var toAmount: AmountParse? = null
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
    val receipt = validateReceipt(form, account)
    if (account == null || money == null || categoryError || !transferOk || !receipt.isValid) {
        return FormResult.Invalid(
            FormErrors(
                accountError = account == null,
                amountError = amount?.error,
                toAccountError = toAccountError,
                toAmountError = toAmount?.error?.takeIf { form.needsToAmount },
                categoryError = categoryError,
                receiptCurrencyError = receipt.currencyError,
                receiptAmountError = receipt.amountError,
            ),
        )
    }
    return FormResult.Valid(
        ValidForm(
            type = if (form.refund && form.type == TransactionType.EXPENSE) TransactionType.INCOME else form.type,
            account = account,
            amount = money,
            toAccount = toAccount.takeIf { form.isTransfer },
            toAmount = toMoney.takeIf { form.isTransfer },
            categoryId = categoryId,
            note = form.note.trim(),
            originalAmount = receipt.money,
        ),
    )
}

private fun parseAmount(text: String, account: Account): AmountParse = parsePositiveAmount(text, account.currency)
