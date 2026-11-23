package app.coshell.screenshots

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import app.coshell.ai.cloud.CloudAiActions
import app.coshell.ai.cloud.CloudAiScreen
import app.coshell.ai.cloud.CloudAiStage
import app.coshell.ai.cloud.CloudAiUiState
import app.coshell.core.designsystem.theme.CoShellTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** «Облачный разбор» (#635; boards CloudOff, CloudOn, CloudConsent, CloudNoLogin): the same setup as [AiAddonScreenshotTest]. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp-xhdpi", application = Application::class)
class CloudAiScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun `off dark`() = shot("cloud_ai_dark", dark = true) { CloudAiScreen(OFF, CloudAiActions()) }

    @Test fun `off light`() = shot("cloud_ai_light") { CloudAiScreen(OFF, CloudAiActions()) }

    @Test fun `on dark`() = shot("cloud_ai_on_dark", dark = true) { CloudAiScreen(ON, CloudAiActions()) }

    @Test fun `on light`() = shot("cloud_ai_on_light") { CloudAiScreen(ON, CloudAiActions()) }

    @Test fun `consent dark`() = shot("cloud_ai_consent_dark", dark = true) { CloudAiScreen(OFF.copy(askConsent = true), CloudAiActions()) }

    @Test fun `consent light`() = shot("cloud_ai_consent_light") { CloudAiScreen(OFF.copy(askConsent = true), CloudAiActions()) }

    @Test fun `no sign in dark`() = shot("cloud_ai_nologin_dark", dark = true) { CloudAiScreen(OFF.copy(stage = CloudAiStage.NO_LOGIN), CloudAiActions()) }

    @Test fun `no sign in light`() = shot("cloud_ai_nologin_light") { CloudAiScreen(OFF.copy(stage = CloudAiStage.NO_LOGIN), CloudAiActions()) }

    private fun shot(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent { CoShellTheme(darkTheme = dark) { Surface { content() } } }
        compose.mainClock.advanceTimeBy(3_000L)
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }

    private companion object {
        val OFF = CloudAiUiState(configured = true)
        val ON = CloudAiUiState(configured = true, stage = CloudAiStage.ON, left = 87, limit = 100)
    }
}
