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

    companion object {
        const val DEFAULT_HOUR = 9

        /** The hours «Время напоминания» offers. */
        val HOURS = listOf(8, 9, 12, 18)
    }
}
