package app.coshell.core.data.repository

import app.coshell.core.model.PostMode
import app.coshell.core.model.Recurrence
import app.coshell.core.model.RecurringSchedule
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber

/**
 * «Повтор» of a template (#143) as one text column `templates.schedule` (v13), the same in sync and backup:
 * `<recurrence>;<start>;<mode>;<handledThrough or empty>` — recurrence `M31` (month day), `W5` (ISO weekday,
 * Monday = 1), `Y2-29` (month-day). Text, not columns: a newer app may add kinds without a migration;
 * a value this version cannot read is treated as «no repeat» rather than failing the whole list.
 */
internal object ScheduleCodec {
    fun encode(s: RecurringSchedule): String {
        val r = when (val x = s.recurrence) {
            is Recurrence.Monthly -> "M${x.day}"
            is Recurrence.Weekly -> "W${x.dayOfWeek.isoDayNumber}"
            is Recurrence.Yearly -> "Y${x.month}-${x.day}"
        }
        return listOf(r, s.start.toString(), s.mode.name, s.handledThrough?.toString().orEmpty()).joinToString(";")
    }

    fun decode(text: String?): RecurringSchedule? = text?.let {
        runCatching {
            val (r, start, mode, handled) = it.split(";")
            val body = r.drop(1)
            val recurrence = when (r.first()) {
                'M' -> Recurrence.Monthly(body.toInt())
                'W' -> Recurrence.Weekly(DayOfWeek(body.toInt()))
                'Y' -> body.split("-").let { (m, d) -> Recurrence.Yearly(m.toInt(), d.toInt()) }
                else -> error("unknown recurrence $r")
            }
            RecurringSchedule(recurrence, LocalDate.parse(start), PostMode.valueOf(mode), handled.takeIf(String::isNotEmpty)?.let(LocalDate::parse))
        }.getOrNull()
    }
}
