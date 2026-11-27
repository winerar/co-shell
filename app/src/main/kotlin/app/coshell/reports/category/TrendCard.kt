package app.coshell.reports.category

import app.coshell.ui.motion.SwapText
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import app.coshell.R
import app.coshell.core.designsystem.amount.shownAmount
import app.coshell.core.designsystem.charts.ChartColumn
import app.coshell.core.designsystem.charts.CoShellColumns
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.reports.LONG_USUAL_MONTHS
import app.coshell.reports.ReportsUiState.Mark
import app.coshell.reports.components.MarkPill
import app.coshell.reports.components.markDescription
import app.coshell.reports.components.markLine
import app.coshell.ui.format.formatMonthShort
import app.coshell.ui.format.formatMonthYear
import kotlinx.datetime.YearMonth

/**
 * «По месяцам» of one category (#238, board `Tr238DrillA`): a column per month of its history up to the shown one,
 * the dashed line of the long average, «За 3 мес.: 62 · За год: 65» under them. A tap on a column — that month.
 */
@Composable
internal fun TrendCard(trend: Trend, onMonth: (YearMonth) -> Unit, modifier: Modifier = Modifier) {
    CoShellCard(modifier.fillMaxWidth()) {
        Row(Modifier.padding(start = Spacing.l, end = Spacing.l, top = Spacing.l), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.report_trend_title), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            if (trend.average != null) {
                Text(stringResource(R.string.report_trend_caption), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        val columns = trend.columns.map { column ->
            ChartColumn(
                label = formatMonthShort(column.month),
                valueLabel = column.value,
                fraction = column.fraction,
                description = stringResource(R.string.reports_months_description, formatMonthYear(column.month), shownAmount(column.value)),
            )
        }
        CoShellColumns(
            columns,
            Modifier.padding(start = Spacing.l, end = Spacing.l, top = Spacing.l),
            selected = trend.selected,
            average = trend.average,
            onSelect = { onMonth(trend.columns[it].month) },
        )
        val averages = listOfNotNull(
            trend.recent?.let { stringResource(R.string.report_trend_months, trend.recentMonths, shownAmount(it)) },
            trend.long?.let {
                if (trend.longMonths >= LONG_USUAL_MONTHS) stringResource(R.string.report_trend_year, shownAmount(it))
                else stringResource(R.string.report_trend_months, trend.longMonths, shownAmount(it))
            },
        )
        if (averages.isNotEmpty()) {
            SwapText(
                averages.joinToString("   "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = Spacing.l, end = Spacing.l, bottom = Spacing.l),
            )
        } else {
            Spacer(Modifier.height(Spacing.s))
        }
    }
}

/** «↑ 45%  больше обычного · к 11-му обычно 61» under the head of «Из чего сложилось»; TalkBack — in words. */
@Composable
internal fun MarkRow(mark: Mark, modifier: Modifier = Modifier) {
    val line = markLine(mark)
    val spoken = markDescription(mark)
    Row(
        modifier.clearAndSetSemantics { contentDescription = if (mark.percent == null) spoken else "$spoken. $line" },
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MarkPill(mark)
        Text(line, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
