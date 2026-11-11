package app.coshell.templates.edit

import app.coshell.core.model.Account
import app.coshell.core.model.Money
import app.coshell.core.model.TransactionTemplate
import app.coshell.transactions.edit.parsePositiveAmount

internal sealed interface TemplateFormResult {
    data class Valid(val template: TransactionTemplate) : TemplateFormResult
    data class Invalid(
        val nameError: Boolean = false,
        val accountError: Boolean = false,
        val toAccountError: Boolean = false,
        val amountError: Boolean = false,
    ) : TemplateFormResult
}

/**
 * Checks the typed form and builds the template. Pure: no repositories. A blank amount is fine («спросим
 * при вводе»); a transfer needs a second, different account and never keeps a category.
 */
internal fun buildTemplate(
    form: TemplateEditUiState,
    accountsById: Map<String, Account>,
    id: String,
    sortOrder: Int,
): TemplateFormResult {
    val name = form.name.trim()
    val account = form.accountId?.let { accountsById[it] }
    val toAccount = form.toAccountId?.let { accountsById[it] }.takeIf { form.isTransfer }
    val toAccountError = form.isTransfer && (toAccount == null || toAccount.id == account?.id)

    var amount: Money? = null
    var amountError = false
    if (form.amount.isNotBlank() && account != null) {
        amount = parsePositiveAmount(form.amount, account.currency).money
        amountError = amount == null
    }
    if (name.isEmpty() || account == null || toAccountError || amountError) {
        return TemplateFormResult.Invalid(name.isEmpty(), account == null, toAccountError, amountError)
    }
    return TemplateFormResult.Valid(
        TransactionTemplate(
            id = id,
            name = name,
            type = form.type,
            accountId = account.id,
            currency = account.currency,
            amount = amount,
            categoryId = form.categoryId.takeIf { !form.isTransfer },
            toAccountId = toAccount?.id,
            note = form.note.trim(),
            sortOrder = sortOrder,
        ),
    )
}
