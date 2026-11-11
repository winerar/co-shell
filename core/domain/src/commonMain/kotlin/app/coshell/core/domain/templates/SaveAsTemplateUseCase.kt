package app.coshell.core.domain.templates

import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionTemplate

/**
 * «Сохранить как шаблон» in the operation editor: remembers the type, account, category and note of
 * [Transaction]; the amount — only if asked ([rememberAmount]). The date, the receipt in another
 * currency and the arriving amount of a transfer are never part of a template.
 */
class SaveAsTemplateUseCase(
    private val templates: TemplateRepository,
    private val save: SaveTemplateUseCase,
    private val ids: IdGenerator,
) {
    suspend operator fun invoke(transaction: Transaction, name: String, rememberAmount: Boolean): SaveTemplateUseCase.Result =
        save(templateFrom(transaction, ids.newId(), name, rememberAmount, sortOrder = (templates.maxSortOrder() ?: -1) + 1))
}

/** The template that repeats [transaction]; a new template goes to the end of the list ([sortOrder]). */
fun templateFrom(
    transaction: Transaction,
    id: String,
    name: String,
    rememberAmount: Boolean,
    sortOrder: Int,
) = TransactionTemplate(
    id = id,
    name = name.trim(),
    type = transaction.type,
    accountId = transaction.accountId,
    currency = transaction.amount.currency,
    amount = transaction.amount.takeIf { rememberAmount },
    categoryId = transaction.categoryId,
    toAccountId = transaction.toAccountId,
    note = transaction.note,
    sortOrder = sortOrder,
)
