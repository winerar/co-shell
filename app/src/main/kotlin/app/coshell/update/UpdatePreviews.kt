package app.coshell.update

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.domain.update.AppRelease

private val sample = AppRelease(
    versionCode = 43,
    versionName = "0.38",
    apkUrl = "https://github.com/winerar/co-shell-releases/releases/download/build-43/co-shell-43.apk",
    sha256 = "0".repeat(64),
    sizeBytes = 6_300_000,
    notes = "• Обновления приходят сами\n• Фильтр по счёту в операциях",
)

@Preview(showBackground = true)
@Composable
private fun UpdateAvailablePreview() = CoShellTheme {
    UpdateScreen(UpdateUiState("0.37", 42, phase = UpdatePhase.Available(sample)), UpdateActions())
}

@Preview(showBackground = true)
@Composable
private fun UpdateDownloadingPreview() = CoShellTheme {
    UpdateScreen(UpdateUiState("0.37", 42, phase = UpdatePhase.Downloading(sample, 0.45f)), UpdateActions())
}

@Preview(showBackground = true)
@Composable
private fun UpdateFailedPreview() = CoShellTheme {
    UpdateScreen(UpdateUiState("0.37", 42, phase = UpdatePhase.Failed(UpdateError.Checksum, sample)), UpdateActions())
}

@Preview(showBackground = true)
@Composable
private fun UpdateUpToDateDarkPreview() = CoShellTheme(darkTheme = true) {
    UpdateScreen(UpdateUiState("0.38", 43, phase = UpdatePhase.UpToDate(sample)), UpdateActions())
}
