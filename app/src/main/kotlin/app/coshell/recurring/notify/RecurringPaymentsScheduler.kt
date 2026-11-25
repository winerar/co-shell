package app.coshell.recurring.notify

import android.content.Context
import app.coshell.budgets.notify.LimitAlertsWorker
import app.coshell.recurring.notify.data.PrefsReminderSettings
import kotlinx.datetime.LocalTime
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit
import kotlin.time.Clock
import kotlinx.datetime.TimeZone

/**
 * When [RecurringPaymentsWorker] runs (#143): every day at about 9:00 — posts «само» payments and
 * reminds of today's; and once at every app start — posts what came due while the phone was off,
 * without a reminder (the app is open, «Скоро» on «Обзоре» shows it). At the same hour — the limit check [LimitAlertsWorker] (#186). No network needed: the
 * operations go to Room, sync sends them later.
 */
object RecurringPaymentsScheduler {
    private const val DAILY = "recurring-daily"
    private const val ON_START = "recurring-start"
    private const val LIMITS = "limits-daily"

    /** [replace]: the hour changed — the daily job starts again from the new time (otherwise the first schedule keeps its phase). */
    fun schedule(context: Context, clock: Clock = Clock.System, zone: TimeZone = TimeZone.currentSystemDefault(), replace: Boolean = false) {
        val work = WorkManager.getInstance(context)
        val daily = PeriodicWorkRequestBuilder<RecurringPaymentsWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(ReminderTime.untilNext(clock.now(), zone, LocalTime(PrefsReminderSettings.readHour(context), 0)).inWholeMinutes, TimeUnit.MINUTES)
            .setInputData(workDataOf(RecurringPaymentsWorker.KEY_REMIND to true))
            .build()
        // KEEP: the first schedule fixes the phase; later app starts leave it alone. A new hour starts it over.
        work.enqueueUniquePeriodicWork(DAILY, if (replace) ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE else ExistingPeriodicWorkPolicy.KEEP, daily)
        // The limit check (#186) at the same hour: «Время напоминания» is one for all notifications.
        val limits = PeriodicWorkRequestBuilder<LimitAlertsWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(ReminderTime.untilNext(clock.now(), zone, LocalTime(PrefsReminderSettings.readHour(context), 0)).inWholeMinutes, TimeUnit.MINUTES)
            .build()
        work.enqueueUniquePeriodicWork(LIMITS, if (replace) ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE else ExistingPeriodicWorkPolicy.KEEP, limits)
        work.enqueueUniqueWork(ON_START, ExistingWorkPolicy.REPLACE, OneTimeWorkRequestBuilder<RecurringPaymentsWorker>().build())
    }
}
