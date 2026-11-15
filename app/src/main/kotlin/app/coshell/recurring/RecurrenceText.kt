package app.coshell.recurring

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.model.Recurrence
import app.coshell.ui.format.formatDayMonth
import app.coshell.ui.format.formatWeekdayShort
import kotlinx.datetime.LocalDate

/** «Каждый месяц, 5-го», «Каждую неделю, пт», «Каждый год, 5 декабря»; `null` — «Нет». */
@Composable
fun recurrenceText(recurrence: Recurrence?): String = when (recurrence) {
    null -> stringResource(R.string.recurring_off)
    is Recurrence.Monthly -> stringResource(R.string.recurring_monthly_value, recurrence.day)
    is Recurrence.Weekly -> stringResource(R.string.recurring_weekly_value, formatWeekdayShort(recurrence.dayOfWeek))
    // Any leap year: 29 February must be formattable.
    is Recurrence.Yearly -> stringResource(R.string.recurring_yearly_value, formatDayMonth(LocalDate(LEAP_YEAR, recurrence.month, recurrence.day)))
}

private const val LEAP_YEAR = 2028
