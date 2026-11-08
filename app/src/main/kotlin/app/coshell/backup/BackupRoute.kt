package app.coshell.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/** Connects [BackupScreen] to its ViewModel and to the system "save as" / "open file" dialogs. */
@Composable
fun BackupRoute(onBack: () -> Unit, viewModel: BackupViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val createFile = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(BACKUP_MIME_TYPE)) {
        it?.let { uri -> viewModel.exportTo(uri.toString()) }
    }
    val openFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        it?.let { uri -> viewModel.onImportFilePicked(uri.toString()) }
    }
    BackupScreen(
        state = state,
        actions = BackupActions(
            onBack = onBack,
            onExport = { createFile.launch(viewModel.suggestedFileName()) },
            onImport = { openFile.launch(arrayOf(BACKUP_MIME_TYPE, "text/plain", "application/octet-stream", "*/*")) },
            onConfirmImport = viewModel::confirmImport,
            onCancelImport = viewModel::cancelImport,
        ),
    )
}

/** Everything the screen can ask for; defaults make previews short. */
data class BackupActions(
    val onBack: () -> Unit = {},
    val onExport: () -> Unit = {},
    val onImport: () -> Unit = {},
    val onConfirmImport: () -> Unit = {},
    val onCancelImport: () -> Unit = {},
)
