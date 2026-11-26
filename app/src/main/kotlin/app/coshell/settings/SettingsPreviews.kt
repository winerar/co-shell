package app.coshell.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.domain.settings.ThemeMode
import app.coshell.core.domain.settings.WeekStart

/** «Настройки» as on board SetCardsC: the family's three currencies, shake on; also the screenshot sample. */
internal val SettingsPreviewState = SettingsState(themeMode = ThemeMode.DARK, shake = true, currencies = "USD · ARS · AMD", weekStart = WeekStart.MONDAY)

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    CoShellTheme { SettingsScreen(SettingsPreviewState, SettingsActions()) }
}
