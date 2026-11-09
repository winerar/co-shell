package app.coshell.rates.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import app.coshell.core.domain.rates.RefreshRatesUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Rates download in the background: daily ([RatesRefreshScheduler]) or once on "Update now"
 * ([WorkManagerRatesRefresher], with [KEY_ONE_OFF]). Works without the app open.
 * The one-off run never retries, so the screen gets its answer at once.
 */
@HiltWorker
class RatesRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val refresh: RefreshRatesUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val result = refresh()
        Log.i(TAG, "Rates refresh: $result")
        val oneOff = inputData.getBoolean(KEY_ONE_OFF, false)
        if (result.latestFailed && !oneOff && runAttemptCount < MAX_RETRIES) return Result.retry()
        return Result.success(
            workDataOf(KEY_FAILED to result.latestFailed, KEY_UPDATED to (result.latestDate != null)),
        )
    }

    companion object {
        const val KEY_ONE_OFF = "one_off"
        const val KEY_FAILED = "latest_failed"
        const val KEY_UPDATED = "latest_updated"
        private const val TAG = "RatesRefresh"
        private const val MAX_RETRIES = 3
    }
}
