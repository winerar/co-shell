package app.coshell.core.domain.recurring

import app.coshell.core.model.Money
import app.coshell.core.model.Recurrence
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * «Похоже на ежемесячный платёж» (#789, poll 2026-11-27 «А», board `Smart789Coach`): the same expense or income — account,
 * category, payee or note — at least [RECURRING_MIN_TIMES] times about a month (or a week) apart, the last one recent.
 * [latest] is the newest record (what the template repeats), [amount] — the typical one, [next] — when it is expected.
 */
data class RecurringCandidate(
    val key: String,
    val latest: Transaction,
    val recurrence: Recurrence,
    val amount: Money,
    val last: LocalDate,
    val next: LocalDate,
)

/** Three in a row make a habit; two may be chance. */
const val RECURRING_MIN_TIMES = 3

/** Amounts may differ by this many percent (a bill grows a little) and still be one payment. */
const val RECURRING_AMOUNT_SPREAD_PERCENT = 10

private val MONTH_GAP = 25..35
private val WEEK_GAP = 6..8

/**
 * The regular payments hidden in [transactions] (the last few hundred are enough), newest first. Left out: transfers,
 * payments a template with «Повтор» already covers (same type, account and category), [dismissed] keys («Не сейчас»)
 * and habits that stopped (the last one more than one and a half periods before [today]).
 */
fun recurringCandidates(
    transactions: List<Transaction>,
    templates: List<TransactionTemplate>,
    dismissed: Set<String>,
    timeZone: TimeZone,
    today: LocalDate,
): List<RecurringCandidate> {
    val scheduled = templates.filter { it.schedule != null }.map { Triple(it.type, it.accountId, it.categoryId) }.toSet()
    return transactions
        .filter { it.type != TransactionType.TRANSFER && (it.label().isNotEmpty() || it.categoryId != null) }
        .groupBy { it.groupKey() }
        .mapNotNull { (key, list) ->
            val first = list.first()
            if (key in dismissed || Triple(first.type, first.accountId, first.categoryId) in scheduled) return@mapNotNull null
            candidate(key, list.sortedBy { it.date }, timeZone, today)
        }
        .sortedByDescending { it.last }
}

private fun candidate(key: String, sorted: List<Transaction>, timeZone: TimeZone, today: LocalDate): RecurringCandidate? {
    if (sorted.size < RECURRING_MIN_TIMES || sorted.map { it.amount.currency }.distinct().size > 1) return null
    val recent = sorted.takeLast(RECURRING_MIN_TIMES * 2)
    val days = recent.map { it.date.toLocalDateTime(timeZone).date }
    val gaps = days.zipWithNext { a, b -> a.daysUntil(b) }
    val tail = gaps.takeLast(RECURRING_MIN_TIMES - 1)
    val monthly = tail.all { it in MONTH_GAP }
    val weekly = tail.all { it in WEEK_GAP }
    if (!monthly && !weekly) return null
    val amounts = recent.takeLast(RECURRING_MIN_TIMES).map { it.amount.amountMinor }.sorted()
    val median = amounts[amounts.size / 2]
    if (amounts.any { kotlin.math.abs(it - median) * 100 > median * RECURRING_AMOUNT_SPREAD_PERCENT }) return null
    val last = days.last()
    val period = if (monthly) MONTH_DAYS else WEEK_DAYS
    if (last.daysUntil(today) > period * 3 / 2) return null
    val latest = recent.last()
    return RecurringCandidate(
        key = key,
        latest = latest,
        recurrence = if (monthly) Recurrence.Monthly(last.day) else Recurrence.Weekly(last.dayOfWeek),
        amount = Money(median, latest.amount.currency),
        last = last,
        next = last.plus(if (monthly) DatePeriod(months = 1) else DatePeriod(days = WEEK_DAYS)),
    )
}

/** What tells one payment from another besides the account and category: the payee, else the note, lower-cased. */
private fun Transaction.label(): String = (payee?.takeIf { it.isNotBlank() } ?: note).trim().lowercase()

private fun Transaction.groupKey(): String = listOf(KEY_PREFIX + type.name, accountId, categoryId.orEmpty(), label()).joinToString("|")

private const val MONTH_DAYS = 30
private const val WEEK_DAYS = 7
private const val KEY_PREFIX = "recurring:"
