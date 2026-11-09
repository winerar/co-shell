package app.coshell.rates.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.coshell.core.domain.rates.RefreshRatesUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Daily rates download in the background ([RatesRefreshScheduler]); works without the app open. */
@HiltWorker
class RatesRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val refresh: RefreshRatesUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val result = refresh()
        Log.i(TAG, "Rates refresh: $result")
        return if (result.latestFailed && runAttemptCount < MAX_RETRIES) Result.retry() else Result.success()
    }

    private companion object {
        const val TAG = "RatesRefresh"
        const val MAX_RETRIES = 3
    }
}
