package app.coshell.feedback.files

import android.content.Context
import android.net.Uri
import app.coshell.receipt.inbox.SharedFile
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** [FeedbackFiles] on the phone: copies into `cache/feedback/` (cleared by the system when space runs low). */
class AndroidFeedbackFiles @Inject constructor(@ApplicationContext private val context: Context) : FeedbackFiles {

    override suspend fun fromUri(uri: String): FeedbackFile? = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val parsed = Uri.parse(uri)
        val name = SharedFile.displayName(resolver, parsed) ?: DEFAULT_NAME
        val mime = resolver.getType(parsed) ?: "application/octet-stream"
        val dir = File(context.cacheDir, DIR).apply { mkdirs() }
        val target = File(dir, "${System.currentTimeMillis()}-${SharedFile.safeName(name)}")
        val copied = try {
            resolver.openInputStream(parsed)?.use { input -> target.outputStream().use { SharedFile.copyLimited(input, it, MAX_BYTES) } } ?: false
        } catch (_: IOException) {
            false
        } catch (_: SecurityException) {
            false
        }
        if (copied) FeedbackFile(target.path, name, mime, target.length()) else null.also { target.delete() }
    }

    override suspend fun bytes(file: FeedbackFile): ByteArray? = withContext(Dispatchers.IO) {
        runCatching { File(file.path).readBytes() }.getOrNull()
    }

    private companion object {
        const val DIR = "feedback"
        const val DEFAULT_NAME = "file"
        /** Bigger files are refused at once; the 3 MB message limit is checked when sending. */
        const val MAX_BYTES = 10L * 1024 * 1024
    }
}
