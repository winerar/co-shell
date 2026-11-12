package app.coshell.crash

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** The last crash, kept until it is sent or dismissed (#218). */
interface CrashLog {
    /** Text of the last crash not yet sent or dismissed, or null. */
    fun pending(): String?

    fun save(text: String)

    fun clear()
}

/** One small file in the app's own storage: written synchronously while the process is dying. */
@Singleton
class FileCrashLog @Inject constructor(@ApplicationContext context: Context) : CrashLog {
    private val file = File(context.filesDir, "crash/last.txt")

    override fun pending(): String? = runCatching { file.takeIf { it.exists() }?.readText() }.getOrNull()?.ifBlank { null }

    override fun save(text: String) {
        runCatching {
            file.parentFile?.mkdirs()
            file.writeText(text)
        }
    }

    override fun clear() {
        file.delete()
    }
}
