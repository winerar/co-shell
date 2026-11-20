package app.coshell.reports

import app.coshell.budgets.list.number
import app.coshell.core.designsystem.charts.ChartScale
import app.coshell.core.domain.budgets.BudgetPeriod
import app.coshell.core.domain.budgets.budgetPeriod
import app.coshell.core.domain.reports.ReportLedger
import app.coshell.core.domain.reports.ReportPeriodKind
import app.coshell.core.domain.reports.UsualDirection
import app.coshell.core.domain.reports.UsualMark
import app.coshell.core.domain.reports.usualCategoryMarks
import app.coshell.core.domain.reports.usualMarks
import app.coshell.core.domain.reports.ReportPeriod
import app.coshell.core.domain.reports.categorySpending
import app.coshell.core.domain.reports.monthlySeries
import app.coshell.core.domain.reports.periodComparison
import app.coshell.core.domain.reports.spendingBreakdown
import app.coshell.core.domain.reports.spendingByFolder
import app.coshell.core.model.Category
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.reports.ReportsUiState.Bar
import app.coshell.reports.ReportsUiState.CategoryRow
import app.coshell.reports.ReportsUiState.Mark
import app.coshell.reports.ReportsUiState.Comparison
import app.coshell.reports.ReportsUiState.MonthColumn
import app.coshell.reports.ReportsUiState.Months
import app.coshell.reports.ReportsUiState.Other
import app.coshell.reports.ReportsUiState.Spending
import kotlinx.datetime.LocalDate

/** What a report is counted from; [ledger] must hold the days of [ReportPeriod.loadRange]. */
internal class ReportInput(
    val ledger: ReportLedger,
    val categories: List<Category>,
    val period: ReportPeriod,
    val startDay: Int,
    val today: LocalDate,
    val compare: Boolean,
)

/** The months of «По месяцам» and «Доходы и расходы»: up to the current month while the period fits (#568). */
internal val ReportInput.columns: List<BudgetPeriod>
    get() = period.columnMonths(startDay, today)

/** The column of the period's last month in [columns]; the last one if it is not there. */
internal fun ReportInput.selectedColumn(columns: List<BudgetPeriod>): Int =
    columns.indexOfFirst { it.month == period.lastMonth }.takeIf { it >= 0 } ?: columns.lastIndex

/** The family month of a month report — marks «выше обычного» are counted only for one; `null` for other periods. */
internal val ReportInput.month: BudgetPeriod?
    get() = if (period.kind == ReportPeriodKind.MONTH) budgetPeriod(period.firstMonth, startDay) else null

/** The mark for the screen; «к 11-му» while [month] runs. */
internal fun UsualMark.toUi(month: BudgetPeriod, today: LocalDate, format: MoneyFormat): Mark = Mark(
    up = direction == UsualDirection.ABOVE,
    percent = percent?.let { kotlin.math.abs(it) },
    expected = format.number(expected),
    byDay = today.day.takeIf { month.daysPassed(today) < month.days },
)

private fun Map<String, UsualMark>.toUi(month: BudgetPeriod, today: LocalDate, format: MoneyFormat): Map<String, Mark> =
    mapValues { it.value.toUi(month, today, format) }

/** The days to load for [this] report: the period, the period before it and the months of «По месяцам». */
internal fun ReportPeriod.loadRange(startDay: Int, today: LocalDate): Pair<LocalDate, LocalDate> {
    val previous = previous(startDay)
    val columns = columnMonths(startDay, today)
    return minOf(start, previous.start, columns.first().start) to maxOf(endExclusive, columns.last().endExclusive)
}

/**
 * Pure: «На что ушли деньги» of the period, or `null` when nothing was spent. Each bar is a top-level category with its
 * subcategories added in; the grey mark is the same bar in the period before, on the one scale of the chart.
 */
