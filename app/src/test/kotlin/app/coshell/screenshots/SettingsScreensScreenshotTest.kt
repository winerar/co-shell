package app.coshell.screenshots

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.domain.DataCounts
import app.coshell.feedback.FeedbackActions
import app.coshell.feedback.FeedbackScreen
import app.coshell.feedback.FeedbackUiState
import app.coshell.wipe.WipeDataScreen
import app.coshell.wipe.WipeDataUiState
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** «Отзыв» (board `Feedback`) and «Стереть все данные» (board `Wipe`) in both themes (#382); same setup as [ScreensScreenshotTest]. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp-xhdpi", application = Application::class)
class SettingsScreensScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun `feedback light`() = shot("feedback_light") { FeedbackScreen(feedback, FeedbackActions()) }

    @Test fun `feedback dark`() = shot("feedback_dark", dark = true) { FeedbackScreen(feedback, FeedbackActions()) }

    @Test fun `wipe light`() = shot("wipe_light") { WipeDataScreen(wipe) }

    @Test fun `wipe dark`() = shot("wipe_dark", dark = true) { WipeDataScreen(wipe) }

    private fun shot(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent { CoShellTheme(darkTheme = dark) { Surface { content() } } }
        compose.mainClock.advanceTimeBy(3_000L)
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }

    private companion object {
        val feedback = FeedbackUiState(text = "Кнопка «Провести» на узком экране налезает на сумму")
        val wipe = WipeDataUiState(counts = DataCounts(accounts = 12, transactions = 3_480, categories = 41, manualRates = 3), word = "СТЕР")
    }
}
