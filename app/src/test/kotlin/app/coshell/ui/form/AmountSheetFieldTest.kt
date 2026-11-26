package app.coshell.ui.form

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
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

/** The amount of an editor is typed on our own keypad in a sheet, and the sheet always goes away (#300, #730). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w393dp-h873dp-xhdpi", application = Application::class)
class AmountSheetFieldTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private val value = mutableStateOf("")

    private fun show(initial: String = "", currency: String? = "USD") {
        value.value = initial
        compose.setContent {
            CoShellTheme {
                Surface {
                    AmountSheetField(value.value, { value.value = it }, label = "Лимит", currency = currency)
                }
            }
        }
    }

    private fun open() {
        compose.onNodeWithText("Лимит").performClick()
        compose.waitForIdle()
    }

    @Test
    fun `a tap on the field raises the sheet with the keypad`() {
        show()
        compose.onAllNodesWithText("Готово").assertCountEquals(0)
        open()
        compose.onAllNodesWithText("Готово").assertCountEquals(1)
    }

    @Test
    fun `typed digits go to the field on Done and the sheet closes`() {
        show()
        open()
        compose.onNodeWithText("7").performClick()
        compose.onNodeWithText("Готово").performClick()
        compose.waitForIdle()

        assertEquals("7", value.value)
        compose.onAllNodesWithText("Готово").assertCountEquals(0)
    }

    @Test
    fun `the number already in the field is the start of the sheet`() {
        show(initial = "1234,5")
        open()
        compose.onNodeWithText("Готово").performClick()
        compose.waitForIdle()

        assertEquals("1234,5", value.value)
    }

    @Test
    fun `closing with the cross keeps the old number and closes the sheet`() {
        show(initial = "10")
        open()
        compose.onNodeWithText("5").performClick()
        compose.onNodeWithContentDescription("Закрыть").performClick()
        compose.waitForIdle()

        assertEquals("10", value.value)
        compose.onAllNodesWithText("Готово").assertCountEquals(0)
    }

    @Test
    fun `a currency without fraction digits has no decimal key and others do`() {
        assertEquals(0, fractionDigitsOf("JPY"))
        assertEquals(2, fractionDigitsOf("USD"))
        assertEquals(2, fractionDigitsOf(null))
        assertEquals(2, fractionDigitsOf("not a code"))
    }
}
