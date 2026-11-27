package app.coshell.reports

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.reports.ReportPeriod
import app.coshell.reports.components.CalendarCard
import app.coshell.reports.components.CapitalCard
import app.coshell.reports.components.ConversionCard
import app.coshell.reports.components.CushionCard
import app.coshell.reports.components.FixedCard
import app.coshell.reports.components.DayOperationsCard
import app.coshell.reports.components.IncomeCard
import app.coshell.reports.components.InflationCard
import app.coshell.reports.components.InflationGrowthCard
import app.coshell.reports.components.MembersCard
import app.coshell.reports.components.MonthsCard
import app.coshell.reports.components.SpendingCard
import app.coshell.reports.components.UnusualCard
import app.coshell.ui.format.formatMonth

/** The «Обзор» tab of «Отчёты» (boards `RepA`, #140): the cards of the period («Необычное», #787, under the spending), then the note about rates. */
internal fun LazyListScope.overviewItems(state: ReportsUiState, period: ReportPeriod, actions: ReportsActions) {
    val spending = state.spending
    if (spending == null) {
        item(key = "empty") { Note(stringResource(R.string.reports_empty, stringResource(R.string.reports_period_title))) }
    } else {
        item(key = "spending") {
            SpendingCard(
                spending,
                state.currency,
                ghostCaption = if (state.compare) ghostCaption(period, state.startDay) else null,
                onAll = actions.onAllCategories,
                onBar = actions.onCategory,
            )
        }
    }
    if (state.unusual.isNotEmpty()) {
        item(key = "unusual") { UnusualCard(state.unusual, formatMonth(period.firstMonth.firstDay), onCategory = actions.onCategory) }
    }
    state.conversion?.let { conversion -> item(key = "conversion") { ConversionCard(conversion) } }
    state.months?.let { months -> item(key = "months") { MonthsCard(months, onMonth = actions.onMonth) } }
    state.inflation?.let { inflation ->
        item(key = "inflation") { InflationCard(inflation) }
        if (inflation.fastest.isNotEmpty()) item(key = "inflation-growth") { InflationGrowthCard(inflation) }
    }
    state.income?.let { income -> item(key = "income") { IncomeCard(income, onMonth = actions.onMonth) } }
    state.capital?.let { capital -> item(key = "capital") { CapitalCard(capital, state.currency, onPoint = actions.onCapital) } }
    state.cushion?.let { cushion -> item(key = "cushion") { CushionCard(cushion) } }
    state.fixed?.let { fixed -> item(key = "fixed") { FixedCard(fixed, actions.onAllFixed, actions.onTemplate) } }
    if (state.members.isNotEmpty()) item(key = "members") { MembersCard(state.members, onMember = actions.onMember) }
    item(key = "note") {
        val missing = (spending?.missing.orEmpty() + state.capital?.missing.orEmpty()).distinct().sorted()
        CoShellHint(
            if (missing.isEmpty()) stringResource(R.string.reports_rate_note, state.currency)
            else stringResource(R.string.reports_rate_note, state.currency) + ". " + stringResource(R.string.reports_missing, missing.joinToString()),
        )
    }
}

/** The «Календарь» tab (#553, board `CalTabA`): the month grid, a word for an empty month, the tapped day's operations. */
internal fun LazyListScope.calendarItems(state: ReportsUiState, actions: ReportsActions) {
    val calendar = state.calendar ?: return
    item(key = "calendar") { CalendarCard(calendar, state.currency, state.selectedDay, actions.onDay) }
    if (calendar.isEmpty) item(key = "calendar-empty") { Note(stringResource(R.string.reports_calendar_empty)) }
    if (state.selectedDay != null) item(key = "day") { DayOperationsCard(state.dayOps, actions.onTransaction) }
    if (calendar.missing.isNotEmpty()) {
        item(key = "calendar-missing") { CoShellHint(stringResource(R.string.reports_missing, calendar.missing.joinToString())) }
    }
}

@Composable
internal fun Note(text: String) {
    CoShellCard {
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(Spacing.l),
        )
    }
}
