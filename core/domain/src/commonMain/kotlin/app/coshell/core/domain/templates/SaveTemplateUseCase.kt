package app.coshell.core.domain.templates

import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.model.CategoryType
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType

/**
 * Validates a template against the accounts and category it references, then saves it. Structural
 * rules (name, positive amount, transfer target) are enforced by [TransactionTemplate] itself.
 * Archived accounts and categories are refused here: a new or edited template must be usable.
 */
class SaveTemplateUseCase(
    private val accounts: AccountRepository,
    private val categories: CategoryRepository,
    private val templates: TemplateRepository,
) {
    sealed interface Result {
        data object Saved : Result
        data class Invalid(val reason: Reason) : Result
    }

    enum class Reason {
        ACCOUNT_NOT_FOUND,
        CURRENCY_MISMATCH,
        TARGET_ACCOUNT_NOT_FOUND,
        CATEGORY_NOT_FOUND,
        CATEGORY_TYPE_MISMATCH,
    }

    suspend operator fun invoke(template: TransactionTemplate): Result {
        val account = accounts.getAccount(template.accountId)?.takeUnless { it.archived }
            ?: return Result.Invalid(Reason.ACCOUNT_NOT_FOUND)
        if (account.currency != template.currency) return Result.Invalid(Reason.CURRENCY_MISMATCH)
        if (template.type == TransactionType.TRANSFER) {
            template.toAccountId?.let { accounts.getAccount(it) }?.takeUnless { it.archived }
                ?: return Result.Invalid(Reason.TARGET_ACCOUNT_NOT_FOUND)
        }
        template.categoryId?.let { id ->
            val category = categories.getCategory(id)?.takeUnless { it.archived }
                ?: return Result.Invalid(Reason.CATEGORY_NOT_FOUND)
            if (!template.type.accepts(category.type)) return Result.Invalid(Reason.CATEGORY_TYPE_MISMATCH)
        }
        templates.saveTemplate(template)
        return Result.Saved
    }
}

/** Expenses take expense categories, incomes — income ones, transfers — any (e.g. fees). */
internal fun TransactionType.accepts(category: CategoryType): Boolean = when (this) {
    TransactionType.EXPENSE -> category == CategoryType.EXPENSE
    TransactionType.INCOME -> category == CategoryType.INCOME
    TransactionType.TRANSFER -> true
}
