package app.coshell.screenshots

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.home.HomeScreen
import app.coshell.home.homePreviewSample
import app.coshell.release.ReleaseNotice
import app.coshell.release.ReleaseStories
import app.coshell.release.ReleaseStoriesScreen
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Release stories (#693; boards Rel693NoticeB, Rel693S1…S5): the card on «Обзор» and every story, same setup as [NotesScreenshotTest]. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp-xhdpi", application = Application::class)
class ReleaseStoriesScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private val notice = ReleaseNotice("26.12.1", ReleaseStories.current.size)

    @Test fun `home notice dark`() = shot("release_home_dark", dark = true) { HomeScreen(homePreviewSample, release = notice) }

    @Test fun `home notice light`() = shot("release_home_light") { HomeScreen(homePreviewSample, release = notice) }

    @Test fun `story 1 dark`() = story(0, dark = true)

    @Test fun `story 2 dark`() = story(1, dark = true)

    @Test fun `story 3 dark`() = story(2, dark = true)

    @Test fun `story 4 dark`() = story(3, dark = true)

    @Test fun `story 5 dark`() = story(4, dark = true)

    @Test fun `story 2 light`() = story(1, dark = false)

    private fun story(index: Int, dark: Boolean) = shot("release_story_${index + 1}_${if (dark) "dark" else "light"}", dark) {
        ReleaseStoriesScreen("26.12.1", ReleaseStories.current, startAt = index)
    }

    private fun shot(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent { CoShellTheme(darkTheme = dark) { Surface { content() } } }
        compose.mainClock.advanceTimeBy(3_000L)
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }
}
