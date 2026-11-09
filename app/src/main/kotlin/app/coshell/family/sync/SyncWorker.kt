package app.coshell.family.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.coshell.core.data.family.FamilyService
import app.coshell.core.data.remote.ApiException
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * One sync run in the background: after local changes ([SyncScheduler.syncSoon]) and
 * periodically to receive the other phone's changes. Not in a family — does nothing.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val family: FamilyService,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        family.load()
        return try {
            val result = family.sync()
            Log.i(TAG, "Sync: $result")
            Result.success()
        } catch (e: ApiException) {
            Log.w(TAG, "Sync failed: ${e.message}")
            if (e.isPermissionDenied || runAttemptCount >= MAX_RETRIES) Result.failure() else Result.retry()
        } catch (e: Exception) {
            Log.w(TAG, "Sync failed: ${e.message}")
            if (runAttemptCount >= MAX_RETRIES) Result.failure() else Result.retry()
        }
    }

    private companion object {
        const val TAG = "Sync"
        const val MAX_RETRIES = 5
    }
}
