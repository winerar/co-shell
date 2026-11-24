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
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Copy of the whole database into the chosen folder, when the schedule says it is due or when [FORCE]d ([AutoBackupScheduler]). */
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
        val config = settings.config.value
        val folder = config.folderUri ?: return Result.success()
        val now = clock.now()
        val local = now.toLocalDateTime(timeZone)
        val last = config.lastBackupAtMs?.let { Instant.fromEpochMilliseconds(it).toLocalDateTime(timeZone) }
        if (!inputData.getBoolean(FORCE, false) && !AutoBackupPolicy.isDue(config.schedule, last, local)) return Result.success()
        return try {
            writer.write(folder, AutoBackupPolicy.fileName(local.date), backup.exportJson(), config.schedule.keep)
            settings.recordResult(now.toEpochMilliseconds(), failed = false)
            Result.success()
        } catch (e: Exception) {
            Log.w("AutoBackup", "Automatic backup failed: ${e.message}")
            settings.recordResult(now.toEpochMilliseconds(), failed = true)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        /** Input flag: copy now, whatever the schedule says (the first copy after choosing a folder). */
        const val FORCE = "force"
    }
}
