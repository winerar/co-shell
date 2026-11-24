package app.coshell.backup.auto

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Turns the automatic copy on and off. WorkManager only wakes the worker every hour; whether a copy is due (period,
 * weekday or day of the month, hour) is decided by [AutoBackupPolicy], so a change of the schedule needs no re-planning.
 */
class AutoBackupScheduler @Inject constructor(@ApplicationContext private val context: Context) {
    private val work get() = WorkManager.getInstance(context)

    /** The hourly check (kept if already set) plus one copy right now. */
    fun enable() {
        ensureChecking()
        work.enqueueUniqueWork(NOW, ExistingWorkPolicy.REPLACE, OneTimeWorkRequestBuilder<AutoBackupWorker>().setInputData(forced()).build())
    }

    /** The hourly check only; at start for phones that had the weekly copy before #606 (their old 7-day job is dropped). */
    fun ensureChecking() {
        val constraints = Constraints.Builder().setRequiresBatteryNotLow(true).build()
        val periodic = PeriodicWorkRequestBuilder<AutoBackupWorker>(CHECK_HOURS, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.HOURS)
            .build()
        work.cancelUniqueWork(LEGACY_WEEKLY)
        work.enqueueUniquePeriodicWork(PERIODIC, ExistingPeriodicWorkPolicy.KEEP, periodic)
    }

    fun disable() {
        work.cancelUniqueWork(PERIODIC)
        work.cancelUniqueWork(LEGACY_WEEKLY)
    }

    private fun forced() = Data.Builder().putBoolean(AutoBackupWorker.FORCE, true).build()

    private companion object {
        const val PERIODIC = "auto-backup-check"
        const val LEGACY_WEEKLY = "auto-backup"
        const val NOW = "auto-backup-now"
        const val CHECK_HOURS = 1L
    }
}