internal fun buildSpending(input: ReportInput, format: MoneyFormat): Spending? {
    val ledger = input.ledger
    val currency = ledger.currency
    val period = input.period
    val sums = ledger.spent(period.start, period.endExclusive)
    val breakdown = spendingBreakdown(sums, input.categories, currency)
    if (breakdown.isEmpty) return null
    val previous = period.previous(input.startDay)
    val previousSums = if (input.compare) ledger.spent(previous.start, previous.endExclusive) else null
    val previousFolders = previousSums?.let { spendingByFolder(it, input.categories, currency) }
    val byId = input.categories.associateBy { it.id }
    val month = input.month
    val folderMarks = month?.let { m -> usualMarks(ledger, input.categories, m, input.startDay, input.today).toUi(m, input.today, format) }.orEmpty()
    val categoryMarks = month?.let { m -> usualCategoryMarks(ledger, m, input.startDay, input.today).toUi(m, input.today, format) }.orEmpty()

    fun ghostOf(id: String): Money? = previousFolders?.let { it[id] ?: Money.zero(currency) }
    val scale = ChartScale.of(breakdown.slices.flatMap { listOf(it.amount.amountMinor, ghostOf(it.categoryId)?.amountMinor) })
    val bars = breakdown.slices.map { slice ->
        val category = byId[slice.categoryId]
        val ghost = ghostOf(slice.categoryId)
        Bar(
            categoryId = slice.categoryId,
            name = category?.name,
            icon = category?.icon,
            color = category?.color,
            value = format.number(slice.amount),
            percent = slice.percent,
            fraction = scale.of(slice.amount.amountMinor),
            ghost = ghost?.let { scale.of(it.amountMinor) },
            ghostValue = ghost?.let { format.number(it) },
            mark = folderMarks[slice.categoryId],
        )
    }
    val other = breakdown.other?.let { o ->
        Other(byId[o.slices.first().categoryId]?.name, o.slices.size - 1, format.number(o.amount), o.percent)
    }

    val flat = categorySpending(sums, previousSums, currency)
    val flatScale = ChartScale.of(flat.flatMap { listOf(it.amount.amountMinor, if (input.compare) it.previous.amountMinor else null) })
    val rows = flat.map { row ->
        val category = byId[row.categoryId]
        CategoryRow(
            categoryId = row.categoryId,
            name = category?.name,
            icon = category?.icon,
            color = category?.color,
            value = format.number(row.amount),
            percent = row.percent,
            fraction = flatScale.of(row.amount.amountMinor),
            ghost = if (input.compare) flatScale.of(row.previous.amountMinor) else null,
            ghostValue = if (input.compare) format.number(row.previous) else null,
            growthMinor = row.growth.amountMinor,
            amountMinor = row.amount.amountMinor,
            parentId = category?.parentId?.takeIf { it in byId },
            mark = categoryMarks[row.categoryId],
        )
    }

    return Spending(
        total = format.number(breakdown.total),
        comparison = if (input.compare) comparison(input, previous, format) else null,
        bars = bars,
        other = other,
        categories = rows,
        missing = breakdown.missing.map { it.value }.sorted(),
    )
}

/** The line under the total; `null` for a period that has not started. */
private fun comparison(input: ReportInput, previous: ReportPeriod, format: MoneyFormat): Comparison? {
    if (input.period.daysPassed(input.today) == 0) return null
    val c = periodComparison(input.ledger, input.period, previous, input.today)
    return if (c.isComplete) {
        Comparison.Whole(input.period.kind, format.number(c.previousTotal), more = c.spent > c.previousTotal)
    } else {
        Comparison.SoFar(c.daysPassed, input.period.kind, c.sameDay.day, format.number(c.previousSameDays), faster = c.difference.isPositive)
    }
}

/** Pure: «По месяцам» — spending of each month of [ReportInput.columns], the period's last month highlighted. */
internal fun buildMonths(input: ReportInput, format: MoneyFormat): Months {
    val series = monthlySeries(input.ledger, input.columns)
    val scale = ChartScale.of(series.map { it.spent.amountMinor })
    return Months(
        columns = series.map { MonthColumn(it.period.month, format.number(it.spent), scale.of(it.spent.amountMinor)) },
        selected = input.selectedColumn(series.map { it.period }),
    )
}
