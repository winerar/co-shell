package app.coshell.templates.edit.components

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import app.coshell.core.model.PostMode
import app.coshell.core.model.Recurrence
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/** Segments of the «Повтор» sheet, in their order. */
internal enum class RepeatKind { NONE, MONTH, WEEK, YEAR }

/**
 * What the «Повтор» sheet shows before «Готово»: every frequency keeps its own day, so switching «Месяц» → «Неделя» → «Месяц»
 * does not lose the 5th. [yearDay] past the end of [yearMonth] is clamped when the rule is built.
 */
internal data class RepeatDraft(
    val kind: RepeatKind,
    val monthDay: Int,
    val weekday: DayOfWeek,
    val yearMonth: Int,
    val yearDay: Int,
    val mode: PostMode,
) {
    val recurrence: Recurrence?
        get() = when (kind) {
            RepeatKind.NONE -> null
            RepeatKind.MONTH -> Recurrence.Monthly(monthDay)
            RepeatKind.WEEK -> Recurrence.Weekly(weekday)
            RepeatKind.YEAR -> Recurrence.Yearly(yearMonth, minOf(yearDay, daysInMonth(yearMonth)))
        }

    companion object {
        /** From the template's rule; the other frequencies start from [today]. */
        fun from(recurrence: Recurrence?, mode: PostMode, today: LocalDate): RepeatDraft {
            val base = RepeatDraft(RepeatKind.NONE, today.day, today.dayOfWeek, today.month.ordinal + 1, today.day, mode)
            return when (recurrence) {
                null -> base
                is Recurrence.Monthly -> base.copy(kind = RepeatKind.MONTH, monthDay = recurrence.day)
                is Recurrence.Weekly -> base.copy(kind = RepeatKind.WEEK, weekday = recurrence.dayOfWeek)
                is Recurrence.Yearly -> base.copy(kind = RepeatKind.YEAR, yearMonth = recurrence.month, yearDay = recurrence.day)
            }
        }

        /** 29 for February: a yearly payment on 29 February falls on the 28th in a common year. */
        fun daysInMonth(month: Int): Int = MONTH_MAX[month - 1]

        private val MONTH_MAX = intArrayOf(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

        val Saver: Saver<RepeatDraft, Any> = listSaver(
            save = { listOf(it.kind.ordinal, it.monthDay, it.weekday.ordinal, it.yearMonth, it.yearDay, it.mode.ordinal) },
            restore = {
                RepeatDraft(RepeatKind.entries[it[0]], it[1], DayOfWeek.entries[it[2]], it[3], it[4], PostMode.entries[it[5]])
            },
        )
    }
}
