package app.coshell.backup

/** State of the backup screen; [message] is the result of the last export or import. */
data class BackupUiState(
    val isBusy: Boolean = false,
    /** File the user picked for restoring; the screen asks for confirmation while it is set. */
    val pendingImportUri: String? = null,
    val message: BackupMessage? = null,
)

/** Outcome shown under the buttons until the next action. */
sealed interface BackupMessage {
    val isError: Boolean get() = false

    data object Exported : BackupMessage
    data class Imported(val accounts: Int, val categories: Int, val transactions: Int) : BackupMessage
    data object NotABackup : BackupMessage { override val isError get() = true }
    data class UnsupportedVersion(val version: Int) : BackupMessage { override val isError get() = true }
    data object Corrupted : BackupMessage { override val isError get() = true }
    data object WriteFailed : BackupMessage { override val isError get() = true }
    data object ReadFailed : BackupMessage { override val isError get() = true }
}
