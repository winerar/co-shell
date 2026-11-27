package app.coshell.core.domain.settings

import kotlinx.coroutines.flow.Flow

/**
 * «Напоминания о платежах» in «Внешний вид» (#405): the 9:00 «Сегодня платёж: …». Off — no reminders;
 * «Проводить само» still posts and says «Проведено». Kept on the phone only: each one decides for itself.
 */
interface ReminderSettings {
    /** On by default. */
    val enabled: Flow<Boolean>

    suspend fun setEnabled(enabled: Boolean)

    /** The time of the reminder on the phone's clock, minutes since midnight («Уведомления → Время напоминания»); [DEFAULT_TIME] by default. */
    val time: Flow<Int>

    suspend fun setTime(minutes: Int)

    /** «Предупреждать»: 0 — only on the day, 1…[MAX_DAYS_BEFORE] — one more reminder that many days before. */
    val daysBefore: Flow<Int>

    suspend fun setDaysBefore(days: Int)

    companion object {
        val DEFAULT_TIME = ClockMinutes.of(9)

        /** The times «Время напоминания» offers as buttons; any other — «Другое…» (#777). */
        val TIMES = listOf(8, 9, 12, 18).map { ClockMinutes.of(it) }

        /** The values «Предупреждать» offers as buttons; any other up to [MAX_DAYS_BEFORE] — «Другое…» (#777). */
        val DAYS_BEFORE = listOf(0, 1, 3)

        const val MAX_DAYS_BEFORE = 30

        /** [steps] counter presses away from [days], kept in 0…[MAX_DAYS_BEFORE]. */
        fun stepDays(days: Int, steps: Int): Int = (days + steps).coerceIn(0, MAX_DAYS_BEFORE)
    }
}
