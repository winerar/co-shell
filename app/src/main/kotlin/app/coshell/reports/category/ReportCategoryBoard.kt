package app.coshell.reports.category

import app.coshell.budgets.list.number
import app.coshell.core.designsystem.charts.ChartScale
import app.coshell.core.domain.budgets.budgetCategoryIds
import app.coshell.core.domain.budgets.BudgetPeriod
import app.coshell.core.domain.budgets.budgetPeriod
import app.coshell.core.domain.budgets.budgetPeriodOf
import app.coshell.core.domain.reports.CategoryTrend
import app.coshell.core.domain.reports.LONG_USUAL_MONTHS
import app.coshell.core.domain.reports.ReportLedger
import app.coshell.core.domain.reports.ReportPeriod
import app.coshell.core.domain.reports.ReportPeriodKind
import app.coshell.core.domain.reports.TREND_MONTHS
import app.coshell.core.domain.reports.categoryDrill
import app.coshell.core.domain.reports.categoryTrend
import app.coshell.core.domain.reports.monthsBefore
import app.coshell.core.domain.reports.trendCategoryIds
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.core.model.isBalanceAdjustment
import app.coshell.reports.ReportsUiState.Bar
import app.coshell.reports.ReportsUiState.MonthColumn
import app.coshell.reports.toUi
import app.coshell.transactions.list.buildTransactionDays
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus

/** What the drill is counted from; [ledger] must hold the days of [drillRange]. */
internal class DrillInput(
    val categoryId: String,
    val ledger: ReportLedger,
    val categories: List<Category>,
    val period: ReportPeriod,
    val startDay: Int,
    val compare: Boolean,
    /** The period's operations of the category and its subcategories (any order). */
    val operations: List<Transaction>,
    val accounts: List<Account>,
    val authors: Map<String, String>,
    val timeZone: TimeZone,
    val today: LocalDate,
    /** The month «По месяцам» ends with ([trendChartEnd]), so a tap on an earlier column keeps the later ones (#808). */
    val chartEnd: ReportPeriod = period,
)

/**
 * The last column of «По месяцам», as on the Reports tab (`ReportPeriod.columnMonths`): the current month, but never before the
 * shown [period] and never so late that it drops out of the [TREND_MONTHS] columns (#808). Not a month — the period itself.
 */
internal fun trendChartEnd(period: ReportPeriod, startDay: Int, today: LocalDate): ReportPeriod {
    if (period.kind != ReportPeriodKind.MONTH) return period
    val current = budgetPeriodOf(today, startDay).month
    val latest = period.firstMonth.plus(TREND_MONTHS - 1, DateTimeUnit.MONTH)
    val end = when {
        current < period.firstMonth -> period.firstMonth
        current > latest -> latest
        else -> current
    }
    return ReportPeriod.month(end, startDay)
}

/**
 * The days the drill reads: the period, the one before and, for a month, the [LONG_USUAL_MONTHS] before it (the trend), up to
 * the end of [chartEnd] ([trendChartEnd]) — the later months «По месяцам» still shows.
 */
internal fun drillRange(period: ReportPeriod, startDay: Int, chartEnd: ReportPeriod = period): Pair<LocalDate, LocalDate> {
    val previous = period.previous(startDay)
    val yearBefore = if (period.kind == ReportPeriodKind.MONTH) {
        monthsBefore(budgetPeriod(period.firstMonth, startDay), startDay, LONG_USUAL_MONTHS).first().start
    } else period.start
    return minOf(previous.start, period.start, yearBefore) to maxOf(period.endExclusive, chartEnd.endExclusive)
}

/** The category and its subcategories: what the drill shows operations of. */
internal fun drillCategoryIds(categoryId: String, categories: List<Category>): Set<String> = budgetCategoryIds(categoryId, categories)

/**
 * Pure: «Из чего сложилось». The head and the bars are the report's numbers (each day at its own rate, refunds taken off);
 * the grey mark of a bar is the same subcategory in the period before, on the one scale of the bars. The operations are
 * the expenses and refunds behind them, by day, each in its own currency.
 */
