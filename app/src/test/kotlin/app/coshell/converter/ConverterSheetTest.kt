package app.coshell.converter

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.coshell.core.designsystem.theme.CoShellTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** A currency picked for a side of the pair must close the list: a left-over invisible window swallowed every key (#730). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w393dp-h873dp-xhdpi", application = Application::class)
class ConverterSheetTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private val state = ConverterUiState(
        isLoading = false,
        from = "USD",
        to = "ARS",
        fromText = "0",
        toText = "0",
        options = listOf("USD", "AMD", "ARS"),
        source = "USD",
    )
    private val picked = mutableListOf<Pair<ConverterSide, String>>()

    private fun show() {
        compose.setContent {
            CoShellTheme {
                Surface {
                    ConverterSheetBody(state, ConverterActions(onPick = { side, code -> picked += side to code }), onClose = {})
                }
            }
        }
    }

    @Test
    fun `picking the give currency passes it on and closes the list`() {
        show()
        compose.onNodeWithText("USD ▾").performClick()
        compose.waitForIdle()
        compose.onAllNodesWithText("Отдаю в валюте").assertCountEquals(1)

        compose.onNodeWithText("AMD").performClick()
        compose.waitForIdle()

        assertEquals(listOf(ConverterSide.FROM to "AMD"), picked)
        compose.onAllNodesWithText("Отдаю в валюте").assertCountEquals(0)
    }

    @Test
    fun `picking the get currency passes it on and closes the list`() {
        show()
        compose.onNodeWithText("ARS ▾").performClick()
        compose.waitForIdle()
        compose.onAllNodesWithText("Получаю в валюте").assertCountEquals(1)

        compose.onNodeWithText("AMD").performClick()
        compose.waitForIdle()

        assertEquals(listOf(ConverterSide.TO to "AMD"), picked)
        compose.onAllNodesWithText("Получаю в валюте").assertCountEquals(0)
    }
}
