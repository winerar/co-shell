package app.coshell.reports.category

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellDivider
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.RowGroupPosition
import app.coshell.core.designsystem.components.rowGroupSegment
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.reports.ReportPeriod
import app.coshell.core.domain.reports.ReportPeriodKind
import app.coshell.reports.categoryName
import app.coshell.reports.components.SpendingBar
import app.coshell.reports.periodTitle
import app.coshell.transactions.list.TransactionsUiState.Day
import app.coshell.transactions.list.components.DayHeader
import app.coshell.transactions.list.components.TransactionRow
import app.coshell.ui.format.formatMonth
import app.coshell.ui.list.SectionTitle
import app.coshell.ui.top.ScreenTopBar
import kotlinx.datetime.YearMonth

/**
 * «Дом» (board `RepDrill`, #188): «Дом за ноябрь · 290 USD · 38% трат» with the subcategories' bars, then «Из чего
 * сложилось» — the operations by day with the day's total. For a month: «↑ 45% больше обычного» under the head and
 * «По месяцам» of the category (#238); a tap on a column shows that month. Each day is one lazy item drawing its piece of the card,
 * so a year of a large category scrolls lightly. A tap on an operation opens it for editing.
 */
@Composable
fun ReportCategoryScreen(
    state: ReportCategoryUiState,
    onBack: () -> Unit,
    onTransactionClick: (String) -> Unit,
    onMonth: (YearMonth) -> Unit = {},
) {
    Scaffold(
        topBar = { ScreenTopBar(if (state.isLoading) "" else categoryName(state.name), onBack = onBack) },
    ) { padding ->
        if (state.isLoading) return@Scaffold
        val side = Modifier.padding(horizontal = Spacing.l)
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = Spacing.xl)) {
            item(key = "head") { Head(state, side.padding(vertical = Spacing.s)) }
            state.trend?.let { trend -> item(key = "trend") { TrendCard(trend, onMonth, side.padding(vertical = Spacing.s)) } }
            item(key = "operationsTitle") {
                SectionTitle(pluralStringResource(R.plurals.report_drill_operations, state.operationCount, state.operationCount))
            }
            if (state.days.isEmpty()) {
                item(key = "noOperations") { CoShellHint(stringResource(R.string.report_drill_no_operations), side.padding(vertical = Spacing.s)) }
            }
            itemsIndexed(state.days, key = { _, day -> day.date.toString() }) { index, day ->
                DayPart(day, RowGroupPosition(index, state.days.size), onTransactionClick, side)
            }
            item(key = "note") {
                val note = stringResource(R.string.reports_rate_note, state.currency)
                val text = if (state.missing.isEmpty()) note else note + ". " + stringResource(R.string.reports_missing, state.missing.joinToString())
                CoShellHint(text, side.padding(vertical = Spacing.m))
            }
        }
    }
}

/** The card's head line and the subcategories' bars. */
@Composable
private fun Head(state: ReportCategoryUiState, modifier: Modifier = Modifier) {
    val period = state.period ?: return
    val name = categoryName(state.name)
    Column(modifier.fillMaxWidth().rowGroupSegment(RowGroupPosition(0, 1)).padding(bottom = Spacing.s)) {
        Row(Modifier.padding(start = Spacing.l, end = Spacing.l, top = Spacing.l, bottom = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
            Text(
                headTitle(name, period),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(end = Spacing.s),
            )
            Text(
                state.total?.let { stringResource(R.string.report_drill_share, it, state.currency, state.percent) }
                    ?: stringResource(R.string.report_drill_refunds),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        state.mark?.let { MarkRow(it, Modifier.padding(start = Spacing.l, end = Spacing.l, bottom = Spacing.s)) }
        state.parts.forEach { bar -> SpendingBar(bar, Modifier.padding(horizontal = Spacing.l)) }
    }
}

/** One day of «Из чего сложилось»: its title with the day's total, then its operations. */
@Composable
private fun DayPart(day: Day, position: RowGroupPosition, onTransactionClick: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().rowGroupSegment(position)) {
        DayHeader(day, background = Color.Transparent)
        day.rows.forEachIndexed { index, row ->
            if (index > 0) CoShellDivider(Modifier.padding(start = Spacing.l))
            TransactionRow(row, onClick = onTransactionClick, background = Color.Transparent)
        }
    }
}

/** «Дом за ноябрь» (a year that is not this one is added), «Дом за 4-й квартал 2026», «Дом за 2026 год», «Дом · 1 нояб. – 10 нояб.». */
@Composable
private fun headTitle(name: String, period: ReportPeriod): String = when (period.kind) {
    ReportPeriodKind.MONTH -> {
        val month = formatMonth(period.firstMonth.firstDay)
        val year = period.firstMonth.year
        stringResource(R.string.report_drill_head, name, if (year == java.time.LocalDate.now().year) month else "$month $year")
    }
    ReportPeriodKind.QUARTER -> stringResource(R.string.report_drill_head, name, periodTitle(period))
    ReportPeriodKind.YEAR -> stringResource(R.string.report_drill_head_year, name, period.firstMonth.year)
    ReportPeriodKind.CUSTOM -> stringResource(R.string.report_drill_head_range, name, periodTitle(period))
}
