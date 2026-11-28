package app.coshell.screenshots

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import app.coshell.access.AccessEntry
import app.coshell.access.AccessError
import app.coshell.access.AccessLockedDialog
import app.coshell.access.AccessScreen
import app.coshell.access.InviteRow
import app.coshell.settings.HelpActions
import app.coshell.settings.HelpSettingsScreen
import app.coshell.settings.HelpState
import app.coshell.core.designsystem.theme.CoShellTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * #666/#671: the code screen (board `AccessGate`, with «Отмена» — `Inv671Change`), the «Доступ закрыт» window (`AccessLocked`)
 * and the «Код приглашения» row in «Настройки → Помощь» (`Inv671A`, `Inv671ARevoked`).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp-xhdpi", application = Application::class)
class AccessScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun `code dark`() = shot("access_code_dark", dark = true) { AccessScreen(AccessEntry(), onSubmit = {}) }

    @Test fun `code light`() = shot("access_code_light") { AccessScreen(AccessEntry(), onSubmit = {}) }

    @Test fun `code error with cancel dark`() =
        shot("access_code_error_dark", dark = true) { AccessScreen(AccessEntry(error = AccessError.NOT_FOUND), onSubmit = {}, onCancel = {}) }

    @Test fun `locked dark`() = shot("access_locked_dark", dark = true, dialog = true) { AccessLockedDialog({}, {}, {}) }

    @Test fun `locked light`() = shot("access_locked_light", dialog = true) { AccessLockedDialog({}, {}, {}) }

    @Test fun `settings invite dark`() = shot("settings_invite_dark", dark = true) { help(InviteRow("Алекс", revoked = false)) }

    @Test fun `settings invite light`() = shot("settings_invite_light") { help(InviteRow("Алекс", revoked = false)) }

    @Test fun `settings invite revoked dark`() = shot("settings_invite_revoked_dark", dark = true) { help(InviteRow("Алекс", revoked = true)) }

    /** «Политика конфиденциальности» under the code (#652, board `Priv652A`). */
    @Test fun `settings policy dark`() = shot("settings_policy_dark", dark = true) { help(InviteRow("Алекс", revoked = false), policy = true) }

    @Test fun `settings policy light`() = shot("settings_policy_light") { help(InviteRow("Алекс", revoked = false), policy = true) }

    @Composable private fun help(invite: InviteRow, policy: Boolean = false) = HelpSettingsScreen(HelpState(invite = invite, policy = policy), HelpActions())

    private fun shot(name: String, dark: Boolean = false, dialog: Boolean = false, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent { CoShellTheme(darkTheme = dark) { Surface { content() } } }
        compose.mainClock.advanceTimeBy(3_000L)
        val root = if (dialog) compose.onAllNodes(isRoot())[1] else compose.onRoot()
        root.captureRoboImage("src/test/screenshots/$name.png")
    }
}
