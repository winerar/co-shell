package app.coshell.core.domain.family

import app.coshell.core.model.Money
import app.coshell.core.model.SystemCategories
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/** One «Аня: −80 000 ARS · Техника» notification: [inThreshold] is the amount in the threshold's currency. */
data class PartnerSpendAlert(val transaction: Transaction, val inThreshold: Money)

/**
 * Which operations a sync brought deserve a notification (#326): another member's expense of at least [threshold]
 * (the main report currency, converted at the operation's day). No server: the app runs this after each pull.
 *
 * Not reported: own operations, transfers and incomes (a refund is an income), imports, balance adjustments,
 * operations dated more than [MAX_AGE] before [now] (an old one edited by the partner is not news), and ids in
 * [alreadyNotified] (the same operation comes again after every edit). No rate to compare — not reported:
 * better silent than a wrong amount.
 */
object PartnerSpendAlerts {
    val MAX_AGE: Duration = 24.hours

    fun pick(
        pulled: List<Transaction>,
        me: String,
        threshold: Money,
        now: Instant,
        alreadyNotified: Set<String>,
        convert: (Transaction) -> Money?,
    ): List<PartnerSpendAlert> {
        require(threshold.isPositive) { "Threshold must be positive" }
        return pulled.asSequence()
            .filter { it.type == TransactionType.EXPENSE }
            .filter { it.authorMemberId != null && it.authorMemberId != me }
            .filter { it.importSource == null && !SystemCategories.isAdjustment(it.categoryId) }
            .filter { it.id !in alreadyNotified }
            .filter { now - it.date <= MAX_AGE }
            .mapNotNull { t ->
                val inThreshold = if (t.amount.currency == threshold.currency) t.amount else convert(t)
                inThreshold?.takeIf { it.currency == threshold.currency && it >= threshold }?.let { PartnerSpendAlert(t, it) }
            }
            .sortedBy { it.transaction.date }
            .toList()
    }
}
