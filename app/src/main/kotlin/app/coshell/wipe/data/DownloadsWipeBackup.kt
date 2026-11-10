package app.coshell.wipe.data

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import app.coshell.core.domain.BackupRepository
import app.coshell.wipe.WipeBackup
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
 * Writes `co-shell-before-wipe-<date>-<hhmm>.json` into «Загрузки» (MediaStore, Android 10+, no
 * permission needed); on older phones — into the app's own Downloads folder.
 */
class DownloadsWipeBackup @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backup: BackupRepository,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : WipeBackup {

    override suspend fun save(): String? {
        val json = backup.exportJson()
        val time = clock.now().toLocalDateTime(timeZone)
        val name = "co-shell-before-wipe-${time.date}-%02d%02d.json".format(time.hour, time.minute)
        return withContext(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) writeToDownloads(name, json) else writeToAppFolder(name, json)
                name
            } catch (e: IOException) {
                null
            } catch (e: SecurityException) {
                null
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun writeToDownloads(name: String, json: String) {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: throw IOException("No Downloads")
        resolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) } ?: throw IOException("Cannot write $uri")
    }

    private fun writeToAppFolder(name: String, json: String) {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: throw IOException("No storage")
        File(dir, name).writeText(json)
    }
}
