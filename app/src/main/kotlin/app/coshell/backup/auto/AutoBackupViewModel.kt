package app.coshell.backup.auto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.ui.format.formatDayMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Instant
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** «Резервная копия → Автоматически раз в неделю». */
data class AutoBackupUiState(
    val enabled: Boolean = false,
    val folderName: String? = null,
    /** «9 ноября», `null` — no automatic copy yet. */
    val lastBackup: String? = null,
    val lastFailed: Boolean = false,
)

@HiltViewModel
class AutoBackupViewModel @Inject constructor(
    private val settings: AutoBackupSettings,
    private val writer: FolderBackupWriter,
    private val control: AutoBackupScheduler,
    private val timeZone: TimeZone,
) : ViewModel() {

    val state: StateFlow<AutoBackupUiState> = settings.config.map { it.toUi() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), settings.config.value.toUi())

    /** The user picked a folder: keep access, schedule weekly copies and make the first one now. */
    fun onFolderPicked(treeUri: String) {
        settings.config.value.folderUri?.takeIf { it != treeUri }?.let(writer::releaseFolder)
        val name = runCatching { writer.takeFolder(treeUri) }.getOrNull()
        settings.setFolder(treeUri, name)
        control.enable()
    }

    fun disable() {
        settings.config.value.folderUri?.let(writer::releaseFolder)
        settings.setFolder(null, null)
        control.disable()
    }

    private fun AutoBackupConfig.toUi() = AutoBackupUiState(
        enabled = folderUri != null,
        folderName = folderName,
        lastBackup = lastBackupAtMs?.let { formatDayMonth(Instant.fromEpochMilliseconds(it).toLocalDateTime(timeZone).date) },
        lastFailed = lastFailed,
    )
}
