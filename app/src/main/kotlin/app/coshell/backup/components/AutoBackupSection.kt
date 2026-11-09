package app.coshell.backup.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.core.designsystem.theme.accentInk
import app.coshell.R
import app.coshell.backup.auto.AutoBackupUiState
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.list.ActionRow
import app.coshell.core.designsystem.R as DesignR

/** Weekly automatic copy: chosen folder, last copy, turn off. */
@Composable
internal fun AutoBackupSection(state: AutoBackupUiState, onChooseFolder: () -> Unit, onDisable: () -> Unit) {
    Text(
        stringResource(R.string.auto_backup_title),
        style = MaterialTheme.typography.titleSmall,
        color = accentInk(),
        modifier = Modifier.padding(start = Spacing.l, end = Spacing.l, top = Spacing.l),
    )
    val subtitle = when {
        !state.enabled -> stringResource(R.string.auto_backup_off)
        state.lastFailed -> stringResource(R.string.auto_backup_failed)
        state.lastBackup != null -> stringResource(R.string.auto_backup_last, state.lastBackup)
        else -> stringResource(R.string.auto_backup_pending)
    }
    ActionRow(
        icon = DesignR.drawable.ic_folder_open,
        title = state.folderName?.let { stringResource(R.string.auto_backup_folder, it) }
            ?: stringResource(R.string.auto_backup_choose),
        subtitle = subtitle,
        onClick = onChooseFolder,
    )
    if (state.enabled) {
        ActionRow(icon = DesignR.drawable.ic_close, title = stringResource(R.string.auto_backup_disable), onClick = onDisable)
    }
}
