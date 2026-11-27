package app.coshell.reports

import app.coshell.budgets.list.number
import app.coshell.core.designsystem.charts.ChartScale
import app.coshell.core.domain.budgets.BudgetPeriod
import app.coshell.core.domain.reports.UnusualRow
import app.coshell.core.domain.reports.unusualSpending
import app.coshell.core.domain.reports.usualMarks
import app.coshell.core.model.Category
import app.coshell.core.model.MoneyFormat
import app.coshell.reports.ReportsUiState.Mark
import kotlinx.datetime.LocalDate

/**
 * A row of «Необычное» (#787, board `Smart787Card`) — in «Отчётах» and in the story «Не как обычно» of «Итогов месяца»:
 * the bar is what was spent, the grey mark on it is the usual by now ([usual]), both on one scale of the card.
 */
data class UnusualLine(
    val categoryId: String,
    /** `null` — a category deleted on another phone while its operations stayed. */
    val name: String?,
    val icon: String?,
    val color: Long?,
    val value: String,
    val fraction: Float,
    val usual: Float,
    val mark: Mark,
)

/** Pure: the rows of [rows] for [month] as of [today] (the last day for a finished month). */
internal fun unusualLines(rows: List<UnusualRow>, categories: List<Category>, month: BudgetPeriod, today: LocalDate, format: MoneyFormat): List<UnusualLine> {
    val byId = categories.associateBy { it.id }
    val scale = ChartScale.of(rows.flatMap { listOf(it.mark.amount.amountMinor, it.mark.expected.amountMinor) })
    return rows.map { row ->
        val category = byId[row.categoryId]
        UnusualLine(
            categoryId = row.categoryId,
            name = category?.name,
            icon = category?.icon,
            color = category?.color,
            value = format.number(row.mark.amount),
            fraction = scale.of(row.mark.amount.amountMinor),
            usual = scale.of(row.mark.expected.amountMinor),
            mark = row.mark.toUi(month, today, format),
        )
    }
}

/** Pure: «Необычное» of a month report; empty for other periods and when nothing stands out. */
internal fun buildUnusual(input: ReportInput, format: MoneyFormat): List<UnusualLine> {
    val month = input.month ?: return emptyList()
    val rows = unusualSpending(usualMarks(input.ledger, input.categories, month, input.startDay, input.today))
    return unusualLines(rows, input.categories, month, input.today, format)
}
