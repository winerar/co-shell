package app.coshell.core.domain.importer.wallet

import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType

/**
 * A Wallet category with a direction: in Wallet one category may hold expenses and incomes
 * («Подарки» — both), in co-shell a category has one type, so each direction maps separately.
 */
data class WalletCategoryKey(val name: String, val type: CategoryType)

/** What to do with a Wallet category (stage 5b). */
sealed interface CategoryChoice {
    data class CreateNew(val name: String, val type: CategoryType) : CategoryChoice
    data class UseExisting(val categoryId: String) : CategoryChoice
}

object WalletCategoryMatcher {

    /** Category keys of non-transfer records, most used first. */
    fun keys(records: List<WalletRecord>): List<WalletCategoryKey> =
        records.filter { !it.isTransfer && it.category != WalletSummary.TRANSFER_CATEGORY }
            .groupingBy { it.categoryKey() }
            .eachCount()
            .entries.sortedByDescending { it.value }
            .map { it.key }

    /** Suggestion: a live category of the same type and name (ignoring case), else create. */
    fun suggest(keys: List<WalletCategoryKey>, existing: List<Category>): Map<WalletCategoryKey, CategoryChoice> =
        keys.associateWith { key ->
            val match = existing.firstOrNull {
                !it.archived && it.type == key.type && it.name.trim().equals(key.name.trim(), ignoreCase = true)
            }
            match?.let { CategoryChoice.UseExisting(it.id) } ?: CategoryChoice.CreateNew(key.name, key.type)
        }
}

internal fun WalletRecord.categoryKey() =
    WalletCategoryKey(category, if (amount.isNegative) CategoryType.EXPENSE else CategoryType.INCOME)
