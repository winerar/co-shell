package app.coshell.core.model

/**
 * Categories the app itself relies on. They have fixed ids, so both phones create the same rows
 * and sync merges them instead of making duplicates.
 *
 * «Корректировка баланса» (one per direction) aligns a balance with reality: unpaired Wallet
 * transfers, a difference found when reconciling. Operations in it change the balance but are
 * **not** spending or income — totals and reports leave them out ([isBalanceAdjustment]).
 */
object SystemCategories {
    const val ADJUSTMENT_EXPENSE_ID = "system-adjustment-expense"
    const val ADJUSTMENT_INCOME_ID = "system-adjustment-income"
    const val ADJUSTMENT_NAME = "Корректировка баланса"

    fun adjustmentId(type: CategoryType): String = when (type) {
        CategoryType.EXPENSE -> ADJUSTMENT_EXPENSE_ID
        CategoryType.INCOME -> ADJUSTMENT_INCOME_ID
    }

    /** Direction of an adjustment category, `null` for any other id. Fixed: the id encodes it. */
    fun adjustmentType(categoryId: String?): CategoryType? = when (categoryId) {
        ADJUSTMENT_EXPENSE_ID -> CategoryType.EXPENSE
        ADJUSTMENT_INCOME_ID -> CategoryType.INCOME
        else -> null
    }

    fun isAdjustment(categoryId: String?): Boolean = adjustmentType(categoryId) != null

    /** A fresh adjustment category of [type]; sorted last among its siblings. */
    fun adjustment(type: CategoryType): Category = Category(
        id = adjustmentId(type),
        name = ADJUSTMENT_NAME,
        type = type,
        icon = "bank",
        color = 0xFF7A8494,
        sortOrder = Int.MAX_VALUE / 2,
    )
}

/** An operation in «Корректировка баланса»: moves the balance, but is not spending or income. */
val Transaction.isBalanceAdjustment: Boolean get() = SystemCategories.isAdjustment(categoryId)
