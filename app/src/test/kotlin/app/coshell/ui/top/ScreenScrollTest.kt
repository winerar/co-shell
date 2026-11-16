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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import app.coshell.core.designsystem.theme.CoShellTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** #394 Б: an inner screen's header learns that its list scrolled under it from the destination's [ScreenScrollHost]. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp", application = Application::class)
class ScreenScrollTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private var seen: TabScroll? = null

    @Test
    fun `scrolling the screen's list marks its header scrolled and back at the top unmarks it`() {
        compose.setContent {
            CoShellTheme(darkTheme = true) {
                ScreenScrollHost {
                    seen = LocalScreenScroll.current
                    // A plain screen: Scaffold + list, no scroll wiring of its own.
                    Scaffold(topBar = { ScreenTopBar("Курсы валют", onBack = {}) }) { padding ->
                        LazyColumn(Modifier.fillMaxSize().padding(padding).testTag("list")) {
                            items((1..60).toList()) { Text("USD $it") }
                        }
                    }
                }
            }
        }
        assertEquals(false, seen?.scrolled)
        compose.onNodeWithTag("list").performTouchInput { swipeUp() }
        compose.waitForIdle()
        assertEquals(true, seen?.scrolled)
        compose.onNodeWithTag("list").performTouchInput { repeat(4) { swipeDown() } }
        compose.waitForIdle()
        assertEquals(false, seen?.scrolled)
    }

    @Test
    fun `outside a destination there is no scroll and no line`() {
        compose.setContent { CoShellTheme { seen = LocalScreenScroll.current; ScreenTopBar("Курсы валют", onBack = {}) } }
        assertNull(seen)
    }
}
