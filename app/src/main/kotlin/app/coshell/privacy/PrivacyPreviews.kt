package app.coshell.privacy

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.domain.privacy.PrivacyOptions
import app.coshell.privacy.pin.PinLayout
import app.coshell.privacy.pin.PinUi
import app.coshell.privacy.settings.PrivacyActions
import app.coshell.privacy.settings.PrivacyScreen

/** Samples of #145 screens (boards `Priv145*`); also used by the screenshot tests. */
internal val PrivacyOnSample = PrivacyOptions(pinEnabled = true, biometric = true, hideFaceDown = true, hideInRecents = true)

internal val LockSample = PinUi("Введите PIN", filled = 2)

internal val LockErrorSample = PinUi("Введите PIN", "PIN не подошёл. Попробуйте ещё раз.", error = true, errorKey = 1, filled = 0)

@Composable
internal fun LockSampleScreen(ui: PinUi = LockSample) =
    PinLayout(ui, onKey = {}, biometricLabel = "Отпечаток или лицо", link = "Забыли PIN?" to {})

@Preview
@Composable
private fun LockPreview() = CoShellTheme(darkTheme = true) { LockSampleScreen() }

@Preview
@Composable
private fun PrivacyOnPreview() = CoShellTheme(darkTheme = true) { PrivacyScreen(PrivacyOnSample, biometricAvailable = true, actions = PrivacyActions()) }

@Preview
@Composable
private fun PrivacyOffPreview() = CoShellTheme(darkTheme = false) { PrivacyScreen(PrivacyOptions(), biometricAvailable = true, actions = PrivacyActions()) }
