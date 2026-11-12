package app.coshell.backup.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.tokens.Spacing
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.backup.auto.AutoBackupUiState
import app.coshell.ui.list.ActionRow
import app.coshell.ui.hero.StatusCard
import app.coshell.core.designsystem.R as DesignR

/**
 * Weekly automatic copy as a status card (mockup 14): on, off or failed at a glance, the folder and the
 * last copy under it; «Выбрать папку» / «Выключить» right below.
 */
@Composable
internal fun AutoBackupSection(state: AutoBackupUiState, onChooseFolder: () -> Unit, onDisable: () -> Unit) {
    val subtitle = when {
        !state.enabled -> stringResource(R.string.auto_backup_off)
        state.lastFailed -> stringResource(R.string.auto_backup_failed)
        state.lastBackup != null -> stringResource(R.string.auto_backup_last, state.lastBackup)
        else -> stringResource(R.string.auto_backup_pending)
    }
    StatusCard(
        icon = if (state.enabled && !state.lastFailed) DesignR.drawable.ic_check else DesignR.drawable.ic_settings_backup_restore,
        title = stringResource(if (state.enabled) R.string.auto_backup_on else R.string.auto_backup_title),
        subtitle = subtitle,
        tint = when {
            state.lastFailed -> MaterialTheme.colorScheme.error
            state.enabled -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.outline
        },
    )
    CoShellRowGroup(Modifier.padding(horizontal = Spacing.l)) {
        row {
            ActionRow(
                icon = DesignR.drawable.ic_folder_open,
                title = state.folderName?.let { stringResource(R.string.auto_backup_folder, it) }
                    ?: stringResource(R.string.auto_backup_choose),
                onClick = onChooseFolder,
            )
        }
        if (state.enabled) {
            row { ActionRow(icon = DesignR.drawable.ic_close, title = stringResource(R.string.auto_backup_disable), onClick = onDisable) }
        }
    }
}
