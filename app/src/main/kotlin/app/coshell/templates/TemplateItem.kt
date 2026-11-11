package app.coshell.templates

import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType

/**
 * A template ready to draw — the chip in the «+» sheet and the row of «Все шаблоны» show the same thing.
 * Names of an account or category that is gone are empty: the entry sheet will ask to choose them.
 */
data class TemplateItem(
    val id: String,
    val name: String,
    val type: TransactionType,
    val accountName: String,
    /** For a transfer — the target account. */
    val toAccountName: String = "",
    val categoryName: String = "",
    val categoryIcon: String? = null,
    val categoryColor: Long? = null,
    /** «40 000 ARS»; empty — the amount is asked every time. */
    val amount: String = "",
    val currency: String = "",
)

fun TransactionTemplate.toItem(
    accounts: Map<String, Account>,
    categories: Map<String, Category>,
    moneyFormat: MoneyFormat,
): TemplateItem {
    val category = categoryId?.let { categories[it] }
    return TemplateItem(
        id = id,
        name = name,
        type = type,
        accountName = accounts[accountId]?.name.orEmpty(),
        toAccountName = toAccountId?.let { accounts[it]?.name }.orEmpty(),
        categoryName = category?.name.orEmpty(),
        categoryIcon = category?.icon,
        categoryColor = category?.color,
        amount = amount?.let { moneyFormat.format(it) }.orEmpty(),
        currency = currency.value,
    )
}
