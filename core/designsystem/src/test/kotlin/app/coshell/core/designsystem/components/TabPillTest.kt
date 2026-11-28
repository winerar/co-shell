package app.coshell.core.designsystem.components

import androidx.activity.ComponentActivity
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import app.coshell.core.designsystem.tokens.Motion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The pill follows a tab that grows on a spring after it is chosen (#796), not the size it had at the tap (#809). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TabPillTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `pill ends as wide as the grown tab`() {
        var selected by mutableIntStateOf(0)
        lateinit var state: TabPillState
        compose.setContent {
            state = rememberTabPillState()
            AnimateTabPill(state, selected)
            Row(Modifier.tabPill(state, Color.Green)) {
                repeat(3) { index ->
                    val width by animateDpAsState(if (index == selected) 120.dp else 48.dp, Motion.spatial(), label = "w")
                    Box(Modifier.width(width).height(48.dp).tabPillAnchor(state, index))
                }
            }
        }
        compose.waitForIdle()
        selected = 2
        compose.mainClock.advanceTimeBy(3_000)
        compose.waitForIdle()

        val px = with(compose.density) { 120.dp.toPx() }
        val left = with(compose.density) { (48.dp * 2).toPx() }
        assertEquals(left, state.left.value, 1f)
        assertEquals(px, state.right.value - state.left.value, 1f)
    }

    @Test
    fun `pill starts flowing at once while the tab still grows`() {
        var selected by mutableIntStateOf(0)
        lateinit var state: TabPillState
        val px = { dp: Float -> with(compose.density) { dp.dp.toPx() } }
        compose.setContent {
            state = rememberTabPillState()
            val widths = List(3) { if (it == selected) 120.dp else 48.dp }
            var x = 0f
            val spans = widths.map { w -> val l = x; x += px(w.value); l to x }
            AnimateTabPill(state, selected, spans = spans)
            Row(Modifier.tabPill(state, Color.Green)) {
                repeat(3) { index ->
                    val width by animateDpAsState(widths[index], Motion.spatial(), label = "w")
                    Box(Modifier.width(width).height(48.dp).tabPillAnchor(state, index))
                }
            }
        }
        compose.waitForIdle()
        compose.mainClock.autoAdvance = false
        selected = 2
        Snapshot.sendApplyNotifications()
        val trace = StringBuilder()
        repeat(8) { compose.mainClock.advanceTimeBy(16); trace.append(state.left.value.toInt()).append(' ') }

        // Before the fix the flow restarted on every frame of the growing tab and the pill had not moved yet.
        assertTrue("pill still at the old tab: $trace", state.left.value > px(48f))
        compose.mainClock.advanceTimeBy(3_000)
        assertEquals(px(96f), state.left.value, 1f)
        assertEquals(px(216f), state.right.value, 1f)
    }
}
