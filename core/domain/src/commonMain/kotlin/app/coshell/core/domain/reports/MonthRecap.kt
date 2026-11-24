package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetPeriod
import app.coshell.core.model.Category
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * «Итоги месяца» as stories (#554, owner poll 2026-11-23, all «А»): the facts of a finished month, the cards worth
 * showing for them and one encouraging phrase ([RecapPhrases]). Counted by the rules of all reports (#187).
 * Pure logic: texts and drawing are the app's job, this only picks keys and numbers.
 */
data class MonthRecap(
    val period: BudgetPeriod,
    val currency: CurrencyCode,
    val spent: Money,
    /** `null` — nothing to compare with (first month of the history). */
    val previousSpent: Money?,
    /** Top categories by spending, at most [TOP_CATEGORIES], each with its share of [spent] in whole percents. */
    val top: List<CategoryShare>,
    /** Spending per week of the period (7-day slices from its first day), the last one may be shorter. */
    val weeks: List<Money>,
    /** Average spending per weekday / weekend day; `null` when the month has no such day with records. */
    val weekdayAverage: Money?,
    val weekendAverage: Money?,
    /** Categories with spending now and none in the previous month (empty without history). */
    val newCategoryIds: List<String>,
    /** Days with at least one record, and the longest run of such days in a row. */
    val recordedDays: Int,
    val streak: Int,
    /** 0-based day of the period where the first longest run begins. */
    val streakStart: Int = 0,
    val limits: LimitsTally?,
    val earned: Money,
    val missing: Set<CurrencyCode> = emptySet(),
) {
    /** Change against the previous month in whole percents, negative — lighter; `null` — no comparison. */
    val changePercent: Int? get() = previousSpent?.let { percentOf(spent - it, it) }

    /** Share of the income left, in whole percents (negative — spent more); `null` — nothing came in. */
    val savingsRate: Int? get() = percentOf(earned - spent, earned)

    /** The week with the largest spending (0-based), `null` when nothing was spent. */
    val busiestWeek: Int? get() = weeks.withIndex().maxByOrNull { it.value.amountMinor }?.takeIf { it.value.isPositive }?.index

    val phrase: RecapPhrase get() = RecapPhrases.pick(this)

    /** Cards in the order of the stories; a card without facts is left out, the final one is always there. */
    val cards: List<RecapCard>
        get() = buildList {
            if (top.isNotEmpty()) add(RecapCard.TOP_CATEGORIES)
            if (previousSpent != null && previousSpent.isPositive) add(RecapCard.VS_PREVIOUS)
            if (busiestWeek != null) add(RecapCard.WEEKS)
            if (newCategoryIds.isNotEmpty()) add(RecapCard.NEW_CATEGORY)
            if (weekdayAverage != null && weekendAverage != null) add(RecapCard.WEEKEND)
            if (streak >= MIN_STREAK_CARD) add(RecapCard.STREAK)
            add(RecapCard.FINAL)
        }
}

enum class RecapCard { TOP_CATEGORIES, VS_PREVIOUS, WEEKS, NEW_CATEGORY, WEEKEND, STREAK, FINAL }

data class CategoryShare(val categoryId: String, val amount: Money, val percent: Int)

/** The recap of [month] against [previous] (`null` — no history). The ledger must hold both periods. */
fun monthRecap(
    ledger: ReportLedger,
    categories: List<Category>,
    month: BudgetPeriod,
    previous: BudgetPeriod?,
    limits: LimitsTally? = null,
): MonthRecap {
    val currency = ledger.currency
    val sums = ledger.spent(month)
    val spent = sums.total(currency)
    val before = previous?.let { ledger.spent(it) }
    val dayTotals = (0 until month.days).map { offset ->
        val day = month.start.plus(DatePeriod(days = offset))
        day to ledger.spent(day, day.plus(DatePeriod(days = 1))).total(currency)
    }
    val recorded = dayTotals.indices.map { i ->
        val day = dayTotals[i].first
        ledger.spent(day, day.plus(DatePeriod(days = 1))).byCategory.isNotEmpty() ||
            ledger.earned(day, day.plus(DatePeriod(days = 1))).byCategory.isNotEmpty()
    }
    val knownBefore = before?.let { b -> b.byCategory.filterValues { it.isPositive }.keys }
    return MonthRecap(
        period = month,
        currency = currency,
        spent = spent,
        previousSpent = before?.takeIf { it.byCategory.isNotEmpty() }?.total(currency),
        top = topShares(sums.byCategory, spent),
        weeks = dayTotals.chunked(DAYS_IN_WEEK).map { week -> week.fold(Money.zero(currency)) { acc, d -> acc + d.second } },
        weekdayAverage = average(dayTotals.filter { it.first.dayOfWeek.isWeekday() && it.second.isPositive }.map { it.second }, currency),
        weekendAverage = average(dayTotals.filter { !it.first.dayOfWeek.isWeekday() && it.second.isPositive }.map { it.second }, currency),
        newCategoryIds = if (knownBefore == null || knownBefore.isEmpty()) emptyList()
        else sums.byCategory.filterValues { it.isPositive }.keys.filter { it !in knownBefore }.sorted(),
        recordedDays = recorded.count { it },
        streak = longestRun(recorded),
        streakStart = longestRunStart(recorded),
        limits = limits,
        earned = ledger.earned(month).total(currency),
        missing = sums.missing,
    )
}

private fun topShares(byCategory: Map<String, Money>, total: Money): List<CategoryShare> =
    byCategory.filterValues { it.isPositive }.entries
        .sortedWith(compareByDescending<Map.Entry<String, Money>> { it.value.amountMinor }.thenBy { it.key })
        .take(TOP_CATEGORIES)
        .map { CategoryShare(it.key, it.value, percentOf(it.value, total) ?: 0) }

/** Mean of the days' spending, rounded down to the minor unit; `null` for no days. */
private fun average(values: List<Money>, currency: CurrencyCode): Money? =
    if (values.isEmpty()) null else Money(values.sumOf { it.amountMinor } / values.size, currency)

internal fun longestRun(flags: List<Boolean>): Int {
    var best = 0
    var run = 0
    for (f in flags) {
        run = if (f) run + 1 else 0
        if (run > best) best = run
    }
    return best
}

/** Index where the first longest run of `true` begins; 0 when there is none. */
internal fun longestRunStart(flags: List<Boolean>): Int {
    var best = 0
    var bestStart = 0
    var run = 0
    flags.forEachIndexed { i, f ->
        run = if (f) run + 1 else 0
        if (run > best) {
            best = run
            bestStart = i - run + 1
        }
    }
    return bestStart
}

private fun DayOfWeek.isWeekday() = this != DayOfWeek.SATURDAY && this != DayOfWeek.SUNDAY

const val TOP_CATEGORIES = 5

/** A run of this many days in a row (or more) gets its own story. */
const val MIN_STREAK_CARD = 5
private const val DAYS_IN_WEEK = 7
