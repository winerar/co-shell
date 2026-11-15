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
}
