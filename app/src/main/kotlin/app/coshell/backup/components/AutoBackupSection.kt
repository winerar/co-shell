package app.coshell.backup.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.backup.auto.AutoBackupUiState
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.category.categoryColor
import app.coshell.core.designsystem.components.CoShellMenuRow
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.core.designsystem.components.CoShellSwitchRow
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.designsystem.R as DesignR

/**
 * Weekly automatic copy under the hero (board Backup): the folder («тап — выбрать другую») and a switch.
 * Turning the switch on without a folder opens the folder picker; off — stops the copies.
 */
@Composable
internal fun AutoBackupSection(state: AutoBackupUiState, onChooseFolder: () -> Unit, onDisable: () -> Unit) {
    CoShellRowGroup(Modifier.padding(horizontal = Spacing.l)) {
        row {
            CoShellMenuRow(
                icon = DesignR.drawable.ic_folder_open,
                color = categoryColor(CategoryPalette.colors[FOLDER_COLOR]),
                title = state.folderName?.let { stringResource(R.string.auto_backup_folder, it) }
                    ?: stringResource(R.string.auto_backup_choose),
                subtitle = stringResource(if (state.folderName != null) R.string.auto_backup_folder_change else R.string.auto_backup_folder_hint),
                onClick = onChooseFolder,
            )
        }
        row {
            CoShellSwitchRow(
                title = stringResource(R.string.auto_backup_title),
                checked = state.enabled,
                onCheckedChange = { on -> if (on) onChooseFolder() else onDisable() },
            )
        }
    }
}

// Yellow from the category palette, as on the board.
private const val FOLDER_COLOR = 10
