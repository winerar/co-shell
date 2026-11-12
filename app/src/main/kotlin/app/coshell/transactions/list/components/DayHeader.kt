package app.coshell.transactions.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.core.designsystem.theme.RateTextStyle
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.transactions.list.TransactionsUiState.Day
import app.coshell.transactions.list.TransactionsUiState.Kind
import app.coshell.transactions.list.TransactionsUiState.RelativeDay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.datetime.toJavaLocalDate

/** Sticky day title in accent capitals: "СЕГОДНЯ", "ВЧЕРА" or a short date, with the day's net on the right. */
@Composable
internal fun DayHeader(day: Day) {
    val text = when (day.relative) {
        RelativeDay.TODAY -> stringResource(R.string.transaction_date_today)
        RelativeDay.YESTERDAY -> stringResource(R.string.transaction_date_yesterday)
        RelativeDay.OTHER -> dayTitle(day.date.toJavaLocalDate(), LocalDate.now().year, LocalConfiguration.current.locales[0])
    }.uppercase()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = Spacing.l, vertical = Spacing.s),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = accentInk(),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        day.total?.let { total ->
            Text(
                total,
                style = RateTextStyle,
                color = when (day.totalTone) {
                    Kind.EXPENSE -> LocalAmountPalette.current.expense
                    Kind.INCOME -> LocalAmountPalette.current.income
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
            )
        }
    }
}

/** "10 ноября, пн"; the year is added only when it is not [currentYear]. */
internal fun dayTitle(date: LocalDate, currentYear: Int, locale: Locale): String {
    val pattern = if (date.year == currentYear) "d MMMM, EE" else "d MMMM yyyy, EE"
    return date.format(DateTimeFormatter.ofPattern(pattern, locale))
}
