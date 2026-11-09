package app.coshell.rates.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Schedules [RatesRefreshWorker] once a day, only with network. The first run starts right after
 * scheduling; [ExistingPeriodicWorkPolicy.KEEP] leaves an existing schedule untouched, so calling
 * this on every app start is cheap.
 */
object RatesRefreshScheduler {
    private const val WORK_NAME = "rates-refresh"

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<RatesRefreshWorker>(1, TimeUnit.DAYS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }
}
