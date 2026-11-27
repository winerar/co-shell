package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetPeriod
import app.coshell.core.model.Category
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.TransactionType

/** One month of «Траты в песо и в долларах»: spending counted in the local and in the hard currency at the rate of each day. */
data class InflationMonth(val period: BudgetPeriod, val local: Money, val hard: Money) {
    /** The month against the first half's average, ×10 (1000 — the same; 1380 — +38 %): the chart's points. */
    internal fun index(baseLocal: Money, baseHard: Money): Pair<Long, Long> =
        local.scale(INDEX, baseLocal.amountMinor).amountMinor to hard.scale(INDEX, baseHard.amountMinor).amountMinor
}

/** A folder and how its spending in the hard currency changed from the first half of the months to the second. */
data class CategoryGrowth(val categoryId: String, val percent: Int)

/**
 * «Личная инфляция» (#788, poll 2026-11-27 «А», board `Smart788Chart`): did the family spend more, or did the local currency
 * just get cheaper? The average month of the second half of [months] against the first, in [local] and in [hard].
 * [localIndex]/[hardIndex] — each month against the first half's average, ×10 (the chart). [fastest] — the folders that grew most in [hard].
 */
data class PersonalInflation(
    val local: CurrencyCode,
    val hard: CurrencyCode,
    val months: List<InflationMonth>,
    val localPercent: Int,
    val hardPercent: Int,
    val localIndex: List<Long>,
    val hardIndex: List<Long>,
    val fastest: List<CategoryGrowth>,
)

/** Fewer finished months than this say nothing about prices: one expensive month is not a trend. */
const val INFLATION_MIN_MONTHS = 4

/** «Быстрее всего росли»: at most this many folders. */
const val INFLATION_TOP = 3

/** A folder below this share of the spending is too small to tell a trend (one gift makes «+300 %»). */
const val INFLATION_MIN_SHARE_PERCENT = 5

/**
 * The currency the family actually pays in: the one of the most spending days in [days] (amounts of different currencies cannot
 * be compared without rates, a habit can — ties go to the alphabet). `null` — nothing spent.
 */
fun spendingCurrency(days: List<ReportDay>): CurrencyCode? =
    days.filter { it.type == TransactionType.EXPENSE }
        .groupBy { it.amount.currency }
        .mapValues { (_, list) -> list.map { it.day }.distinct().size.toLong() }
        .entries.sortedWith(compareByDescending<Map.Entry<CurrencyCode, Long>> { it.value }.thenBy { it.key.value })
        .firstOrNull()?.key

/**
 * [PersonalInflation] over the finished [months] (oldest first) from [localLedger] and [hardLedger] — the same days counted in two
 * currencies. `null` with fewer than [INFLATION_MIN_MONTHS] months, the same currency twice, or nothing spent in the first half.
 */
fun personalInflation(localLedger: ReportLedger, hardLedger: ReportLedger, categories: List<Category>, months: List<BudgetPeriod>): PersonalInflation? {
    if (months.size < INFLATION_MIN_MONTHS || localLedger.currency == hardLedger.currency) return null
    val local = localLedger.currency
    val hard = hardLedger.currency
    val rows = months.map { InflationMonth(it, localLedger.spent(it).total(local).atLeastZero(), hardLedger.spent(it).total(hard).atLeastZero()) }
    val half = months.size / 2
    val firstLocal = rows.take(half).map { it.local }.average(local)
    val firstHard = rows.take(half).map { it.hard }.average(hard)
    if (!firstLocal.isPositive || !firstHard.isPositive) return null
    val lastLocal = rows.takeLast(half).map { it.local }.average(local)
    val lastHard = rows.takeLast(half).map { it.hard }.average(hard)
    val index = rows.map { it.index(firstLocal, firstHard) }
    return PersonalInflation(
        local = local,
        hard = hard,
        months = rows,
        localPercent = percentOf(lastLocal - firstLocal, firstLocal) ?: 0,
        hardPercent = percentOf(lastHard - firstHard, firstHard) ?: 0,
        localIndex = index.map { it.first },
        hardIndex = index.map { it.second },
        fastest = fastestGrowing(hardLedger, categories, months, half),
    )
}

private fun fastestGrowing(ledger: ReportLedger, categories: List<Category>, months: List<BudgetPeriod>, half: Int): List<CategoryGrowth> {
    val currency = ledger.currency
    val byMonth = months.map { spendingByFolder(ledger.spent(it), categories, currency).mapValues { it.value.atLeastZero() } }
    val zero = Money.zero(currency)
    val total = byMonth.map { m -> m.values.fold(zero) { acc, v -> acc + v } }.average(currency)
    val threshold = total.scale(INFLATION_MIN_SHARE_PERCENT.toLong(), 100)
    return byMonth.flatMap { it.keys }.toSortedSet().mapNotNull { id ->
        val series = byMonth.map { it[id] ?: zero }
        if (series.average(currency) < threshold) return@mapNotNull null
        val first = series.take(half).average(currency)
        val last = series.takeLast(half).average(currency)
        percentOf(last - first, first)?.let { CategoryGrowth(id, it) }
    }.sortedWith(compareByDescending<CategoryGrowth> { it.percent }.thenBy { it.categoryId }).take(INFLATION_TOP)
}

private const val INDEX = 1_000L
