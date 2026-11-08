package app.coshell.backup

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme

@Preview(showBackground = true)
@Composable
private fun BackupImportedPreview() {
    CoShellTheme {
        BackupScreen(BackupUiState(message = BackupMessage.Imported(3, 24, 1520)), BackupActions())
    }
}

@Preview(showBackground = true)
@Composable
private fun BackupErrorPreview() {
    CoShellTheme { BackupScreen(BackupUiState(message = BackupMessage.NotABackup), BackupActions()) }
}
