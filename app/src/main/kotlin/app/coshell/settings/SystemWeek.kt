package app.coshell.settings

import java.time.temporal.WeekFields
import java.util.Locale
import kotlinx.datetime.DayOfWeek

/** The first day of a week in the phone's locale («Как в системе»). */
fun systemFirstDay(locale: Locale = Locale.getDefault()): DayOfWeek =
    DayOfWeek.entries[WeekFields.of(locale).firstDayOfWeek.value - 1]
