package app.coshell.transactions.edit

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.transactions.edit.components.CategoryBlock
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** A category picked in «Все» must close the sheet: a left-over invisible window swallowed every tap. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w393dp-h873dp-xhdpi", application = Application::class)
class AllCategoriesSheetTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `picking a category in the all sheet selects it and closes the sheet`() {
        val state = mutableStateOf(entrySheetSample.copy(categoryId = null))
        val title = "Все категории"
        compose.setContent {
            CoShellTheme {
                Surface {
                    CategoryBlock(
                        state.value,
                        TransactionEditActions(
                            onCategorySelect = { id -> state.value = state.value.copy(categoryId = id) },
                        ),
                    )
                }
            }
        }
        compose.onNodeWithText("Все").performClick()
        compose.waitForIdle()
        compose.onAllNodesWithText(title).assertCountEquals(1)

        compose.onNodeWithText("Мебель").performClick()
        compose.waitForIdle()

        assertEquals("furniture", state.value.categoryId)
        compose.onAllNodesWithText(title).assertCountEquals(0)
    }
}
