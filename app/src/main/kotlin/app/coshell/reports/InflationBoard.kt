package app.coshell.reports

import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.reports.ReportDay
import app.coshell.core.domain.reports.ReportLedger
import app.coshell.core.domain.reports.personalInflation
import app.coshell.core.domain.reports.spendingCurrency
import app.coshell.core.model.CurrencyCode
import kotlinx.datetime.YearMonth

/**
 * «Траты в ARS и в USD» (#788, board `Smart788Chart`): the finished months of «По месяцам» in the currency the family pays in
 * ([local]) and in a hard one ([hard]); [localIndex]/[hardIndex] — each month against the first half's average (the chart).
 */
data class Inflation(
    val local: String,
    val hard: String,
    val localPercent: Int,
    val hardPercent: Int,
    val months: List<YearMonth>,
    val localIndex: List<Long>,
    val hardIndex: List<Long>,
    val fastest: List<Growth>,
) {
    /** Nearly all the growth is prices and the rate, not new spending: the line under the phrase. */
    val pricesNotSpending: Boolean get() = localPercent - hardPercent >= NOTABLE && hardPercent < NOTABLE
}

/** A folder of «Быстрее всего росли»: [percent] in the hard currency, signed. */
data class Growth(val categoryId: String, val name: String?, val icon: String?, val color: Long?, val percent: Int)

private const val NOTABLE = 10

/**
 * Pure: the card from the report's [days] and [book]; the hard currency is the report one, or — when the family pays in it — the
 * next totals currency. `null` — fewer than four finished months, one currency only, or nothing to compare.
 */
internal fun buildInflation(input: ReportInput, days: List<ReportDay>, book: RateBook, currencies: List<CurrencyCode>): Inflation? {
    val local = spendingCurrency(days) ?: return null
    val hard = input.ledger.currency.takeIf { it != local } ?: currencies.firstOrNull { it != local } ?: return null
    val finished = input.columns.filter { it.endExclusive <= input.today }
    val result = personalInflation(
        ReportLedger(days, input.categories, book, local),
        ReportLedger(days, input.categories, book, hard),
        input.categories,
        finished,
    ) ?: return null
    val byId = input.categories.associateBy { it.id }
    return Inflation(
        local = local.value,
        hard = hard.value,
        localPercent = result.localPercent,
        hardPercent = result.hardPercent,
        months = result.months.map { it.period.month },
        localIndex = result.localIndex,
        hardIndex = result.hardIndex,
        fastest = result.fastest.map { g -> byId[g.categoryId].let { Growth(g.categoryId, it?.name, it?.icon, it?.color, g.percent) } },
    )
}
