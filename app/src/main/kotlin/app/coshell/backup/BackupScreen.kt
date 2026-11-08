package app.coshell.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.coshell.R
import app.coshell.backup.components.BackupMessageCard
import app.coshell.ui.form.ConfirmDialog
import app.coshell.ui.list.ActionRow
import app.coshell.core.designsystem.R as DesignR

/** "More → Backup": explanation, "Save a copy", "Restore from a copy", and the last result. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(state: BackupUiState, actions: BackupActions) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.backup_title)) },
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(painterResource(DesignR.drawable.ic_arrow_back), stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(R.string.backup_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            ActionRow(
                icon = DesignR.drawable.ic_file_save,
                title = stringResource(R.string.backup_export),
                subtitle = stringResource(R.string.backup_export_hint),
                onClick = actions.onExport,
                enabled = !state.isBusy,
            )
            ActionRow(
                icon = DesignR.drawable.ic_file_open,
                title = stringResource(R.string.backup_import),
                subtitle = stringResource(R.string.backup_import_hint),
                onClick = actions.onImport,
                enabled = !state.isBusy,
            )
            if (state.isBusy) {
                LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            }
            state.message?.let { BackupMessageCard(it, Modifier.padding(horizontal = 16.dp)) }
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