internal fun buildCategoryDrill(input: DrillInput, format: MoneyFormat, moneyFormat: MoneyFormat): ReportCategoryUiState {
    val currency = input.ledger.currency
    val byId = input.categories.associateBy { it.id }
    val sums = input.ledger.spent(input.period.start, input.period.endExclusive)
    val drill = categoryDrill(sums, input.categoryId, input.categories, currency)
    val previous = input.period.previous(input.startDay)
    val previousSums = if (input.compare) input.ledger.spent(previous.start, previous.endExclusive) else null

    val parts = drill?.parts.orEmpty()
    fun ghostOf(id: String): Money? = previousSums?.let { it.byCategory[id] ?: Money.zero(currency) }
    val scale = ChartScale.of(parts.flatMap { listOf(it.amount.amountMinor, ghostOf(it.categoryId)?.amountMinor) })
    val bars = parts.map { part ->
        val category = byId[part.categoryId]
        val ghost = ghostOf(part.categoryId)
        Bar(
            categoryId = part.categoryId,
            name = category?.name,
            icon = category?.icon,
            color = category?.color,
            value = format.number(part.amount),
            percent = part.percent,
            fraction = scale.of(part.amount.amountMinor),
            ghost = ghost?.let { scale.of(it.amountMinor) },
            ghostValue = ghost?.let { format.number(it) },
        )
    }

    val ids = drillCategoryIds(input.categoryId, input.categories)
    val shown = input.operations.filter { it.categoryId in ids && it.type != TransactionType.TRANSFER && !it.isBalanceAdjustment }
    val days = buildTransactionDays(
        shown,
        input.accounts.associateBy { it.id },
        byId,
        moneyFormat,
        input.timeZone,
        input.today,
        input.authors,
    )
    val month = if (input.period.kind == ReportPeriodKind.MONTH) budgetPeriod(input.period.firstMonth, input.startDay) else null
    val trendIds = trendCategoryIds(input.categoryId, input.categories)
    val trend = month?.let { categoryTrend(input.ledger, trendIds, it, input.startDay, input.today) }
    val end = input.chartEnd.takeIf { it.kind == ReportPeriodKind.MONTH && it.firstMonth > input.period.firstMonth }
    val chart = end?.let { categoryTrend(input.ledger, trendIds, budgetPeriod(it.firstMonth, input.startDay), input.startDay, input.today) }
    return ReportCategoryUiState(
        name = byId[input.categoryId]?.name,
        period = input.period,
        currency = currency.value,
        total = drill?.let { format.number(it.amount) },
        percent = drill?.percent ?: 0,
        parts = bars,
        days = days,
        missing = sums.missing.map { it.value }.sorted(),
        mark = month?.let { m -> trend?.mark?.toUi(m, input.today, format) },
        trend = trend?.let { trendOf(it, chart ?: it, month, format) },
    )
}

/**
 * «По месяцам»: the columns of [chart] (the months up to the one the drill was opened on), the averages of [trend] (the shown
 * [month]); the months before the history are left out; only the shown month carries its value.
 */
private fun trendOf(trend: CategoryTrend, chart: CategoryTrend, month: BudgetPeriod, format: MoneyFormat): Trend? {
    val months = chart.months.mapNotNull { m -> m.amount?.let { m.period to it } }
    if (months.isEmpty()) return null
    val scale = ChartScale.of(months.map { it.second.amountMinor } + listOfNotNull(trend.average12?.amountMinor))
    val selected = months.indexOfFirst { it.first == month }.takeIf { it >= 0 } ?: months.lastIndex
    return Trend(
        columns = months.mapIndexed { i, (period, amount) ->
            MonthColumn(period.month, if (i == selected) format.number(amount) else "", scale.of(amount.amountMinor))
        },
        selected = selected,
        average = trend.average12?.let { scale.of(it.amountMinor) },
        recent = trend.average3?.let { format.number(it) },
        recentMonths = trend.recentMonths,
        long = trend.average12?.takeIf { trend.longMonths > trend.recentMonths }?.let { format.number(it) },
        longMonths = trend.longMonths,
    )
}
