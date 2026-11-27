package app.coshell.screenshots

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.goals.edit.GoalEditActions
import app.coshell.goals.edit.GoalEditScreen
import app.coshell.goals.list.GoalsActions
import app.coshell.goals.list.GoalsScreen
import app.coshell.goals.list.GoalsUiState
import app.coshell.goals.list.goalEditPreviewState
import app.coshell.goals.list.goalsPreviewState
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** «Цели» (#768; boards GoalList, GoalEmpty, GoalDetail): same setup as [NotesScreenshotTest]. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp-xhdpi", application = Application::class)
class GoalsScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun `goals dark`() = shot("goals_dark", dark = true) { GoalsScreen(goalsPreviewState(), GoalsActions()) }

    @Test fun `goals light`() = shot("goals_light") { GoalsScreen(goalsPreviewState(), GoalsActions()) }

    @Test fun `goals empty dark`() = shot("goals_empty_dark", dark = true) { GoalsScreen(GoalsUiState(isLoading = false), GoalsActions()) }

    @Test fun `goals empty light`() = shot("goals_empty_light") { GoalsScreen(GoalsUiState(isLoading = false), GoalsActions()) }

    @Test fun `goal edit dark`() = shot("goal_edit_dark", dark = true) { GoalEditScreen(goalEditPreviewState(existing = true), GoalEditActions()) }

    @Test fun `goal edit light`() = shot("goal_edit_light") { GoalEditScreen(goalEditPreviewState(existing = true), GoalEditActions()) }

    private fun shot(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent { CoShellTheme(darkTheme = dark) { Surface { content() } } }
        compose.mainClock.advanceTimeBy(3_000L)
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }
}
