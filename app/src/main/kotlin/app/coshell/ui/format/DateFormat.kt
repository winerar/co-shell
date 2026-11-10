package app.coshell.ui.format

import java.time.format.DateTimeFormatter
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime

/** "8 ноября" in the phone's locale: rate dates in hints ("по курсу на …"). */
fun formatDayMonth(date: LocalDate): String = date.toJavaLocalDate().format(DateTimeFormatter.ofPattern("d MMMM"))

/** "ноябрь" (stand-alone month name) in the phone's locale: "Траты за ноябрь". */
fun formatMonth(firstDay: LocalDate): String = firstDay.toJavaLocalDate().format(DateTimeFormatter.ofPattern("LLLL"))

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
