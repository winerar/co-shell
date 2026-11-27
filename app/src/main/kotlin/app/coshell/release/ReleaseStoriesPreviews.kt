package app.coshell.release

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme

@Preview(showBackground = true)
@Composable
private fun ReleaseStoriesPreview() {
    CoShellTheme(darkTheme = true) { ReleaseStoriesScreen("26.12.1", ReleaseStories.sample) }
}

@Preview(showBackground = true)
@Composable
private fun ReleaseNoticePreview() {
    CoShellTheme { ReleaseNoticeCard("26.12.1", onOpen = {}, onDismiss = {}) }
}
