package app.coshell.reports

import app.coshell.core.domain.budgets.BudgetPeriod
import app.coshell.core.domain.reports.HISTORY_MONTHS
import app.coshell.core.domain.reports.NetWorth
import app.coshell.core.domain.reports.ReportPeriod
import app.coshell.core.domain.reports.cushion
import app.coshell.core.domain.reports.monthlySeries
import app.coshell.core.domain.reports.reportMonths
import app.coshell.core.model.MoneyFormat
import app.coshell.reports.ReportsUiState.Cushion
import app.coshell.reports.ReportsUiState.CushionGloss

/** The months «Подушка» averages over: half a year up to the period's last month, plus that month (it may still run). */
internal fun ReportPeriod.cushionMonths(startDay: Int): List<BudgetPeriod> = reportMonths(lastMonth, HISTORY_MONTHS + 1, startDay)

/**
 * Pure: «Подушка» (#241, boards `Cush241Reports`, `Cush241Edge`) at the end of the period, from the money of «Капитал» ([worth])
 * and the spending of the last full months. The phrase under the number — the first of [CushionGloss] that fits.
 */
internal fun buildCushion(input: ReportInput, worth: NetWorth, format: MoneyFormat): Cushion {
    val money = format.format(worth.now)
    val c = cushion(worth, monthlySeries(input.ledger, input.period.cushionMonths(input.startDay))) ?: return Cushion(money)
    val tenths = c.tenths
    val gloss = when {
        tenths <= 0 -> CushionGloss.EMPTY
        tenths < MONTH_TENTHS -> CushionGloss.LOW
        c.months < HISTORY_MONTHS -> CushionGloss.FEW
        c.missing.isNotEmpty() -> CushionGloss.NO_RATE
        else -> CushionGloss.NORMAL
    }
    return Cushion(
        money = money,
        months = if (tenths >= WHOLE_FROM_TENTHS) "${tenths / 10}" else "${tenths / 10}${format.decimalSeparator}${tenths % 10}",
        fraction = (tenths.toFloat() / (HISTORY_MONTHS * MONTH_TENTHS)).coerceIn(0f, 1f),
        gloss = gloss,
        monthlySpend = format.format(c.monthlySpend),
        historyMonths = c.months,
        weeks = c.weeks.toInt(),
        missing = c.missing.map { it.value }.sorted(),
    )
}

private const val MONTH_TENTHS = 10L

/** From ten months on the tenth says nothing: «≈ 12». */
private const val WHOLE_FROM_TENTHS = 100L
