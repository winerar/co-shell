package app.coshell.core.domain.shake

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FaceDownDetectorTest {
    private val g = ShakeDetector.STANDARD_GRAVITY

    /** Feeds samples every 20 ms from [from] to [to] (inclusive) and returns how many changed the state. */
    private fun FaceDownDetector.hold(from: Long, to: Long, x: Float = 0f, y: Float = 0f, z: Float): Int =
        (from..to step 20).count { onSample(x, y, z, it * 1_000_000L) }

    @Test
    fun `lying screen up is not face down`() {
        val d = FaceDownDetector()
        assertEquals(0, d.hold(0, 2_000, z = g))
        assertFalse(d.isFaceDown)
    }

    @Test
    fun `screen down for a while hides the sums once`() {
        val d = FaceDownDetector()
        assertEquals(0, d.hold(0, 500, z = -g))
        assertEquals(1, d.hold(520, 2_000, z = -g))
        assertTrue(d.isFaceDown)
    }

    @Test
    fun `flipping through the pose in the hand does not hide`() {
        val d = FaceDownDetector()
        d.hold(0, 300, z = -g)
        d.hold(320, 1_000, z = 0f)
        assertFalse(d.isFaceDown)
    }

    @Test
    fun `tilted phone screen down is not face down`() {
        val d = FaceDownDetector()
        d.hold(0, 2_000, x = 5f, z = -8.6f)
        assertFalse(d.isFaceDown)
    }

    @Test
    fun `lifting shows the sums quickly`() {
        val d = FaceDownDetector()
        d.hold(0, 1_000, z = -g)
        assertTrue(d.isFaceDown)
        assertEquals(0, d.hold(1_020, 1_120, z = 2f))
        assertEquals(1, d.hold(1_140, 1_300, z = 2f))
        assertFalse(d.isFaceDown)
    }

    @Test
    fun `small wobble on the table keeps it hidden`() {
        val d = FaceDownDetector()
        d.hold(0, 1_000, z = -g)
        assertEquals(0, d.hold(1_020, 1_400, x = 1f, z = -7f))
        assertTrue(d.isFaceDown)
    }

    @Test
    fun `reset returns to face up`() {
        val d = FaceDownDetector()
        d.hold(0, 1_000, z = -g)
        d.reset()
        assertFalse(d.isFaceDown)
    }
}
