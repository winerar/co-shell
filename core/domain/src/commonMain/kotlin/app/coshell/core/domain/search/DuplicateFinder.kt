package app.coshell.core.domain.search

import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * «Похоже на дубль» (#791, poll 2026-11-27 «А», board `Smart791Mark`): two expenses or incomes of the same amount, currency
 * and category (or the same account when there is none) within [DUPLICATE_WINDOW] of each other — most often one purchase
 * written down by two people. Nothing is deleted by itself: the list only asks.
 */
data class DuplicatePair(val first: String, val second: String) {
    /** The same pair whichever row is asked about: «Это разные» remembers it. */
    val key: String get() = listOf(first, second).sorted().joinToString(KEY_SEPARATOR)
}

/** How close in time two records must be to look like one purchase. */
val DUPLICATE_WINDOW: Duration = 10.minutes

/** Only fresh records are compared: an old pair has been checked already, or nobody remembers it. */
val DUPLICATE_RECENT: Duration = 30.days

/**
 * The duplicate pairs among [transactions]: each row in at most one pair (the earliest match), [first] — the older record,
 * [second] — the newer one («Удалить вторую»). Pairs in [dismissed] ([DuplicatePair.key]), imports (they have their own
 * guard) and records older than [DUPLICATE_RECENT] before [now] are left out. Returns row id → its pair.
 */
fun duplicatePairs(transactions: List<Transaction>, dismissed: Set<String>, now: Instant): Map<String, DuplicatePair> {
    val candidates = transactions
        .filter { it.type != TransactionType.TRANSFER && it.importSource == null && now - it.date <= DUPLICATE_RECENT }
        .sortedWith(compareBy<Transaction> { it.date }.thenBy { it.id })
    val result = HashMap<String, DuplicatePair>()
    candidates.forEachIndexed { i, a ->
        if (a.id in result) return@forEachIndexed
        for (j in i + 1 until candidates.size) {
            val b = candidates[j]
            if (b.date - a.date > DUPLICATE_WINDOW) break
            if (b.id in result || !a.looksLike(b)) continue
            val pair = DuplicatePair(a.id, b.id)
            if (pair.key in dismissed) continue
            result[a.id] = pair
            result[b.id] = pair
            break
        }
    }
    return result
}

private fun Transaction.looksLike(other: Transaction): Boolean =
    type == other.type && amount == other.amount &&
        (if (categoryId != null || other.categoryId != null) categoryId == other.categoryId else accountId == other.accountId)

/** Pairs the person called different («Это разные»): kept on the phone, not synced — like hints. */
interface DuplicateDismissals {
    val dismissed: Flow<Set<String>>

    suspend fun dismiss(key: String)
}

/** For previews and tests. */
class InMemoryDuplicateDismissals(initial: Set<String> = emptySet()) : DuplicateDismissals {
    private val state = MutableStateFlow(initial)
    override val dismissed: Flow<Set<String>> = state

    override suspend fun dismiss(key: String) = state.update { it + key }
}

private const val KEY_SEPARATOR = "|"
