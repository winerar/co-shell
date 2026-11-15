package app.coshell.core.domain.reports

import app.coshell.core.model.Category
import app.coshell.core.model.Money
import kotlinx.datetime.LocalDate

/**
 * One row of «Кто сколько потратил» (#189, board `Rp189WhoA`): net spending of the operations a family member added.
 * [memberId] `null` — «я»: everything not added by the others (unknown authors too, as the «Кто внёс» filter counts).
 */
data class MemberSpending(
    val memberId: String?,
    val amount: Money,
    val percent: Int,
    /** Up to [TOP_FOLDERS] folders this member spent most on, largest first. */
    val top: List<Pair<String, Money>>,
)

/**
 * Spending of [from] ..< [toExclusive] by who added it: a row per member of [others] and one for me, largest first;
 * members who spent nothing are left out. Empty — no other members (a one-person family has no card) or no spending.
 */
fun spendingByMember(
    ledger: ReportLedger,
    categories: List<Category>,
    others: Set<String>,
    from: LocalDate,
    toExclusive: LocalDate,
): List<MemberSpending> {
    if (others.isEmpty()) return emptyList()
    val currency = ledger.currency
    val folders = linkedMapOf<String?, MutableMap<String, Money>>()
    for ((author, sums) in ledger.spentByAuthor(from, toExclusive)) {
        val who = author?.takeIf { it in others }
        val into = folders.getOrPut(who) { linkedMapOf() }
        for ((folder, money) in spendingByFolder(sums, categories, currency)) {
            into[folder] = (into[folder] ?: Money.zero(currency)) + money
        }
    }
    val totals = folders.mapValues { (_, f) -> f.values.fold(Money.zero(currency)) { a, b -> a + b } }
        .filterValues { it.isPositive }
        .entries.sortedWith(compareByDescending<Map.Entry<String?, Money>> { it.value.amountMinor }.thenBy { it.key ?: "" })
    val percents = percentShares(totals.map { it.value.amountMinor })
    return totals.mapIndexed { i, (who, amount) ->
        val top = folders.getValue(who).entries.filter { it.value.isPositive }
            .sortedWith(compareByDescending<Map.Entry<String, Money>> { it.value.amountMinor }.thenBy { it.key })
            .take(TOP_FOLDERS).map { it.key to it.value }
        MemberSpending(who, amount, percents[i], top)
    }
}

const val TOP_FOLDERS = 3
