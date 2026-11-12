package app.coshell.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellRowGroup
import app.coshell.wipe.components.WipeDataRow
import app.coshell.backup.auto.AutoBackupUiState
import app.coshell.backup.components.AutoBackupSection
import app.coshell.backup.components.BackupMessageCard
import app.coshell.core.designsystem.category.CategoryPalette
import app.coshell.core.designsystem.components.CoShellLinearProgress
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.more.components.MoreTile
import app.coshell.ui.form.ConfirmDialog
import app.coshell.ui.top.ScreenTopBar
import app.coshell.core.designsystem.R as DesignR

/** "More → Backup": explanation, "Save a copy", "Restore from a copy", and the last result. */
@Composable
fun BackupScreen(state: BackupUiState, actions: BackupActions, auto: AutoBackupUiState = AutoBackupUiState()) {
    Scaffold(
        topBar = {
            ScreenTopBar(stringResource(R.string.backup_title), onBack = actions.onBack)
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            // Mockup 14: the automatic copy first (is the family's data safe?), manual copy as two tiles under it.
            AutoBackupSection(auto, actions.onChooseAutoFolder, actions.onDisableAuto)
            Row(
                Modifier.padding(horizontal = Spacing.l, vertical = Spacing.s),
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
            ) {
                MoreTile(
                    DesignR.drawable.ic_file_save,
                    stringResource(R.string.backup_export),
                    stringResource(R.string.backup_export_hint),
                    CategoryPalette.colors[MANUAL_SAVE_COLOR],
                    onClick = { if (!state.isBusy) actions.onExport() },
                    modifier = Modifier.weight(1f),
                )
                MoreTile(
                    DesignR.drawable.ic_file_open,
                    stringResource(R.string.backup_import),
                    stringResource(R.string.backup_import_hint),
                    CategoryPalette.colors[MANUAL_RESTORE_COLOR],
                    onClick = { if (!state.isBusy) actions.onImport() },
                    modifier = Modifier.weight(1f),
                )
            }
            if (state.isBusy) {
                CoShellLinearProgress(Modifier.padding(horizontal = Spacing.l))
            }
            state.message?.let { BackupMessageCard(it, Modifier.padding(horizontal = Spacing.l)) }
            Text(
                stringResource(R.string.backup_intro),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.l, vertical = Spacing.s),
            )
            // Board 154А: the wipe sits under the copy — make a copy first, then start clean.
            CoShellRowGroup(Modifier.padding(horizontal = Spacing.l).padding(top = Spacing.s, bottom = Spacing.l)) {
                row { WipeDataRow(actions.onWipeData) }
            }
        }
    }

    if (state.pendingImportUri != null) {
        ConfirmDialog(
            title = R.string.backup_import_confirm_title,
            text = R.string.backup_import_confirm_text,
            confirmLabel = R.string.backup_import_confirm,
            onConfirm = actions.onConfirmImport,
            onDismiss = actions.onCancelImport,
        )
    }
}

// Tile colours from the category palette: lime-green for saving, cyan for restoring (mockup 14).
private const val MANUAL_SAVE_COLOR = 9
private const val MANUAL_RESTORE_COLOR = 6
