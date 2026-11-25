package app.coshell.screenshots

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.home.HomeScreen
import app.coshell.home.HomeUiState
import app.coshell.release.StoriesScreen
import app.coshell.hints.TourStories
import androidx.compose.ui.res.stringResource
import app.coshell.R
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** «Знакомство с приложением» (#598; boards Tour598Home, Tour598S1…S6): the card on a new «Обзор» and every story. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp-xhdpi", application = Application::class)
class TourScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun `home card dark`() = shot("tour_home_dark", dark = true) { HomeScreen(HomeUiState(), tour = true) }

    @Test fun `home card light`() = shot("tour_home_light") { HomeScreen(HomeUiState(), tour = true) }

    @Test fun `story 1 dark`() = story(0, dark = true)

    @Test fun `story 2 dark`() = story(1, dark = true)

    @Test fun `story 3 dark`() = story(2, dark = true)

    @Test fun `story 4 dark`() = story(3, dark = true)

    @Test fun `story 5 dark`() = story(4, dark = true)

    @Test fun `story 6 dark`() = story(5, dark = true)

    @Test fun `story 3 light`() = story(2, dark = false)

    private fun story(index: Int, dark: Boolean) = shot("tour_story_${index + 1}_${if (dark) "dark" else "light"}", dark) {
        StoriesScreen(stringResource(R.string.tour_header), TourStories.all, stringResource(R.string.tour_start), startAt = index, steps = true)
    }

    private fun shot(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent { CoShellTheme(darkTheme = dark) { Surface { content() } } }
        compose.mainClock.advanceTimeBy(3_000L)
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }
}
