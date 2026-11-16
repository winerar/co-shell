package app.coshell.transactions.list.components

import app.coshell.core.designsystem.components.CoShellDayHeader
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.transactions.list.TransactionsUiState.Day
import app.coshell.transactions.list.TransactionsUiState.Kind
import app.coshell.transactions.list.TransactionsUiState.RelativeDay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.datetime.toJavaLocalDate

/**
 * Day title in accent capitals: "СЕГОДНЯ", "ВЧЕРА" or a short date, with the day's net on the right.
 * The title keeps its width; a long net of several currencies wraps after a «·» (aligned to the end), never the title.
 * [background] — the list's surface; inside a card that draws its own fill — transparent.
 */
@Composable
internal fun DayHeader(day: Day, background: Color = MaterialTheme.colorScheme.surface) {
    CoShellDayHeader(
        title = dayLabel(day),
        total = day.total,
        totalColor = when (day.totalTone) {
            Kind.EXPENSE -> LocalAmountPalette.current.expense
            Kind.INCOME -> LocalAmountPalette.current.income
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        background = background,
    )
}

/** «Сегодня», «Вчера» or «10 ноября, пн» — the day's name in [DayHeader] and in the tab header's second line. */
@Composable
internal fun dayLabel(day: Day): String = when (day.relative) {
    RelativeDay.TODAY -> stringResource(R.string.transaction_date_today)
    RelativeDay.YESTERDAY -> stringResource(R.string.transaction_date_yesterday)
    RelativeDay.OTHER -> dayTitle(day.date.toJavaLocalDate(), LocalDate.now().year, LocalConfiguration.current.locales[0])
}

/** "10 ноября, пн"; the year is added only when it is not [currentYear]. */
internal fun dayTitle(date: LocalDate, currentYear: Int, locale: Locale): String {
    val pattern = if (date.year == currentYear) "d MMMM, EE" else "d MMMM yyyy, EE"
    return date.format(DateTimeFormatter.ofPattern(pattern, locale))
}
