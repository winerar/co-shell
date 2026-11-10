package app.coshell.importer.data

import android.content.Context
import app.coshell.backup.auto.AutoBackupSettings
import app.coshell.backup.auto.FolderBackupWriter
import app.coshell.core.domain.BackupRepository
import app.coshell.importer.PreImportBackup
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Writes the copy into the app's private `files/before-import/` (newest [KEEP] stay) and, when
 * automatic copies are on, also into that folder as `co-shell-before-import-<date>.json` — so
 * «Резервная копия → Восстановить» can pick it. The folder copy is best effort; the private one must succeed.
 */
class FilesPreImportBackup @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backup: BackupRepository,
    private val autoSettings: AutoBackupSettings,
    private val folder: FolderBackupWriter,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : PreImportBackup {

    override suspend fun save(): Boolean {
        val json = backup.exportJson()
        val now = clock.now()
        val saved = withContext(Dispatchers.IO) {
            try {
                val dir = File(context.filesDir, DIR).apply { mkdirs() }
                File(dir, "before-import-${now.toEpochMilliseconds()}.json").writeText(json)
                dir.listFiles().orEmpty().sortedByDescending { it.name }.drop(KEEP).forEach { it.delete() }
                true
            } catch (e: IOException) {
                false
            }
        }
        autoSettings.config.value.folderUri?.let { tree ->
            try {
                folder.write(tree, "co-shell-before-import-${now.toLocalDateTime(timeZone).date}.json", json)
            } catch (e: IOException) {
                // The folder is a bonus; the private copy above is what the import relies on.
            }
        }
        return saved
    }

    private companion object {
        const val DIR = "before-import"
        const val KEEP = 3
    }
}
