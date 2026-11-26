package app.coshell.aibench

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import java.io.File

/**
 * Hands the recorded answers (`answers/<model>-<cpu|gpu>.json`) to another app as files (#742): a long text pasted into a chat gets cut,
 * a file does not. Read-only, not exported — access only through the share intent's one-time grant. No androidx in this tool, so no FileProvider.
 */
class AnswersProvider : ContentProvider() {
    override fun onCreate() = true

    private fun fileOf(uri: Uri): File? {
        val name = uri.lastPathSegment ?: return null
        if (!name.endsWith(".json") || name.contains('/')) return null
        return File(context?.getExternalFilesDir("answers"), name).takeIf { it.isFile }
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? =
        fileOf(uri)?.let { ParcelFileDescriptor.open(it, ParcelFileDescriptor.MODE_READ_ONLY) }

    override fun getType(uri: Uri) = "application/json"

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, args: Array<out String>?, sort: String?): Cursor? {
        val file = fileOf(uri) ?: return null
        return MatrixCursor(arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)).apply { addRow(arrayOf<Any>(file.name, file.length())) }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, args: Array<out String>?) = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, args: Array<out String>?) = 0

    companion object {
        const val AUTHORITY = "app.coshell.aibench.answers"
        fun uriOf(name: String): Uri = Uri.parse("content://$AUTHORITY/$name")
    }
}
