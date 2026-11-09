package app.coshell.ui.format

import java.time.format.DateTimeFormatter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

/** "8 ноября" in the phone's locale: rate dates in hints ("по курсу на …"). */
fun formatDayMonth(date: LocalDate): String = date.toJavaLocalDate().format(DateTimeFormatter.ofPattern("d MMMM"))

/** "ноябрь" (stand-alone month name) in the phone's locale: "Траты за ноябрь". */
fun formatMonth(firstDay: LocalDate): String = firstDay.toJavaLocalDate().format(DateTimeFormatter.ofPattern("LLLL"))
