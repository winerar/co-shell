package app.coshell.reports.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.amount.shownAmount
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellHeatGrid
import app.coshell.core.designsystem.components.CoShellHeatLegend
import app.coshell.core.designsystem.components.HeatCell
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.reports.ReportsUiState.Calendar
import app.coshell.ui.format.formatDayMonth
import kotlinx.datetime.LocalDate

/**
 * «Календарь трат» (#553, board `CalTabA`): the month in weeks, the brighter the day the more was spent; a tap on a day shows its
 * operations under the card. The legend ends with how many past days had no spending.
 */
@Composable
internal fun CalendarCard(calendar: Calendar, currency: String, selected: LocalDate?, onDay: (LocalDate) -> Unit, modifier: Modifier = Modifier) {
    val weekdays = listOf(
        R.string.reports_weekday_1, R.string.reports_weekday_2, R.string.reports_weekday_3, R.string.reports_weekday_4,
        R.string.reports_weekday_5, R.string.reports_weekday_6, R.string.reports_weekday_7,
    ).map { stringResource(it) }
    val spent = R.string.reports_calendar_day_spent
    val none = R.string.reports_calendar_day_none
    val cells = calendar.cells.map { cell ->
        val day = formatDayMonth(cell.date)
        HeatCell(
            label = cell.date.day.toString(),
            level = cell.level,
            description = cell.spent?.let { stringResource(spent, day, shownAmount(it), currency) } ?: stringResource(none, day),
            today = cell.today,
            quiet = cell.future,
        )
    }
    val selectedIndex = remember(calendar, selected) { calendar.cells.indexOfFirst { it.date == selected }.takeIf { it >= 0 } }
    CoShellCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(Spacing.m)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.reports_calendar_title), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text(stringResource(R.string.reports_calendar_cue), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            CoShellHeatGrid(weekdays, calendar.leadingBlanks, cells, selectedIndex, onSelect = { onDay(calendar.cells[it].date) })
            CoShellHeatLegend(
                stringResource(R.string.reports_calendar_less),
                stringResource(R.string.reports_calendar_more),
                pluralStringResource(R.plurals.reports_calendar_nospend, calendar.noSpendDays, calendar.noSpendDays),
            )
        }
    }
}
