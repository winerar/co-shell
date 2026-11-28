package app.coshell.more

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.coshell.R
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.newmark.NewFeatures
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The «новое» pill (#335): every key of [NewFeatures] sits on a row of «Ещё», so a new place really wears the mark after an
 * update; a tap on the row hands its key back so the mark goes away.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ru-w411dp-h2400dp", application = Application::class)
class MoreNewMarksTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `every new feature key shows the pill on its place`() {
        compose.setContent { CoShellTheme { MoreScreen(MoreActions(), MoreState(newMarks = NewFeatures.more.toSet())) } }
        val pill = compose.activity.getString(R.string.new_mark)
        assertEquals(NewFeatures.more.size, compose.onAllNodesWithText(pill).fetchSemanticsNodes().size)
    }

    @Test
    fun `opening a marked tile takes its mark away`() {
        val opened = mutableListOf<String>()
        var debts = 0
        compose.setContent {
            CoShellTheme {
                MoreScreen(MoreActions(onDebts = { debts++ }, onOpenedNew = { opened += it }), MoreState(newMarks = setOf(NewFeatures.DEBTS)))
            }
        }
        val title = compose.activity.getString(R.string.debts_title)
        compose.onNodeWithText(title).performClick()
        compose.waitForIdle()
        assertEquals(listOf(NewFeatures.DEBTS), opened)
        assertEquals(1, debts)
    }
}

