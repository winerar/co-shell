package app.coshell.reports.components

import app.coshell.ui.motion.Swap
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.list.TransactionsUiState.Day
import app.coshell.transactions.list.components.DayHeader
import app.coshell.transactions.list.components.TransactionRow

/**
 * Operations of the day tapped in «Календарь трат» (#553): the same day header and rows as in «Операции»; a tap on a row opens it.
 * [day] is `null` while nothing happened that day (the calendar still knows the day).
 */
@Composable
internal fun DayOperationsCard(day: Day?, onTransaction: (String) -> Unit, modifier: Modifier = Modifier) {
    // Another day tapped: its operations rise in where the last day's were (owner 2026-11-27).
    CoShellCard(modifier.fillMaxWidth()) { Swap(day, Modifier.fillMaxWidth()) { shown ->
        if (shown == null) {
            Text(
                stringResource(R.string.reports_calendar_day_empty),
                Modifier.padding(Spacing.l),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return@Swap
        }
        Column {
            DayHeader(shown, background = Color.Transparent)
            shown.rows.forEach { row -> TransactionRow(row, onClick = onTransaction, background = Color.Transparent) }
        }
    } }
}
