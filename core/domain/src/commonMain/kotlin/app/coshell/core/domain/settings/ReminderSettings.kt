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

    /** The hour of the reminder on the phone's clock («Уведомления → Время напоминания»); [DEFAULT_HOUR] by default. */
    val hour: Flow<Int>

    suspend fun setHour(hour: Int)

    /** «Предупреждать за»: 0 — only on the day, 1 or 3 — one more reminder that many days before. */
    val daysBefore: Flow<Int>

    suspend fun setDaysBefore(days: Int)

    /** «Лимиты» (#186): once a day at [hour] — 80 % of a limit or spending ahead of the month. On by default. */
    val limitsEnabled: Flow<Boolean>

    suspend fun setLimitsEnabled(enabled: Boolean)

    companion object {
        const val DEFAULT_HOUR = 9

        /** The hours «Время напоминания» offers. */
        val HOURS = listOf(8, 9, 12, 18)

        /** The values «Предупреждать за» offers. */
        val DAYS_BEFORE = listOf(0, 1, 3)
    }
}
