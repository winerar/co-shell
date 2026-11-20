package app.coshell.receipt.inbox

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/** A shared file kept in the app cache: [name] as the sender called it, [mime] its type. */
data class SharedFile(val file: File, val name: String, val mime: String) {
    companion object {
        fun displayName(resolver: ContentResolver, uri: Uri): String? = runCatching {
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
        }.getOrNull()

        /** Letters, digits, dot, dash and underscore only; at most 80 characters. */
        fun safeName(name: String): String =
            name.map { if (it.isLetterOrDigit() || it in "._-") it else '_' }.joinToString("").take(MAX_NAME).ifEmpty { "file" }

        /** Copies at most [limit] bytes; `false` when the input is longer (the copy is not used). */
        fun copyLimited(input: InputStream, output: OutputStream, limit: Long): Boolean {
            val buffer = ByteArray(BUFFER)
            var total = 0L
            while (true) {
                val n = input.read(buffer)
                if (n < 0) return true
                total += n
                if (total > limit) return false
                output.write(buffer, 0, n)
            }
        }

        private const val MAX_NAME = 80
        private const val BUFFER = 8192
    }
}
