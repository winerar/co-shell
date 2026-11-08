package app.coshell.backup

/**
 * Reads and writes whole text files picked by the user (Storage Access Framework).
 * [uri] is a `content://` string; the Android implementation is [ContentResolverDocumentStorage].
 * Kept as an interface so [BackupViewModel] is tested without Android.
 */
interface DocumentStorage {
    suspend fun writeText(uri: String, text: String)
    suspend fun readText(uri: String): String
}
