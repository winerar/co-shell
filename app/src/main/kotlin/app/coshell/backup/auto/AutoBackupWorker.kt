package app.coshell.backup.auto

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.coshell.core.domain.BackupRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Weekly copy of the whole database into the chosen folder ([AutoBackupScheduler]). */
@HiltWorker
class AutoBackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val backup: BackupRepository,
    private val writer: FolderBackupWriter,
    private val settings: AutoBackupSettings,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val folder = settings.config.value.folderUri ?: return Result.success()
        val now = clock.now()
        return try {
            writer.write(folder, AutoBackupPolicy.fileName(now.toLocalDateTime(timeZone).date), backup.exportJson())
            settings.recordResult(now.toEpochMilliseconds(), failed = false)
            Result.success()
        } catch (e: Exception) {
            Log.w("AutoBackup", "Automatic backup failed: ${e.message}")
            settings.recordResult(now.toEpochMilliseconds(), failed = true)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
