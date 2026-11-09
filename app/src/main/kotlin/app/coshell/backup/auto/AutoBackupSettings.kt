package app.coshell.backup.auto

import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Where and when the automatic copy was made. [folderUri] `null` — automatic copies are off. */
data class AutoBackupConfig(
    val folderUri: String? = null,
    val folderName: String? = null,
    val lastBackupAtMs: Long? = null,
    val lastFailed: Boolean = false,
)

/** Settings of the weekly copy (phone-only, like other app settings), observable by the backup screen. */
@Singleton
class AutoBackupSettings @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("auto_backup", Context.MODE_PRIVATE)
    private val _config = MutableStateFlow(read())
    val config: StateFlow<AutoBackupConfig> = _config.asStateFlow()

    fun setFolder(uri: String?, name: String?) = write { putString(FOLDER, uri); putString(NAME, name); putBoolean(FAILED, false) }

    fun recordResult(atMs: Long, failed: Boolean) = write {
        if (!failed) putLong(LAST, atMs)
        putBoolean(FAILED, failed)
    }

    private fun write(block: android.content.SharedPreferences.Editor.() -> Unit) {
        prefs.edit().apply(block).apply()
        _config.value = read()
    }

    private fun read() = AutoBackupConfig(
        folderUri = prefs.getString(FOLDER, null),
        folderName = prefs.getString(NAME, null),
        lastBackupAtMs = prefs.getLong(LAST, 0L).takeIf { it > 0 },
        lastFailed = prefs.getBoolean(FAILED, false),
    )

    private companion object {
        const val FOLDER = "folder"
        const val NAME = "folder_name"
        const val LAST = "last"
        const val FAILED = "failed"
    }
}
