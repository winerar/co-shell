package app.coshell.backup.auto

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import app.coshell.backup.BACKUP_MIME_TYPE
import java.io.IOException
import javax.inject.Inject
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Writes a copy into a folder the user granted once (Storage Access Framework tree; local
 * storage or Google Drive) and deletes old automatic copies ([AutoBackupPolicy.toPrune]).
 */
class FolderBackupWriter @Inject constructor(@ApplicationContext private val context: Context) {
    private val resolver get() = context.contentResolver

    /** Keeps access to the folder across restarts; returns its display name. */
    fun takeFolder(treeUri: String): String? {
        val uri = treeUri.toUri()
        val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        resolver.takePersistableUriPermission(uri, flags)
        return displayName(DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri)))
    }

    fun releaseFolder(treeUri: String) {
        runCatching {
            val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            resolver.releasePersistableUriPermission(treeUri.toUri(), flags)
        }
    }

    suspend fun write(treeUri: String, fileName: String, json: String) = withContext(Dispatchers.IO) {
        try {
            val tree = treeUri.toUri()
            val dir = DocumentsContract.buildDocumentUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree))
            val existing = children(tree)
            // Same day again: overwrite instead of creating "name (1).json".
            val target = existing[fileName]?.let { DocumentsContract.buildDocumentUriUsingTree(tree, it) }
                ?: DocumentsContract.createDocument(resolver, dir, BACKUP_MIME_TYPE, fileName)
                ?: throw IOException("Cannot create $fileName")
            resolver.openOutputStream(target, "wt")?.bufferedWriter(Charsets.UTF_8)?.use { it.write(json) }
                ?: throw IOException("Cannot open $fileName")
            val names = children(tree)
            AutoBackupPolicy.toPrune(names.keys.toList()).forEach { name ->
                runCatching { DocumentsContract.deleteDocument(resolver, DocumentsContract.buildDocumentUriUsingTree(tree, names.getValue(name))) }
            }
        } catch (e: SecurityException) {
            throw IOException(e) // access to the folder was revoked
        }
    }

    /** Display name → document id of the folder's files. */
    private fun children(tree: Uri): Map<String, String> {
        val uri = DocumentsContract.buildChildDocumentsUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree))
        val columns = arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME)
        return resolver.query(uri, columns, null, null, null)?.use { c ->
            buildMap { while (c.moveToNext()) put(c.getString(1), c.getString(0)) }
        }.orEmpty()
    }

    private fun displayName(doc: Uri): String? =
        resolver.query(doc, arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME), null, null, null)
            ?.use { if (it.moveToFirst()) it.getString(0) else null }
}
