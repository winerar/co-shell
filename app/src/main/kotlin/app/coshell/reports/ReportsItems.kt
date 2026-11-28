package app.coshell.reports

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyListScope
import app.coshell.core.designsystem.motion.listItemMotion
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
import app.coshell.reports.components.ForecastCard
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
import app.coshell.ui.edit.longPressToEdit
import app.coshell.core.domain.settings.ReportCard
import app.coshell.core.domain.settings.ReportLayout

/**
 * One card of the feed. It fades and moves into place when the period, the currency or the tab brings other cards
 * (owner 2026-11-27: switching data in report cards is smooth) — nothing pops in or out. [onEdit] — holding it starts the
 * editing of the feed (#826); `null` — not a card that can be edited.
 */
private fun LazyListScope.card(key: String, onEdit: (() -> Unit)? = null, content: @Composable () -> Unit) {
    item(key = key) {
        Box(listItemMotion().longPressToEdit(enabled = onEdit != null) { onEdit?.invoke() }) { content() }
    }
}

/**
 * The «Обзор» tab of «Отчёты» (boards `RepA`, #140): the cards of the period in the order of [layout] (#826: moved and hidden
 * on this phone), then the note about rates. A card with nothing to say now is simply absent.
 */
internal fun LazyListScope.overviewItems(
    state: ReportsUiState,
    period: ReportPeriod,
    actions: ReportsActions,
    layout: ReportLayout = ReportLayout(),
    onEdit: () -> Unit = {},
) {
    val spending = state.spending
    if (spending == null) {
        card(key = "empty") { Note(stringResource(R.string.reports_empty, stringResource(R.string.reports_period_title))) }
    }
    layout.visible.forEach { reportCard(it, state, period, actions, onEdit) }
    card(key = "note") {
        val missing = (spending?.missing.orEmpty() + state.capital?.missing.orEmpty()).distinct().sorted()
        CoShellHint(
            if (missing.isEmpty()) stringResource(R.string.reports_rate_note, state.currency)
            else stringResource(R.string.reports_rate_note, state.currency) + ". " + stringResource(R.string.reports_missing, missing.joinToString()),
        )
    }
}

/** One card of [overviewItems], if it has something to show. */
private fun LazyListScope.reportCard(which: ReportCard, state: ReportsUiState, period: ReportPeriod, actions: ReportsActions, onEdit: () -> Unit) {
    val key = which.name
    when (which) {
        ReportCard.SPENDING -> state.spending?.let { spending ->
            card(key, onEdit) {
                SpendingCard(
                    spending,
                    state.currency,
                    ghostCaption = if (state.compare) ghostCaption(period, state.startDay) else null,
                    onAll = actions.onAllCategories,
                    onBar = actions.onCategory,
                )
            }
        }
        ReportCard.UNUSUAL -> if (state.unusual.isNotEmpty()) {
            card(key, onEdit) { UnusualCard(state.unusual, formatMonth(period.firstMonth.firstDay), onCategory = actions.onCategory) }
        }
        ReportCard.CONVERSION -> state.conversion?.let { card(key, onEdit) { ConversionCard(it) } }
        ReportCard.MONTHS -> state.months?.let { card(key, onEdit) { MonthsCard(it, onMonth = actions.onMonth) } }
        ReportCard.INFLATION -> state.inflation?.let { card(key, onEdit) { InflationCard(it) } }
        ReportCard.INFLATION_GROWTH -> state.inflation?.takeIf { it.fastest.isNotEmpty() }?.let { card(key, onEdit) { InflationGrowthCard(it) } }
        ReportCard.INCOME -> state.income?.let { card(key, onEdit) { IncomeCard(it, onMonth = actions.onMonth) } }
        ReportCard.CAPITAL -> state.capital?.let { card(key, onEdit) { CapitalCard(it, state.currency, onPoint = actions.onCapital) } }
        ReportCard.CUSHION -> state.cushion?.let { card(key, onEdit) { CushionCard(it) } }
        ReportCard.FORECAST -> state.forecast?.let { card(key, onEdit) { ForecastCard(it) } }
        ReportCard.FIXED -> state.fixed?.let { card(key, onEdit) { FixedCard(it, actions.onAllFixed, actions.onTemplate) } }
        ReportCard.MEMBERS -> if (state.members.isNotEmpty()) card(key, onEdit) { MembersCard(state.members, onMember = actions.onMember) }
    }
}

/** The «Календарь» tab (#553, board `CalTabA`): the month grid, a word for an empty month, the tapped day's operations. */
internal fun LazyListScope.calendarItems(state: ReportsUiState, actions: ReportsActions) {
    val calendar = state.calendar ?: return
    card(key = "calendar") { CalendarCard(calendar, state.currency, state.selectedDay, actions.onDay) }
    if (calendar.isEmpty) card(key = "calendar-empty") { Note(stringResource(R.string.reports_calendar_empty)) }
    if (state.selectedDay != null) card(key = "day") { DayOperationsCard(state.dayOps, actions.onTransaction) }
    if (calendar.missing.isNotEmpty()) {
        card(key = "calendar-missing") { CoShellHint(stringResource(R.string.reports_missing, calendar.missing.joinToString())) }
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
