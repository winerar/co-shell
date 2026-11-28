package app.coshell.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.access.InviteRow
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.domain.settings.ThemeMode

/** «Настройки» as on board Set1Root: dark theme, the family's three currencies; also the screenshot sample. */
internal val SettingsPreviewState = SettingsState(themeMode = ThemeMode.DARK, currencies = "USD · ARS · AMD")

/** «Помощь» with every row (board Set1Help). */
internal val HelpPreviewState = HelpState(invite = InviteRow("Алекс", revoked = false), policy = true)

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    CoShellTheme { SettingsScreen(SettingsPreviewState, SettingsActions()) }
}

@Preview(showBackground = true)
@Composable
private fun SettingsSearchPreview() {
    CoShellTheme(darkTheme = true) { SettingsScreen(SettingsPreviewState, SettingsActions(), initialQuery = "вибр") }
}

@Preview(showBackground = true)
@Composable
private fun HelpSettingsPreview() {
    CoShellTheme { HelpSettingsScreen(HelpPreviewState, HelpActions()) }
}
