package app.coshell.transactions.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.coshell.R
import app.coshell.transactions.list.TransactionsUiState.RelativeDay
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

/** Sticky day title: "Сегодня", "Вчера" or the full date in the phone's locale. */
@Composable
internal fun DayHeader(date: LocalDate, relative: RelativeDay) {
    val text = when (relative) {
        RelativeDay.TODAY -> stringResource(R.string.transaction_date_today)
        RelativeDay.YESTERDAY -> stringResource(R.string.transaction_date_yesterday)
        RelativeDay.OTHER -> date.toJavaLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))
    }
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
