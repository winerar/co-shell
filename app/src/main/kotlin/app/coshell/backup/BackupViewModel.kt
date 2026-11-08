package app.coshell.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.BackupImportResult
import app.coshell.core.domain.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Saves the whole database to a JSON file and restores from one.
 * The file is picked by the system dialog in [BackupRoute]; here only its uri arrives.
 */
@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backup: BackupRepository,
    private val storage: DocumentStorage,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : ViewModel() {

    private val _state = MutableStateFlow(BackupUiState())
    val state: StateFlow<BackupUiState> = _state.asStateFlow()

    /** Name offered in the "save as" dialog, e.g. `co-shell-2026-11-08.json`. */
    fun suggestedFileName(): String = backupFileName(clock.now().toLocalDateTime(timeZone).date)

    fun exportTo(uri: String) = runBusy {
        val json = backup.exportJson()
        try {
            storage.writeText(uri, json)
            BackupMessage.Exported
        } catch (e: IOException) {
            BackupMessage.WriteFailed
        }
    }

    fun onImportFilePicked(uri: String) = _state.update { it.copy(pendingImportUri = uri, message = null) }

    fun cancelImport() = _state.update { it.copy(pendingImportUri = null) }

    fun confirmImport() {
        val uri = _state.value.pendingImportUri ?: return
        _state.update { it.copy(pendingImportUri = null) }
        runBusy {
            val json = try {
                storage.readText(uri)
            } catch (e: IOException) {
                return@runBusy BackupMessage.ReadFailed
            }
            backup.importJson(json).toMessage()
        }
    }

    fun dismissMessage() = _state.update { it.copy(message = null) }

    private fun runBusy(action: suspend () -> BackupMessage) {
        if (_state.value.isBusy) return
        _state.update { it.copy(isBusy = true, message = null) }
        viewModelScope.launch {
            val message = action()
            _state.update { it.copy(isBusy = false, message = message) }
        }
    }
}

internal fun BackupImportResult.toMessage(): BackupMessage = when (this) {
    is BackupImportResult.Imported -> BackupMessage.Imported(accounts, categories, transactions)
    BackupImportResult.NotABackup -> BackupMessage.NotABackup
    is BackupImportResult.UnsupportedVersion -> BackupMessage.UnsupportedVersion(version)
    is BackupImportResult.Corrupted -> BackupMessage.Corrupted
}
