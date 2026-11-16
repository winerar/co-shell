package app.coshell.ui.top

import app.coshell.core.designsystem.components.TabScroll
import app.coshell.core.designsystem.components.rememberTabScrollBehavior
import app.coshell.core.designsystem.components.tabScroll
import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.transactions.list.TransactionsScreen
import app.coshell.transactions.list.transactionsLongSample
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** «Живая строка» (header poll А, 2026-11-15): what the tab header shows at rest, scrolled and while choosing rows. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp", application = Application::class)
class TabTopBarTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private fun tab(subtitle: String?) = compose.setContent {
        CoShellTheme(darkTheme = true) {
            val scroll = rememberTabScrollBehavior()
            Scaffold(topBar = { TabTopBar(TITLE, scroll, subtitle) }, modifier = Modifier.tabScroll(scroll)) { padding ->
                LazyColumn(Modifier.fillMaxSize().padding(padding).testTag(LIST)) {
                    items((1..60).toList()) { Text("Строка $it") }
                }
            }
        }
    }

    @Test
    fun `second line shows only while the list is scrolled`() {
        tab(subtitle = WHERE)
        compose.onNodeWithText(TITLE).assertIsDisplayed()
        compose.onNodeWithText(WHERE).assertDoesNotExist()

        compose.onNodeWithTag(LIST).performTouchInput { swipeUp() }
        compose.waitForIdle()
        compose.onNodeWithText(TITLE).assertIsDisplayed()
        compose.onNodeWithText(WHERE).assertIsDisplayed()

        compose.onNodeWithTag(LIST).performTouchInput { repeat(4) { swipeDown() } }
        compose.waitForIdle()
        compose.onNodeWithText(WHERE).assertDoesNotExist()
    }

    @Test
    fun `without a second line scrolling keeps one line`() {
        tab(subtitle = null)
        compose.onNodeWithTag(LIST).performTouchInput { swipeUp() }
        compose.waitForIdle()
        compose.onNodeWithText(TITLE).assertIsDisplayed()
        compose.onNodeWithText(" · ", substring = true).assertDoesNotExist()
    }

    @Test
    fun `operations name the day at the top once scrolled`() {
        compose.setContent { CoShellTheme(darkTheme = true) { TransactionsScreen(transactionsLongSample) } }
        // Day titles in the list show the name and the net apart; only the header joins them with « · ».
        compose.onNodeWithText(" · -", substring = true).assertDoesNotExist()
        compose.onNodeWithText("Операции").assertIsDisplayed()
        compose.onNode(hasScrollToIndexAction()).performTouchInput { swipeUp() }
        compose.waitForIdle()
        compose.onNodeWithText(" · -", substring = true).assertIsDisplayed()
    }

    @Test
    fun `selection header replaces the tab header and counts rows`() {
        var count by mutableIntStateOf(0)
        compose.setContent {
            CoShellTheme(darkTheme = true) {
                val scroll = rememberTabScrollBehavior()
                Scaffold(topBar = { TabOrSelectionTopBar(TITLE, scroll, count, onClearSelection = { count = 0 }) }) { }
            }
        }
        compose.onNodeWithText(TITLE).assertIsDisplayed()

        count = 2
        compose.waitForIdle()
        compose.onNodeWithText("Выбрано 2").assertIsDisplayed()
        compose.onNodeWithText(TITLE).assertDoesNotExist()

        count = 3 // the number rolls; the phrase is still read as one
        compose.waitForIdle()
        compose.onNodeWithText("Выбрано 3").assertIsDisplayed()

        count = 0
        compose.waitForIdle()
        compose.onNodeWithText(TITLE).assertIsDisplayed()
        compose.onNodeWithText("Выбрано", substring = true).assertDoesNotExist()
    }

    private companion object {
        const val TITLE = "Операции"
        const val WHERE = "Сегодня · −4 500,00 ARS"
        const val LIST = "list"
    }
}
