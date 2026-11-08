package app.coshell.backup.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.backup.BackupMessage
import app.coshell.ui.form.MessageCard

/** Result of the last export/import. */
@Composable
internal fun BackupMessageCard(message: BackupMessage, modifier: Modifier = Modifier) =
    MessageCard(messageText(message), message.isError, modifier)

@Composable
private fun messageText(message: BackupMessage): String = when (message) {
    BackupMessage.Exported -> stringResource(R.string.backup_result_exported)
    is BackupMessage.Imported ->
        stringResource(R.string.backup_result_imported, message.accounts, message.categories, message.transactions)
    BackupMessage.NotABackup -> stringResource(R.string.backup_error_not_backup)
    is BackupMessage.UnsupportedVersion -> stringResource(R.string.backup_error_version)
    BackupMessage.Corrupted -> stringResource(R.string.backup_error_corrupted)
    BackupMessage.WriteFailed -> stringResource(R.string.backup_error_write)
    BackupMessage.ReadFailed -> stringResource(R.string.backup_error_read)
}
