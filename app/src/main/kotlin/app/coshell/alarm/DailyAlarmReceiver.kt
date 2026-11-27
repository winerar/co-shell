package app.coshell.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.coshell.notices.NoticeScheduler
import app.coshell.recurring.notify.RecurringPaymentsScheduler

/**
 * [DailyAlarm] went off: run that job now and set tomorrow's alarm. Also after a reboot, an app update or a change of the
 * clock or time zone (alarms do not survive them): both alarms are set again from the saved times.
 */
class DailyAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.getStringExtra(DailyAlarm.EXTRA_JOB)?.let { name -> DailyAlarm.Job.entries.firstOrNull { it.name == name } }) {
            DailyAlarm.Job.NOTICES -> NoticeScheduler.alarmFired(context)
            DailyAlarm.Job.PAYMENTS -> RecurringPaymentsScheduler.alarmFired(context)
            null -> if (intent.action in RESCHEDULE) {
                NoticeScheduler.schedule(context)
                RecurringPaymentsScheduler.schedule(context)
            }
        }
    }

    private companion object {
        val RESCHEDULE = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
        )
    }
}
