package app.coshell.core.domain.recurring

import app.coshell.core.model.PostMode
import app.coshell.core.model.Recurrence
import app.coshell.core.model.RecurringSchedule
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * The «Повтор» after «Готово» in the template editor's sheet (#143). `null` [recurrence] — «Нет»: an ordinary template.
 *
 * - a new rule starts [today]: a payment due today shows at once;
 * - the same rule with another [mode] keeps its dates;
 * - another day or frequency counts from the day after the last handled payment, so nothing posted is due again
 *   on the old day and nothing owed is lost: moving «10-го» to «5-го» after paying October shows November's 5th as missed.
 */
fun RecurringSchedule?.edited(recurrence: Recurrence?, mode: PostMode, today: LocalDate): RecurringSchedule? = when {
    recurrence == null -> null
    this == null -> RecurringSchedule(recurrence, start = today, mode = mode)
    this.recurrence == recurrence -> copy(mode = mode)
    else -> RecurringSchedule(
        recurrence = recurrence,
        start = handledThrough?.plus(DatePeriod(days = 1)) ?: today,
        mode = mode,
        handledThrough = handledThrough,
    )
}
