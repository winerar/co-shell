package app.coshell.settings

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.domain.settings.ClockMinutes
import app.coshell.core.domain.settings.ReminderSettings
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** «Другое…» in «Уведомления» (#777): the counter sheet saves on «Готово», goes away, and the own value takes the place of «Другое…». */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w393dp-h873dp-xhdpi", application = Application::class)
class ReminderPickersTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private val days = mutableIntStateOf(1)
    private val time = mutableIntStateOf(ClockMinutes.of(9))

    private fun show() {
        compose.setContent {
            CoShellTheme {
                Surface {
                    androidx.compose.foundation.layout.Column {
                        TimePickCard("Время", time.intValue, ReminderSettings.TIMES) { time.intValue = it }
                        DaysPickCard(days.intValue) { days.intValue = it }
                    }
                }
            }
        }
    }

    @Test
    fun `other days are counted in the sheet and saved on Done`() {
        show()
        compose.onAllNodesWithText("Другое…")[1].performClick()
        compose.waitForIdle()
        compose.onNodeWithContentDescription("Больше дней").performClick()
        compose.onNodeWithContentDescription("Больше дней").performClick()
        compose.onNodeWithText("Готово").performClick()
        compose.waitForIdle()
        assertEquals(3, days.intValue)
        compose.onAllNodesWithText("Готово").assertCountEquals(0)
    }

    @Test
    fun `an own value stands in place of Other`() {
        days.intValue = 5
        show()
        compose.onNodeWithText("За 5 дней").performClick()
        compose.waitForIdle()
        compose.onNodeWithContentDescription("Меньше дней").performClick()
        compose.onNodeWithText("Готово").performClick()
        compose.waitForIdle()
        assertEquals(4, days.intValue)
        compose.onAllNodesWithText("За 4 дня").assertCountEquals(1)
    }

    @Test
    fun `the same day is the lower end`() {
        days.intValue = 0
        show()
        compose.onAllNodesWithText("Другое…")[1].performClick()
        compose.waitForIdle()
        compose.onNodeWithContentDescription("Меньше дней").assertIsNotEnabled()
    }

    @Test
    fun `time steps by a quarter of an hour`() {
        show()
        compose.onAllNodesWithText("Другое…")[0].performClick()
        compose.waitForIdle()
        compose.onNodeWithContentDescription("Раньше").performClick()
        compose.onNodeWithText("Готово").performClick()
        compose.waitForIdle()
        assertEquals(ClockMinutes.of(8, 45), time.intValue)
        compose.onAllNodesWithText("8:45").assertCountEquals(1)
    }
}
