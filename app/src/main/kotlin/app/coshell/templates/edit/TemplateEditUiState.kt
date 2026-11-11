package app.coshell.templates.edit

import app.coshell.core.model.CategoryType
import app.coshell.core.model.TransactionType

data class TemplateEditUiState(
    val isLoading: Boolean = false,
    val isNew: Boolean = true,
    val type: TransactionType = TransactionType.EXPENSE,
    val name: String = "",
    val nameError: Boolean = false,
    /** Accounts that can be chosen: not archived. */
    val accounts: List<AccountChoice> = emptyList(),
    val accountId: String? = null,
    val accountError: Boolean = false,
    /** Target of a transfer. */
    val toAccountId: String? = null,
    val toAccountError: Boolean = false,
    /** As typed, in the currency of [accountId]; blank — the amount is asked every time. */
    val amount: String = "",
    val amountError: Boolean = false,
    /** Every usable category; the screen offers [categoryChoices] — those of the current [type]. */
    val categories: List<CategoryChoice> = emptyList(),
    /** Optional: without it the entry sheet asks for the category. Never set for a transfer. */
    val categoryId: String? = null,
    val note: String = "",
    /** Saved or deleted: the screen should close. */
    val isDone: Boolean = false,
) {
    data class AccountChoice(val id: String, val name: String, val currency: String)

    /** [label] — «Папка / Категория» for a subcategory. */
    data class CategoryChoice(val id: String, val label: String, val type: CategoryType)

    val isTransfer: Boolean get() = type == TransactionType.TRANSFER
    val account: AccountChoice? get() = accounts.firstOrNull { it.id == accountId }
    val toAccount: AccountChoice? get() = accounts.firstOrNull { it.id == toAccountId }
    val category: CategoryChoice? get() = categories.firstOrNull { it.id == categoryId }

    val categoryChoices: List<CategoryChoice>
        get() = when (type) {
            TransactionType.EXPENSE -> categories.filter { it.type == CategoryType.EXPENSE }
            TransactionType.INCOME -> categories.filter { it.type == CategoryType.INCOME }
            TransactionType.TRANSFER -> emptyList()
        }
}
