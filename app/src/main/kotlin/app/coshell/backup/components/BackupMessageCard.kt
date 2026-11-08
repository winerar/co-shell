package app.coshell.backup.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.coshell.R
import app.coshell.backup.BackupMessage

/** Result of the last export/import; errors use the error container colour. */
@Composable
internal fun BackupMessageCard(message: BackupMessage, modifier: Modifier = Modifier) {
    val colors = if (message.isError) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
    } else {
        CardDefaults.cardColors()
    }
    Card(colors = colors, modifier = modifier.fillMaxWidth()) {
        Text(messageText(message), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp))
    }
}

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
