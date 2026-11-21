package app.coshell.screenshots

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import app.coshell.ai.AiAddonActions
import app.coshell.ai.AiAddonProblem
import app.coshell.ai.AiAddonScreen
import app.coshell.ai.AiAddonStage
import app.coshell.ai.AiAddonUiState
import app.coshell.core.designsystem.theme.CoShellTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** «ИИ на телефоне» (#587; boards AiOff, AiStep1, AiStep2, AiOn, AiData): the same setup as [UpdateScreenshotTest]. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp-xhdpi", application = Application::class)
class AiAddonScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun `off dark`() = shot("ai_addon_dark", dark = true) { AiAddonScreen(AiAddonUiState(), AiAddonActions()) }

    @Test fun `off light`() = shot("ai_addon_light") { AiAddonScreen(AiAddonUiState(), AiAddonActions()) }

    @Test fun `step 2 dark`() = shot("ai_addon_loading_dark", dark = true) { AiAddonScreen(STEP2, AiAddonActions()) }

    @Test fun `step 2 light`() = shot("ai_addon_loading_light") { AiAddonScreen(STEP2, AiAddonActions()) }

    @Test fun `on dark`() = shot("ai_addon_on_dark", dark = true) { AiAddonScreen(AiAddonUiState(AiAddonStage.ON), AiAddonActions()) }

    @Test fun `on light`() = shot("ai_addon_on_light") { AiAddonScreen(AiAddonUiState(AiAddonStage.ON), AiAddonActions()) }

    @Test fun `weak light`() = shot("ai_addon_weak_light") {
        AiAddonScreen(AiAddonUiState(AiAddonStage.UNSUPPORTED, memoryGb = 4), AiAddonActions())
    }

    @Test fun `weak dark`() = shot("ai_addon_weak_dark", dark = true) {
        AiAddonScreen(AiAddonUiState(AiAddonStage.UNSUPPORTED, memoryGb = 4), AiAddonActions())
    }

    @Test fun `problem dark`() = shot("ai_addon_problem_dark", dark = true) {
        AiAddonScreen(AiAddonUiState(problem = AiAddonProblem.DOWNLOAD_FAILED), AiAddonActions())
    }

    private fun shot(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent { CoShellTheme(darkTheme = dark) { Surface { content() } } }
        compose.mainClock.advanceTimeBy(3_000L)
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }

    private companion object {
        val STEP2 = AiAddonUiState(AiAddonStage.STEP_MODEL, progress = 0.42f, doneMb = 1_060, totalMb = 2_500)
    }
}
