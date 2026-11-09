package app.coshell.navigation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import app.coshell.core.designsystem.motion.PressOriginStore
import app.coshell.core.designsystem.motion.PressPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScreenOriginsTest {
    private val presses = PressOriginStore { 0L }
    private val origins = ScreenOrigins(presses).apply { hostBounds = Rect(0f, 0f, 100f, 200f) }

    @Test
    fun screenGrowsFromTapAndShrinksBackIntoIt() {
        presses.record(PressPoint(Offset(25f, 150f)))
        val expected = TransformOrigin(0.25f, 0.75f)
        assertEquals(expected, origins.forEnter("settings"))
        assertEquals(expected, origins.forEnter("settings")) // asked again in the same transition
        assertEquals(expected, origins.forPop("settings"))
    }

    @Test
    fun withoutTapScreenHasNoOriginAndDoesNotTakeLaterTap() {
        assertNull(origins.forEnter("rates"))
        presses.record(PressPoint(Offset(1f, 1f)))
        assertNull(origins.forEnter("rates")) // decided when it opened
        assertNull(origins.forPop("unknown"))
    }
}
