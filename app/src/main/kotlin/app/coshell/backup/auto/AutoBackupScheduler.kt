package app.coshell.backup.auto

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/** Turns the weekly copy on and off. */
class AutoBackupScheduler @Inject constructor(@ApplicationContext private val context: Context) {
    private val work get() = WorkManager.getInstance(context)

    /** Weekly schedule (kept if already set) plus one copy right now. */
    fun enable() {
        val constraints = Constraints.Builder().setRequiresBatteryNotLow(true).build()
        val periodic = PeriodicWorkRequestBuilder<AutoBackupWorker>(AutoBackupPolicy.PERIOD_DAYS, TimeUnit.DAYS)
            .setConstraints(constraints)
            .setInitialDelay(AutoBackupPolicy.PERIOD_DAYS, TimeUnit.DAYS)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.HOURS)
            .build()
        work.enqueueUniquePeriodicWork(PERIODIC, ExistingPeriodicWorkPolicy.KEEP, periodic)
        work.enqueueUniqueWork(NOW, ExistingWorkPolicy.REPLACE, OneTimeWorkRequestBuilder<AutoBackupWorker>().build())
    }

    fun disable() {
        work.cancelUniqueWork(PERIODIC)
    }

    private companion object {
        const val PERIODIC = "auto-backup"
        const val NOW = "auto-backup-now"
    }
}
