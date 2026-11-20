package app.coshell.screenshots

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import app.coshell.core.designsystem.amount.LocalAmountsHidden
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.domain.privacy.PrivacyOptions
import app.coshell.home.HomeScreen
import app.coshell.home.homePreviewSample
import app.coshell.privacy.LockErrorSample
import app.coshell.privacy.LockSampleScreen
import app.coshell.privacy.PrivacyOnSample
import app.coshell.privacy.settings.PrivacyActions
import app.coshell.privacy.settings.PrivacyScreen
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** #145: lock screen (board `Priv145LockA`, `Priv145Error`), «Конфиденциальность» (`Priv145Settings*`), hidden sums (`Priv145HideA`). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp-xhdpi", application = Application::class)
class PrivacyScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun `lock dark`() = shot("lock_dark", dark = true) { LockSampleScreen() }

    @Test fun `lock light`() = shot("lock_light") { LockSampleScreen() }

    @Test fun `lock error dark`() = shot("lock_error_dark", dark = true) { LockSampleScreen(LockErrorSample) }

    @Test fun `privacy dark`() = shot("privacy_dark", dark = true) { PrivacyScreen(PrivacyOnSample, true, PrivacyActions()) }

    @Test fun `privacy light`() = shot("privacy_light") { PrivacyScreen(PrivacyOnSample, true, PrivacyActions()) }

    @Test fun `privacy off light`() = shot("privacy_off_light") { PrivacyScreen(PrivacyOptions(), true, PrivacyActions()) }

    @Test fun `home hidden dark`() = shot("home_hidden_dark", dark = true, hidden = true) { HomeScreen(homePreviewSample) }

    private fun shot(name: String, dark: Boolean = false, hidden: Boolean = false, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            CoShellTheme(darkTheme = dark) { CompositionLocalProvider(LocalAmountsHidden provides hidden) { Surface { content() } } }
        }
        compose.mainClock.advanceTimeBy(3_000L)
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }
}
