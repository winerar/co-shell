package app.coshell.notices

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.coshell.core.domain.notices.NoticePlanner
import app.coshell.core.domain.notices.NoticeSettings
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * The daily check of the optional notifications (#745, [NoticeScheduler]): collects the facts, asks [NoticePlanner] what (if anything)
 * goes out today — at most one — posts it and writes it to [NoticeLog]. Nothing on, or no permission — nothing happens.
 */
@HiltWorker
class NoticesWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val settings: NoticeSettings,
    private val log: NoticeLog,
    private val source: NoticeFactsSource,
    private val posts: NoticePosts,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val enabled = settings.enabled.first()
        if (enabled.isEmpty()) return Result.success()
        val facts = source.collect()
        val planned = NoticePlanner.next(facts, enabled, log.history(), at = settings.time.first()) ?: return Result.success()
        // Without the permission nothing is told and nothing is written: the same case waits for the next day.
        if (posts.show(planned.kind, NoticeTexts.of(applicationContext, planned, facts))) {
            log.record(planned.kind, planned.key, facts.today)
            Log.i(TAG, "Notice ${planned.kind.key}")
        }
        return Result.success()
    }

    private companion object {
        const val TAG = "Notices"
    }
}
