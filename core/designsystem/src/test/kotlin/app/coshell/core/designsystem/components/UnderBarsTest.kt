package app.coshell.core.designsystem.components

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The list goes under the floating capsules: as tall as the screen, content padded by their height only at rest. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class UnderBarsTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private val scaffold = PaddingValues(top = 80.dp)

    private fun setList() = compose.setContent {
        CompositionLocalProvider(LocalBottomBarInset provides 100.dp) {
            LazyColumn(Modifier.fillMaxSize().testTag("list"), contentPadding = underBars(scaffold, PaddingValues(bottom = 8.dp))) {
                items((1..40).toList()) { Box(Modifier.fillMaxWidth().height(60.dp).testTag("row$it")) }
            }
        }
    }

    @Test
    fun `the padding adds up and includes the bottom bar`() {
        var result: PaddingValues? = null
        compose.setContent {
            CompositionLocalProvider(LocalBottomBarInset provides 100.dp) { result = underBars(scaffold, PaddingValues(bottom = 8.dp)) }
        }
        assertEquals(80.dp, result!!.calculateTopPadding())
        assertEquals(108.dp, result!!.calculateBottomPadding())
    }

    @Test
    fun `at rest the first row starts below the top capsule, the list itself is not cut`() {
        setList()
        assertEquals(80.dp, compose.onNodeWithTag("row1").getUnclippedBoundsInRoot().top)
        assertEquals(0.dp, compose.onNodeWithTag("list").getUnclippedBoundsInRoot().top)
    }

    @Test
    fun `scrolled, rows go up under the top capsule`() {
        setList()
        compose.onNodeWithTag("list").performTouchInput { swipeUp(startY = height * 0.6f, endY = height * 0.5f) }
        compose.waitForIdle()
        val top = compose.onNodeWithTag("row1").getUnclippedBoundsInRoot().top
        assertTrue("row1 top=$top", top < 80.dp)
    }
}
