package app.coshell.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import kotlin.time.Duration

/**
 * A system alarm at the reminder time (#745): the evening notices and the 9:00 payment reminder. A daily WorkManager job drifts
 * by hours in Doze, so the alarm wakes the app on time and [DailyAlarmReceiver] starts the job at once; the periodic job stays as
 * a safety net (the planners say nothing twice a day). `setAndAllowWhileIdle` needs no exact-alarm permission: the system may
 * be late by minutes, never early. A phone that forbids the app to start by itself (Xiaomi «Автозапуск») drops it — `BackgroundCheck`.
 */
object DailyAlarm {
    enum class Job { NOTICES, PAYMENTS }

    const val EXTRA_JOB = "daily_alarm_job"

    /** Sets (or moves) the alarm of [job] to [delay] from now. */
    fun set(context: Context, job: Job, delay: Duration) {
        val alarms = context.getSystemService(AlarmManager::class.java) ?: return
        val at = System.currentTimeMillis() + delay.inWholeMilliseconds
        alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending(context, job))
    }

    private fun pending(context: Context, job: Job): PendingIntent = PendingIntent.getBroadcast(
        context, REQUEST_BASE + job.ordinal,
        Intent(context, DailyAlarmReceiver::class.java).putExtra(EXTRA_JOB, job.name),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private const val REQUEST_BASE = 7460
}
