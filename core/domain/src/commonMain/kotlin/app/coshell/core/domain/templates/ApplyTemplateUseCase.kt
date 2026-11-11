package app.coshell.core.domain.templates

import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.Money
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import kotlin.time.Clock
import kotlin.time.Instant

/** What a template can no longer fill in: the user has to choose it in the entry sheet. */
enum class TemplateGap { ACCOUNT, TARGET_ACCOUNT, CATEGORY }

/**
 * An operation not saved yet: the entry sheet opened from a template. Fields the template could not
 * fill are `null` and listed in [gaps] («нужно выбрать»); everything can still be edited.
 */
data class TransactionDraft(
    val type: TransactionType,
    val date: Instant,
    val accountId: String? = null,
    /** In the currency of [accountId]; `null` — the template asks every time, or the account is gone. */
    val amount: Money? = null,
    val categoryId: String? = null,
    val toAccountId: String? = null,
    val note: String = "",
    val gaps: Set<TemplateGap> = emptySet(),
    /** The template the draft came from — for the «Из шаблона» pill. */
    val templateId: String? = null,
)

/**
 * Template → draft dated "now". An account or category that was archived or deleted since (or no
 * longer fits: other currency, other type) is left empty and reported in [TransactionDraft.gaps]
 * instead of failing. `null` — the template itself is gone.
 */
class ApplyTemplateUseCase(
    private val accounts: AccountRepository,
    private val categories: CategoryRepository,
    private val templates: TemplateRepository,
    private val clock: Clock = Clock.System,
) {
    suspend operator fun invoke(templateId: String): TransactionDraft? {
        val template = templates.getTemplate(templateId) ?: return null
        return draftFrom(
            template,
            now = clock.now(),
            account = accounts.getAccount(template.accountId),
            target = template.toAccountId?.let { accounts.getAccount(it) },
            category = template.categoryId?.let { categories.getCategory(it) },
        )
    }
}

/** Pure part of [ApplyTemplateUseCase]: [account], [target], [category] are what the template points at now. */
fun draftFrom(
    template: TransactionTemplate,
    now: Instant,
    account: Account?,
    target: Account?,
    category: Category?,
): TransactionDraft {
    val gaps = mutableSetOf<TemplateGap>()
    val usableAccount = account?.takeIf { !it.archived && it.currency == template.currency }
    if (usableAccount == null) gaps += TemplateGap.ACCOUNT

    val isTransfer = template.type == TransactionType.TRANSFER
    val usableTarget = target?.takeIf { isTransfer && !it.archived }
    if (isTransfer && usableTarget == null) gaps += TemplateGap.TARGET_ACCOUNT

    val usableCategory = category?.takeIf { !it.archived && template.type.accepts(it.type) }
    if (template.categoryId != null && usableCategory == null) gaps += TemplateGap.CATEGORY

    return TransactionDraft(
        type = template.type,
        date = now,
        accountId = usableAccount?.id,
        // The amount is in the account's currency: without the account it means nothing.
        amount = template.amount.takeIf { usableAccount != null },
        categoryId = usableCategory?.id,
        toAccountId = usableTarget?.id,
        note = template.note,
        gaps = gaps,
        templateId = template.id,
    )
}
