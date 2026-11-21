package app.coshell.core.domain.drafts

import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.Draft

/** What a draft still lacks, in the order the person fixes it: the amount, the account, the category. `null` from [DraftCheck.missing] — ready. */
enum class DraftMissing { AMOUNT, ACCOUNT, CATEGORY }

/** «Готов» = has an amount, a live account in the amount's currency and a live expense category (#586). */
object DraftCheck {
    fun missing(draft: Draft, accounts: Map<String, Account>, categories: Map<String, Category>): DraftMissing? {
        if ((draft.amountMinor ?: 0L) <= 0L) return DraftMissing.AMOUNT
        val account = draft.accountId?.let { accounts[it] }
        if (account == null || account.archived || account.currency != draft.currency) return DraftMissing.ACCOUNT
        val category = draft.categoryId?.let { categories[it] }
        if (category == null || category.archived || category.type != CategoryType.EXPENSE) return DraftMissing.CATEGORY
        return null
    }
}
