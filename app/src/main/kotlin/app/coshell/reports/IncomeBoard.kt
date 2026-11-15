package app.coshell.reports

import app.coshell.budgets.list.number
import app.coshell.core.designsystem.charts.ChartScale
import app.coshell.core.domain.reports.incomeExpenses
import app.coshell.core.domain.reports.isRunning
import app.coshell.core.domain.reports.monthlySeries
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.reports.ReportsUiState.Income
import app.coshell.reports.ReportsUiState.IncomeColumn

/**
 * Pure: «Доходы и расходы» (#189) over the months of «По месяцам», the period's last month selected; `null` when nothing
 * came in or went out in all of them.
 */
internal fun buildIncome(input: ReportInput, format: MoneyFormat): Income? {
    val series = monthlySeries(input.ledger, input.period.columnMonths(input.startDay))
    val picture = incomeExpenses(series, input.today)
    if (picture.isEmpty) return null
    val scale = ChartScale.of(series.flatMap { listOf(it.earned.amountMinor, it.spent.amountMinor) })
    val shown = series.last()
    return Income(
        columns = series.map {
            IncomeColumn(it.period.month, format.number(it.earned), format.number(it.spent), scale.of(it.earned.amountMinor), scale.of(it.spent.amountMinor))
        },
        selected = series.lastIndex,
        earned = format.number(shown.earned),
        spent = format.number(shown.spent),
        saved = format.signed(shown.saved),
        overspent = shown.saved.isNegative,
        rate = shown.savingsRate,
        running = shown.isRunning(input.today),
        averageRate = picture.averageRate,
        averageMonths = picture.averageMonths,
    )
}

/** «+270», «-110», «0». */
internal fun MoneyFormat.signed(money: Money): String = if (money.isPositive) "+" + number(money) else number(money)
