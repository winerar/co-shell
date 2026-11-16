package app.coshell.ui.top

import app.coshell.core.designsystem.components.TabScroll
import app.coshell.core.designsystem.components.rememberTabScrollBehavior
import app.coshell.core.designsystem.components.tabScroll
import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.transactions.list.TransactionsScreen
import app.coshell.transactions.list.transactionsPreviewSample
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Search in the tab header (#394 Б): a loupe opens the field in place of the title, «✕» clears and closes it. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp", application = Application::class)
class TabSearchTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private var query by mutableStateOf("")

    private fun header(start: String = "") {
        query = start
        compose.setContent {
            CoShellTheme(darkTheme = true) {
                val scroll = rememberTabScrollBehavior()
                val search = TabSearch(query, { query = it }, PLACEHOLDER, OPEN, "Очистить поиск", CLOSE)
                Scaffold(topBar = { TabTopBar(TITLE, scroll, search = search) }) { }
            }
        }
    }

    @Test
    fun `loupe opens the field in place of the title`() {
        header()
        compose.onNodeWithText(TITLE).assertIsDisplayed()
        compose.onNodeWithText(PLACEHOLDER).assertDoesNotExist()

        compose.onNodeWithContentDescription(OPEN).performClick()
        compose.waitForIdle()
        compose.onNodeWithText(PLACEHOLDER).assertIsDisplayed()
        compose.onNodeWithText(TITLE).assertDoesNotExist()
        compose.onNodeWithContentDescription(CLOSE).assertIsDisplayed()
    }

    @Test
    fun `typing filters, the cross clears and closes`() {
        header()
        compose.onNodeWithContentDescription(OPEN).performClick()
        compose.waitForIdle()
        compose.onNodeWithText(PLACEHOLDER).performTextInput("кафе")
        assertEquals("кафе", query)

        compose.onNodeWithContentDescription(CLOSE).performClick()
        compose.waitForIdle()
        assertEquals("", query)
        compose.onNodeWithText(TITLE).assertIsDisplayed()
        compose.onNodeWithContentDescription(OPEN).assertIsDisplayed()
    }

    @Test
    fun `a query left from before keeps the field open`() {
        header(start = "500")
        compose.onNodeWithText("500").assertIsDisplayed()
        compose.onNodeWithText(TITLE).assertDoesNotExist()
    }

    @Test
    fun `operations have the loupe, not a search field above the chips`() {
        compose.setContent { CoShellTheme(darkTheme = true) { TransactionsScreen(transactionsPreviewSample) } }
        compose.onNodeWithContentDescription("Найти операцию").assertIsDisplayed()
        compose.onNodeWithText("Найти: заметка, тег, сумма").assertDoesNotExist()
        compose.onNodeWithText("Тип").assertIsDisplayed() // the chips stay
    }

    private companion object {
        const val TITLE = "Операции"
        const val PLACEHOLDER = "Найти: заметка, тег, сумма"
        const val OPEN = "Найти операцию"
        const val CLOSE = "Закрыть поиск"
    }
}
