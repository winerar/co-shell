package app.coshell.family.data

import android.content.Context
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Saves this phone's data before it is replaced by the family's (joining a family) into
 * app-private `files/backups/before-family-<time>.json`. Restorable via «Резервная копия» after
 * copying out, and kept for support; nothing else reads these files.
 */
class SafetyBackupWriter(private val context: Context) {
    suspend fun write(json: String) = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "backups").apply { mkdirs() }
        File(dir, "before-family-${System.currentTimeMillis()}.json").writeText(json)
    }
}
