package app.coshell.core.domain.goals

import app.coshell.core.domain.budgets.BudgetPeriod
import app.coshell.core.domain.reports.BalanceDay
import app.coshell.core.domain.reports.ReportLedger
import app.coshell.core.domain.reports.spendingByFolder
import app.coshell.core.model.Category
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.SystemCategories
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * «Что если» of a goal (#792, poll 2026-11-27 «А», boards `Smart792Line`, `Smart792Sheet`): spending [sharePercent] less on
 * [categoryId] puts [saved] more aside each month. [monthsNow] — months to the target at today's pace (`null` — the goal does
 * not get closer), [monthsWith] — with the saving; [dateNow]/[dateWith] — the months they end in. A guess: nothing is changed.
 */
data class GoalWhatIf(
    val categoryId: String,
    val sharePercent: Int,
    val saved: Money,
    val monthsNow: Int?,
    val monthsWith: Int,
    val dateNow: LocalDate?,
    val dateWith: LocalDate,
) {
    /** How much sooner; for a goal that does not get closer now, the whole [monthsWith] is the gain. */
    val monthsEarlier: Int? get() = monthsNow?.let { it - monthsWith }
}

/** A category to save on and its usual month in the goal's currency. */
data class WhatIfCategory(val categoryId: String, val monthly: Money)

/** «10 % · 20 % · 30 %» of the sheet; the line in the card uses the middle one. */
val WHAT_IF_SHARES = listOf(10, 20, 30)
const val WHAT_IF_DEFAULT_SHARE = 20

/** Categories the sheet offers at most. */
const val WHAT_IF_CATEGORIES = 4

/** The pace and the habits are counted over this many months. */
const val WHAT_IF_MONTHS = 3

/**
 * What the goal's account gained a month on average over the last [WHAT_IF_MONTHS] (90 days up to [today]): deposits,
 * transfers in and out, spending from it — everything that moved its balance. [currency] — the account's.
 */
fun savingPace(days: List<BalanceDay>, accountId: String, currency: CurrencyCode, today: LocalDate): Money {
    val from = today.minus(DatePeriod(days = PACE_DAYS))
    val gained = days.filter { it.accountId == accountId && it.day > from && it.day <= today }.sumOf { it.deltaMinor }
    return Money(gained, currency).scale(1, WHAT_IF_MONTHS.toLong())
}

/**
 * The folders worth saving on, biggest usual month first: spending in [months] (finished, oldest first) by folder in the
 * ledger's currency, averaged; folders whose spending is a regular payment ([scheduled] — category ids of templates with
 * «Повтор»: rent, internet) and «Корректировка баланса» are left out.
 */
fun whatIfCategories(ledger: ReportLedger, categories: List<Category>, months: List<BudgetPeriod>, scheduled: Set<String>): List<WhatIfCategory> {
    if (months.isEmpty()) return emptyList()
    val currency = ledger.currency
    val byId = categories.associateBy { it.id }
    val sums = months.map { spendingByFolder(ledger.spent(it), categories, currency) }
    return sums.flatMap { it.keys }.toSortedSet()
        .filter { id -> id !in scheduled && SystemCategories.adjustmentType(id) == null && byId[id]?.let { c -> categories.none { it.parentId == c.id && it.id in scheduled } } != false }
        .map { id -> WhatIfCategory(id, sums.fold(Money.zero(currency)) { acc, m -> acc + (m[id] ?: Money.zero(currency)) }.scale(1, months.size.toLong())) }
        .filter { it.monthly.isPositive }
        .sortedWith(compareByDescending<WhatIfCategory> { it.monthly.amountMinor }.thenBy { it.categoryId })
        .take(WHAT_IF_CATEGORIES)
}

/**
 * «Если тратить на кафе на 20 % меньше — на 2 месяца раньше»: [left] to save at [pace] a month, plus [sharePercent] of
 * [category]'s usual month. `null` — nothing left, a different currency, or the saving changes nothing by a whole month.
 */
fun goalWhatIf(left: Money, pace: Money, category: WhatIfCategory, sharePercent: Int, today: LocalDate): GoalWhatIf? {
    if (!left.isPositive || category.monthly.currency != left.currency || pace.currency != left.currency) return null
    val saved = category.monthly.scale(sharePercent.toLong(), 100)
    val faster = pace + saved
    if (!saved.isPositive || !faster.isPositive) return null
    val now = if (pace.isPositive) monthsFor(left, pace) else null
    val with = monthsFor(left, faster)
    if (now != null && now - with < 1) return null
    return GoalWhatIf(
        categoryId = category.categoryId,
        sharePercent = sharePercent,
        saved = saved,
        monthsNow = now,
        monthsWith = with,
        dateNow = now?.let { today.plus(DatePeriod(months = it)) },
        dateWith = today.plus(DatePeriod(months = with)),
    )
}

/** Whole months to collect [left] at [perMonth] (rounded up: a part of a month is one more deposit). */
private fun monthsFor(left: Money, perMonth: Money): Int {
    val whole = left.amountMinor / perMonth.amountMinor
    return (whole + if (left.amountMinor % perMonth.amountMinor == 0L) 0 else 1).coerceAtMost(MAX_MONTHS).toInt()
}

private const val PACE_DAYS = 90
private const val MAX_MONTHS = 1_200L
