package app.coshell.recurring.notify

import android.content.Context
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
 * without a reminder (the app is open, «Скоро» on «Обзоре» shows it). No network needed: the
 * operations go to Room, sync sends them later.
 */
object RecurringPaymentsScheduler {
    private const val DAILY = "recurring-daily"
    private const val ON_START = "recurring-start"

    fun schedule(context: Context, clock: Clock = Clock.System, zone: TimeZone = TimeZone.currentSystemDefault()) {
        val work = WorkManager.getInstance(context)
        val daily = PeriodicWorkRequestBuilder<RecurringPaymentsWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(ReminderTime.untilNext(clock.now(), zone).inWholeMinutes, TimeUnit.MINUTES)
            .setInputData(workDataOf(RecurringPaymentsWorker.KEY_REMIND to true))
            .build()
        // KEEP: the first schedule fixes the 9:00 phase; later app starts leave it alone.
        work.enqueueUniquePeriodicWork(DAILY, ExistingPeriodicWorkPolicy.KEEP, daily)
        work.enqueueUniqueWork(ON_START, ExistingWorkPolicy.REPLACE, OneTimeWorkRequestBuilder<RecurringPaymentsWorker>().build())
    }
}
