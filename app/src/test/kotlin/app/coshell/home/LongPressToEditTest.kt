package app.coshell.home

import app.coshell.ui.edit.longPressToEdit

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.test.down
import androidx.compose.ui.test.up
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** «Обзор» started editing by itself: a scroll over a card was taken for a long press. Only a still finger counts. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp", application = Application::class)
class LongPressToEditTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private var started = 0

    private fun setList() = compose.setContent {
        LazyColumn(Modifier.fillMaxSize().testTag("list")) {
            items((1..40).toList()) {
                Box(Modifier.fillMaxWidth().height(120.dp).longPressToEdit(enabled = true) { started++ })
            }
        }
    }

    @Test
    fun `scrolling the list never starts the editing`() {
        setList()
        compose.onNodeWithTag("list").performTouchInput { swipeUp() }
        compose.mainClock.advanceTimeBy(2_000)
        assertEquals(0, started)
    }

    @Test
    fun `a quick tap does not start the editing`() {
        setList()
        compose.onNodeWithTag("list").performTouchInput { down(center); up() }
        compose.mainClock.advanceTimeBy(2_000)
        assertEquals(0, started)
    }

    @Test
    fun `a still finger held on a card starts the editing`() {
        setList()
        compose.onNodeWithTag("list").performTouchInput { down(center) }
        compose.mainClock.advanceTimeBy(2_000)
        assertEquals(1, started)
    }
}
