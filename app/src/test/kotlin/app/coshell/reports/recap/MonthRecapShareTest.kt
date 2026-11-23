package app.coshell.reports.recap

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import app.coshell.core.designsystem.theme.CoShellTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** «Поделиться» on the recap (#554): the icon draws the story as a 4:5 picture once and hands it over; no cards — no icon. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp-xhdpi", application = Application::class)
class MonthRecapShareTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `tap on share hands over one picture of the story`() {
        val pictures = mutableListOf<androidx.compose.ui.graphics.ImageBitmap>()
        compose.setContent { CoShellTheme { MonthRecapScreen(MonthRecapSamples.state, onShare = { pictures += it }) } }
        compose.mainClock.autoAdvance = false
        compose.onNodeWithContentDescription("Поделиться итогами").performClick()
        compose.mainClock.advanceTimeByFrame()
        compose.onRoot().captureToImage() // Robolectric draws only on demand: the picture is recorded in that pass
        compose.mainClock.advanceTimeBy(3_000)
        compose.waitForIdle()
        assertEquals(1, pictures.size)
        assertEquals(1080, pictures.single().width)
        assertEquals(1350, pictures.single().height)
    }

    @Test
    fun `no stories - no share icon`() {
        compose.setContent { CoShellTheme { MonthRecapScreen(MonthRecapSamples.state.copy(cards = emptyList())) } }
        assertTrue(compose.onAllNodesWithShare().isEmpty())
        assertNotNull(compose)
    }

    private fun androidx.compose.ui.test.junit4.AndroidComposeTestRule<*, *>.onAllNodesWithShare() =
        onAllNodes(androidx.compose.ui.test.hasContentDescription("Поделиться итогами")).fetchSemanticsNodes()
}
