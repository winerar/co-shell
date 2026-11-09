package app.coshell.rates.work

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import app.coshell.rates.RatesRefresher
import app.coshell.rates.RefreshStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Runs [RatesRefreshWorker] once, without a network constraint: offline it fails at once
 * instead of waiting, and the screen says so.
 */
class WorkManagerRatesRefresher @Inject constructor(
    @ApplicationContext private val context: Context,
) : RatesRefresher {

    private val workManager get() = WorkManager.getInstance(context)

    override fun refreshNow() {
        val request = OneTimeWorkRequestBuilder<RatesRefreshWorker>()
            .setInputData(workDataOf(RatesRefreshWorker.KEY_ONE_OFF to true))
            .build()
        workManager.enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    override fun observe(): Flow<RefreshStatus> =
        workManager.getWorkInfosForUniqueWorkFlow(WORK_NAME).map { infos -> infos.firstOrNull().toStatus() }

    private fun WorkInfo?.toStatus(): RefreshStatus = when (this?.state) {
        null -> RefreshStatus.Idle
        WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING, WorkInfo.State.BLOCKED -> RefreshStatus.Running
        WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> RefreshStatus.Failed
        WorkInfo.State.SUCCEEDED -> when {
            outputData.getBoolean(RatesRefreshWorker.KEY_FAILED, false) -> RefreshStatus.Failed
            outputData.getBoolean(RatesRefreshWorker.KEY_UPDATED, false) -> RefreshStatus.Updated
            else -> RefreshStatus.AlreadyFresh
        }
    }

    private companion object {
        const val WORK_NAME = "rates-refresh-now"
    }
}
