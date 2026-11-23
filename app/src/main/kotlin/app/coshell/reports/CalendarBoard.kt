package app.coshell.reports

import app.coshell.budgets.list.number
import app.coshell.core.domain.budgets.budgetPeriod
import app.coshell.core.domain.reports.spendingCalendar
import app.coshell.core.model.MoneyFormat
import app.coshell.reports.ReportsUiState.Calendar
import app.coshell.reports.ReportsUiState.CalendarCell
import kotlinx.datetime.isoDayNumber

/**
 * Pure: «Календарь трат» (#553, board `CalTabA`) of the period's last month in the family's months: brightness by quartiles
 * (`spendingCalendar`), amounts without the code, weeks start on Monday.
 */
internal fun buildCalendar(input: ReportInput, format: MoneyFormat): Calendar {
    val month = budgetPeriod(input.period.lastMonth, input.startDay)
    val calendar = spendingCalendar(input.ledger, month, input.today)
    return Calendar(
        leadingBlanks = month.start.dayOfWeek.isoDayNumber - 1,
        cells = calendar.days.map { day ->
            CalendarCell(
                date = day.day,
                level = day.level,
                spent = day.spent.takeIf { it.amountMinor > 0 }?.let { format.number(it) },
                today = day.day == input.today,
                future = day.future,
            )
        },
        noSpendDays = calendar.noSpendDays,
        missing = calendar.missing.map { it.value }.sorted(),
    )
}
