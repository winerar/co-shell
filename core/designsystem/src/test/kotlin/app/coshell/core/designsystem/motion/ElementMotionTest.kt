package app.coshell.core.designsystem.motion

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import app.coshell.core.designsystem.components.timeLeftShare
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ElementMotionTest {
    @Test
    fun staggerStepsAndCaps() {
        assertEquals(0L, staggerDelayMs(0))
        assertEquals(90L, staggerDelayMs(3))
        // A long screen does not wait for its 20th block.
        assertEquals(staggerDelayMs(8), staggerDelayMs(20))
        assertEquals(0L, staggerDelayMs(-1))
    }

    @Test
    fun shakeVelocityGrowsWithAmplitude() {
        assertEquals(0f, shakeVelocity(0f))
        assertTrue(shakeVelocity(24f) > shakeVelocity(12f))
    }

    @Test
    fun revealCircleCoversTheWholeScreen() {
        val size = Size(1000f, 2000f)
        // From a corner: the far corner is the diagonal away.
        assertEquals(hypot(1000f, 2000f), revealRadius(Offset(0f, 0f), size), 0.01f)
        // From the bottom centre: the top corners.
        assertEquals(hypot(500f, 2000f), revealRadius(Offset(500f, 2000f), size), 0.01f)
    }

    @Test
    fun snackbarTimeLineShrinksEvenlyAndStops() {
        assertEquals(1f, timeLeftShare(0, 10_000), 0f)
        assertEquals(0.5f, timeLeftShare(5_000, 10_000), 0.0001f)
        assertEquals(0f, timeLeftShare(12_000, 10_000), 0f)
    }

    private fun hypot(a: Float, b: Float) = kotlin.math.hypot(a, b)
}
