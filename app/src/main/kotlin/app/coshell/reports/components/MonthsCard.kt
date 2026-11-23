package app.coshell.reports.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.amount.shownAmount
import app.coshell.core.designsystem.charts.ChartColumn
import app.coshell.core.designsystem.charts.CoShellColumns
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.reports.ReportsUiState.Months
import app.coshell.ui.format.formatMonthShort
import app.coshell.ui.format.formatMonthYear
import kotlinx.datetime.YearMonth

/** «По месяцам» (board `RepA`): spending of each month on one scale, the period's last month highlighted. */
@Composable
internal fun MonthsCard(months: Months, onMonth: (YearMonth) -> Unit, modifier: Modifier = Modifier) {
    CoShellCard(modifier.fillMaxWidth()) {
        Row(Modifier.padding(start = Spacing.l, end = Spacing.l, top = Spacing.l), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.reports_months_title), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Text(stringResource(R.string.reports_months_caption), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        val columns = months.columns.map { column ->
            ChartColumn(
                label = formatMonthShort(column.month),
                valueLabel = column.value,
                fraction = column.fraction,
                description = stringResource(R.string.reports_months_description, formatMonthYear(column.month), shownAmount(column.value)),
            )
        }
        CoShellColumns(columns, Modifier.padding(Spacing.l), selected = months.selected, onSelect = { onMonth(months.columns[it].month) })
    }
}
