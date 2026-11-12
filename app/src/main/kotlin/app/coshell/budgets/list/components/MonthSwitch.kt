package app.coshell.budgets.list.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import app.coshell.R
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.format.formatDayMonth
import app.coshell.ui.format.formatMonthYear
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

/** «‹ Ноябрь 2026 ›»; under it the days of the period when the month starts on another day than the 1st. */
@Composable
internal fun MonthSwitch(
    month: YearMonth,
    periodDays: Pair<LocalDate, LocalDate>?,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        CoShellIconButton(onClick = onPrevious) {
            Icon(painterResource(DesignR.drawable.ic_chevron_right), stringResource(R.string.budgets_previous_month), Modifier.rotate(180f))
        }
        Column(Modifier.weight(1f).padding(horizontal = Spacing.s), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(formatMonthYear(month), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            periodDays?.let { (first, last) ->
                Text(
                    stringResource(R.string.budgets_period_days, formatDayMonth(first), formatDayMonth(last)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        CoShellIconButton(onClick = onNext) {
            Icon(painterResource(DesignR.drawable.ic_chevron_right), stringResource(R.string.budgets_next_month))
        }
    }
}
