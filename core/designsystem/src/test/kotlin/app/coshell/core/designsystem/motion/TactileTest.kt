package app.coshell.core.designsystem.motion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TactileTest {
    @Test
    fun centreDoesNotTilt() {
        assertEquals(0f to 0f, tiltFor(50f, 50f, 100f, 100f, 10f))
    }

    @Test
    fun touchedSideGoesDown() {
        val (x, y) = tiltFor(100f, 0f, 100f, 100f, 10f) // top-right corner of a square
        assertEquals(10f, x, 0.01f)
        assertEquals(10f, y, 0.01f)
        val (bx, by) = tiltFor(0f, 100f, 100f, 100f, 10f)
        assertEquals(-10f, bx, 0.01f)
        assertEquals(-10f, by, 0.01f)
    }

    @Test
    fun wideElementsBarelyTiltSideways() {
        val (_, y) = tiltFor(400f, 25f, 400f, 50f, 10f) // right end of a wide button
        assertTrue("side tilt $y must be damped", y in 0f..4f)
    }

    @Test
    fun noTiltTierOrEmptySize() {
        assertEquals(0f to 0f, tiltFor(10f, 10f, 100f, 100f, 0f))
        assertEquals(0f to 0f, tiltFor(10f, 10f, 0f, 0f, 10f))
    }

    @Test
    fun biggerSurfacesMoveLess() {
        val tiers = listOf(Tactile.Primary, Tactile.Control, Tactile.Surface, Tactile.Row)
        tiers.zipWithNext().forEach { (a, b) ->
            assertTrue(a.pressedScale <= b.pressedScale)
            assertTrue(a.maxTiltDeg >= b.maxTiltDeg)
        }
    }
}
