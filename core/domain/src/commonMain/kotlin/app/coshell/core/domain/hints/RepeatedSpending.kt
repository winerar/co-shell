package app.coshell.core.domain.hints

import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType

/** Trigger of the `template` gesture hint (`hints.md`): the three latest expenses share a category and an account. */
object RepeatedSpending {
    const val RUN = 3

    /** [newestFirst] — transactions sorted by date, newest first; transfers and uncategorised entries break the run. */
    fun hasRun(newestFirst: List<Transaction>): Boolean {
        val latest = newestFirst.take(RUN)
        if (latest.size < RUN) return false
        val first = latest.first()
        val category = first.categoryId ?: return false
        return latest.all {
            it.type == TransactionType.EXPENSE && it.categoryId == category && it.accountId == first.accountId
        }
    }
}
