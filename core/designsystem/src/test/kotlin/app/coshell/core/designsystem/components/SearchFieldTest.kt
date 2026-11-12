package app.coshell.core.designsystem.components

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import app.coshell.core.designsystem.theme.CoShellTheme
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SearchFieldTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private var query = ""

    @Before
    fun setUp() {
        compose.setContent {
            CoShellTheme {
                var value by remember { mutableStateOf("") }
                CoShellSearchField(value, { value = it; query = it }, placeholder = PLACEHOLDER, clearLabel = CLEAR)
            }
        }
    }

    @Test
    fun `empty field shows the placeholder and no clear button`() {
        compose.onNodeWithText(PLACEHOLDER).assertExists()
        compose.onNodeWithContentDescription(CLEAR).assertDoesNotExist()
    }

    @Test
    fun `typed query hides the placeholder and the cross clears it`() {
        compose.onNode(hasSetTextAction()).performTextInput("ga")
        assertEquals("ga", query)
        compose.onNodeWithText(PLACEHOLDER).assertDoesNotExist()

        compose.onNodeWithContentDescription(CLEAR).performClick()

        assertEquals("", query)
        compose.onNodeWithText(PLACEHOLDER).assertExists()
        compose.onNodeWithContentDescription(CLEAR).assertDoesNotExist()
    }

    private companion object {
        const val PLACEHOLDER = "Поиск счёта"
        const val CLEAR = "Очистить поиск"
    }
}
