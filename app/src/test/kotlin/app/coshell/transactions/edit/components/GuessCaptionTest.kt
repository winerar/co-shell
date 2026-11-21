package app.coshell.transactions.edit.components

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.domain.categoryguess.CategoryGuess
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The grey line of an automatic category (#584): history and brand dictionary say where the guess came from. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp", application = Application::class)
class GuessCaptionTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `history says by past operations`() {
        compose.setContent { CoShellTheme(darkTheme = true) { GuessCaption(CategoryGuess.Source.HISTORY) } }
        compose.onNodeWithText("по прошлым операциям").assertIsDisplayed()
    }

    @Test
    fun `dictionary says by name`() {
        compose.setContent { CoShellTheme(darkTheme = true) { GuessCaption(CategoryGuess.Source.DICTIONARY) } }
        compose.onNodeWithText("по названию").assertIsDisplayed()
    }
}
