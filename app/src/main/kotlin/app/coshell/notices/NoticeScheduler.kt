package app.coshell.notices

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import app.coshell.alarm.DailyAlarm
import app.coshell.notices.data.PrefsNoticeSettings
import app.coshell.recurring.notify.ReminderTime
import java.util.concurrent.TimeUnit
import kotlin.time.Clock
import app.coshell.core.domain.settings.ClockMinutes
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone

/**
 * When [NoticesWorker] runs (#745): every day at the chosen evening time (21:00 by default) — by a [DailyAlarm], with a daily
 * job as a safety net. No network needed. [replace] — the hour
 * changed: the daily job starts again from the new time (otherwise the first schedule keeps its phase), like `RecurringPaymentsScheduler`.
 */
object NoticeScheduler {
    private const val DAILY = "notices-daily"

    fun schedule(context: Context, clock: Clock = Clock.System, zone: TimeZone = TimeZone.currentSystemDefault(), replace: Boolean = false) {
        val until = ReminderTime.untilNext(clock.now(), zone, PrefsNoticeSettings.readTime(context).let { LocalTime(ClockMinutes.hour(it), ClockMinutes.minute(it)) })
        DailyAlarm.set(context, DailyAlarm.Job.NOTICES, until)
        val delay = until.inWholeMinutes
        val daily = PeriodicWorkRequestBuilder<NoticesWorker>(1, TimeUnit.DAYS).setInitialDelay(delay, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY, if (replace) ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE else ExistingPeriodicWorkPolicy.KEEP, daily,
        )
    }

    /** The alarm came at the chosen time: check now and set tomorrow's alarm. */
    fun alarmFired(context: Context) {
        WorkManager.getInstance(context).enqueueUniqueWork(ON_TIME, ExistingWorkPolicy.REPLACE, OneTimeWorkRequestBuilder<NoticesWorker>().build())
        schedule(context)
    }

    private const val ON_TIME = "notices-on-time"
}
