package app.coshell.recurring.notify

import android.content.Context
import app.coshell.alarm.DailyAlarm
import app.coshell.recurring.notify.data.PrefsReminderSettings
import app.coshell.core.domain.settings.ClockMinutes
import kotlinx.datetime.LocalTime
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit
import kotlin.time.Clock
import kotlin.time.Duration
import kotlinx.datetime.TimeZone

/**
 * When [RecurringPaymentsWorker] runs (#143): every day at 9:00 (a [DailyAlarm], the daily job as a safety net, #745) — posts «само» payments and
 * reminds of today's; and once at every app start — posts what came due while the phone was off,
 * without a reminder (the app is open, «Скоро» on «Обзоре» shows it). No network needed: the
 * operations go to Room, sync sends them later.
 */
object RecurringPaymentsScheduler {
    private const val DAILY = "recurring-daily"
    private const val ON_START = "recurring-start"

    /** [replace]: the time changed — the daily job starts again from the new time (otherwise the first schedule keeps its phase). */
    fun schedule(context: Context, clock: Clock = Clock.System, zone: TimeZone = TimeZone.currentSystemDefault(), replace: Boolean = false) {
        val work = WorkManager.getInstance(context)
        val until = setAlarm(context, clock, zone)
        val daily = PeriodicWorkRequestBuilder<RecurringPaymentsWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(until.inWholeMinutes, TimeUnit.MINUTES)
            .setInputData(workDataOf(RecurringPaymentsWorker.KEY_REMIND to true))
            .build()
        // KEEP: the first schedule fixes the phase; later app starts leave it alone. A new time starts it over.
        work.enqueueUniquePeriodicWork(DAILY, if (replace) ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE else ExistingPeriodicWorkPolicy.KEEP, daily)
        work.enqueueUniqueWork(ON_START, ExistingWorkPolicy.REPLACE, OneTimeWorkRequestBuilder<RecurringPaymentsWorker>().build())
    }

    /** The alarm came at the reminder time (#745): remind now and set tomorrow's alarm; the periodic job is only a safety net. */
    fun alarmFired(context: Context) {
        val remind = OneTimeWorkRequestBuilder<RecurringPaymentsWorker>().setInputData(workDataOf(RecurringPaymentsWorker.KEY_REMIND to true)).build()
        WorkManager.getInstance(context).enqueueUniqueWork(ON_TIME, ExistingWorkPolicy.REPLACE, remind)
        setAlarm(context)
    }

    private fun setAlarm(context: Context, clock: Clock = Clock.System, zone: TimeZone = TimeZone.currentSystemDefault()): Duration {
        val until = ReminderTime.untilNext(clock.now(), zone, PrefsReminderSettings.readTime(context).let { LocalTime(ClockMinutes.hour(it), ClockMinutes.minute(it)) })
        DailyAlarm.set(context, DailyAlarm.Job.PAYMENTS, until)
        return until
    }

    private const val ON_TIME = "recurring-on-time"
}
