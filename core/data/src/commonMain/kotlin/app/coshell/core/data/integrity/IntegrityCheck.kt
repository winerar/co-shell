package app.coshell.core.data.integrity

import app.coshell.core.data.db.IntegrityDao

/** One broken rule and a few of the rows that break it (at most [IntegrityDao.SAMPLE]). */
data class IntegrityProblem(val kind: Kind, val ids: List<String>) {
    enum class Kind(val label: String) {
        TRANSACTION_ON_MISSING_ACCOUNT("transaction on a missing account"),
        TRANSFER_TO_MISSING_ACCOUNT("transfer to a missing account"),
        TRANSACTION_ON_MISSING_CATEGORY("transaction in a missing category"),
        CURRENCY_NOT_OF_ACCOUNT("transaction currency differs from its account"),
        TRANSFER_CURRENCY_NOT_OF_ACCOUNT("transfer target currency differs from its account"),
        BUDGET_ON_MISSING_CATEGORY("budget for a missing category"),
        CATEGORY_IN_MISSING_FOLDER("category in a missing folder"),
    }

    /** One line for the technical data of a feedback message, e.g. `transfer to a missing account: 2 (t1, t2)`. */
    fun describe(): String {
        val more = if (ids.size >= IntegrityDao.SAMPLE) "+" else ""
        return "${kind.label}: ${ids.size}$more (${ids.take(SHOWN_IDS).joinToString()})"
    }

    private companion object {
        const val SHOWN_IDS = 3
    }
}

/**
 * Checks that the data hangs together after edits from two phones (#219): rows that point at deleted or
 * unknown accounts and categories, currencies that differ from the account. Reports only — repairing is a
 * separate decision. An empty list means everything is consistent.
 */
class IntegrityCheck(private val dao: IntegrityDao) {
    suspend fun run(): List<IntegrityProblem> = listOf(
        IntegrityProblem.Kind.TRANSACTION_ON_MISSING_ACCOUNT to dao.onMissingAccount(),
        IntegrityProblem.Kind.TRANSFER_TO_MISSING_ACCOUNT to dao.transferToMissingAccount(),
        IntegrityProblem.Kind.TRANSACTION_ON_MISSING_CATEGORY to dao.onMissingCategory(),
        IntegrityProblem.Kind.CURRENCY_NOT_OF_ACCOUNT to dao.currencyNotOfAccount(),
        IntegrityProblem.Kind.TRANSFER_CURRENCY_NOT_OF_ACCOUNT to dao.transferCurrencyNotOfAccount(),
        IntegrityProblem.Kind.BUDGET_ON_MISSING_CATEGORY to dao.budgetOnMissingCategory(),
        IntegrityProblem.Kind.CATEGORY_IN_MISSING_FOLDER to dao.categoryInMissingFolder(),
    ).filter { (_, ids) -> ids.isNotEmpty() }.map { (kind, ids) -> IntegrityProblem(kind, ids) }
}
