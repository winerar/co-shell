package app.coshell.backup

import android.content.Context
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** [DocumentStorage] over the system ContentResolver; files are UTF-8. */
class ContentResolverDocumentStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) : DocumentStorage {

    override suspend fun writeText(uri: String, text: String) = io {
        // "wt" truncates: overwriting a longer old backup must not leave its tail behind.
        val stream = context.contentResolver.openOutputStream(uri.toUri(), "wt")
            ?: throw IOException("Cannot open $uri for writing")
        stream.bufferedWriter(Charsets.UTF_8).use { it.write(text) }
    }

    override suspend fun readText(uri: String): String = io {
        val stream = context.contentResolver.openInputStream(uri.toUri())
            ?: throw IOException("Cannot open $uri for reading")
        stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }

    /** Runs on the IO pool; a revoked permission becomes an [IOException] like any other file error. */
    private suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) {
        try {
            block()
        } catch (e: SecurityException) {
            throw IOException(e)
        }
    }
}
