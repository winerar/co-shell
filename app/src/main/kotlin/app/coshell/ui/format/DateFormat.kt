package app.coshell.ui.format

import java.time.format.DateTimeFormatter
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime

/** "8 ноября" in the phone's locale: rate dates in hints ("по курсу на …"). */
fun formatDayMonth(date: LocalDate): String = date.toJavaLocalDate().format(DateTimeFormatter.ofPattern("d MMMM"))

/** "ноябрь" (stand-alone month name) in the phone's locale: "Траты за ноябрь". */
fun formatMonth(firstDay: LocalDate): String = firstDay.toJavaLocalDate().format(DateTimeFormatter.ofPattern("LLLL"))

/** "Ноябрь 2026" (stand-alone, capitalised) in the phone's locale: the month of «Бюджеты». */
fun formatMonthYear(month: YearMonth): String =
    month.firstDay.toJavaLocalDate().format(DateTimeFormatter.ofPattern("LLLL yyyy")).replaceFirstChar { it.titlecase() }

/** "ноября 2026" (genitive in Russian) in the phone's locale: «с ноября 2026». */
fun formatMonthYearFrom(month: YearMonth): String =
    month.firstDay.toJavaLocalDate().format(DateTimeFormatter.ofPattern("MMMM yyyy"))

/** "8 нояб. 2026" in the phone's locale: periods of imports. */
fun formatShortDate(date: LocalDate): String = date.toJavaLocalDate().format(DateTimeFormatter.ofPattern("d MMM yyyy"))

/** "12:03" today, "9 ноября 12:03" on another day: last sync times. */
fun formatDayTime(moment: Instant, now: Instant, zone: TimeZone): String {
    val time = moment.toLocalDateTime(zone)
    val hm = formatClock(time)
    return if (time.date == now.toLocalDateTime(zone).date) hm else "${formatDayMonth(time.date)} $hm"
}

/** "4:00" — hours without a leading zero, as the app writes times. */
fun formatClock(time: LocalDateTime): String = "%d:%02d".format(time.hour, time.minute)

/** "июн" — three letters of the stand-alone month name in the phone's locale: columns of «По месяцам», month cells. */
fun formatMonthShort(month: YearMonth): String =
    month.firstDay.toJavaLocalDate().format(DateTimeFormatter.ofPattern("LLLL")).take(3)

/** "октября" (genitive in Russian, "October" in English) in the phone's locale: «Итог октября», «Все лимиты октября». */
fun formatMonthOf(month: YearMonth): String = month.firstDay.toJavaLocalDate().format(DateTimeFormatter.ofPattern("MMMM"))

/** "пт" — the short stand-alone weekday in the phone's locale: «Повтор» каждую неделю. */
fun formatWeekdayShort(day: kotlinx.datetime.DayOfWeek): String =
    java.time.DayOfWeek.of(day.ordinal + 1).getDisplayName(java.time.format.TextStyle.SHORT_STANDALONE, java.util.Locale.getDefault())

/** "пт, 5 декабря" in the phone's locale: the next regular payment. */
fun formatWeekdayDayMonth(date: LocalDate): String = date.toJavaLocalDate().format(DateTimeFormatter.ofPattern("EEE, d MMMM"))
