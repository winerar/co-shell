package app.coshell.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.center
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.R
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Sizes
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The fan over «+» (#623): hold, lead the finger to a button, let go; a short tap stays a tap. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ActionFanTest {
    @get:Rule val rule = createComposeRule()

    private val picked = mutableListOf<String>()
    private var taps = 0

    private fun show(enabled: Boolean = true) {
        val actions = listOf("receipt", "notes", "template").map { name -> FanAction(R.drawable.ic_add, name) { picked += name } }
        rule.setContent {
            CoShellTheme {
                CoShellActionFan(actions, enabled, Modifier.testTag("fan")) {
                    Box(Modifier.size(48.dp).clickable { taps++ })
                }
            }
        }
    }


    @Test
    fun `holding and letting go over a button runs its action and not the tap`() {
        show()
        rule.onNodeWithTag("fan").performTouchInput { down(center) }
        rule.mainClock.advanceTimeBy(1_000)
        rule.onNodeWithTag("fan").performTouchInput {
            val at = ActionFanGeometry.center(2, 3, Sizes.fanRadius.value * density)
            moveTo(center + at)
            up()
        }
        rule.waitForIdle()
        assertEquals(listOf("template"), picked)
        assertEquals(0, taps)
    }

    @Test
    fun `letting go away from every button closes the fan and does nothing`() {
        show()
        rule.onNodeWithTag("fan").performTouchInput { down(center) }
        rule.mainClock.advanceTimeBy(1_000)
        rule.onNodeWithTag("fan").performTouchInput { up() }
        rule.waitForIdle()
        assertEquals(emptyList<String>(), picked)
        assertEquals(0, taps)
    }

    @Test
    fun `a short tap is an ordinary tap`() {
        show()
        rule.onNodeWithTag("fan").performTouchInput { click(center) }
        rule.waitForIdle()
        assertEquals(1, taps)
        assertEquals(emptyList<String>(), picked)
    }

    @Test
    fun `a fan that is off does not open`() {
        show(enabled = false)
        rule.onNodeWithTag("fan").performTouchInput { down(center) }
        rule.mainClock.advanceTimeBy(1_000)
        rule.onNodeWithTag("fan").performTouchInput {
            moveTo(center + ActionFanGeometry.center(2, 3, Sizes.fanRadius.value * density))
            up()
        }
        rule.waitForIdle()
        assertEquals(emptyList<String>(), picked)
    }
}
