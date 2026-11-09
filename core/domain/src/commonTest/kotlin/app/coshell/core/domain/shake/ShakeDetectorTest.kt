package app.coshell.core.domain.shake

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ShakeDetectorTest {
    private val g = ShakeDetector.STANDARD_GRAVITY

    /** Feeds one sample of [gForce] along X at [millis] and returns the detector's answer. */
    private fun ShakeDetector.at(millis: Long, gForce: Float): Boolean =
        onSample(gForce * g, 0f, 0f, millis * 1_000_000L)

    @Test
    fun `phone at rest is not a shake`() {
        val detector = ShakeDetector()
        val fired = (0L..2_000L step 20).count { detector.onSample(0f, 0f, g, it * 1_000_000L) }
        assertEquals(0, fired)
    }

    @Test
    fun `two strong jolts close together are a shake`() {
        val detector = ShakeDetector()
        assertFalse(detector.at(0, 3f))
        assertTrue(detector.at(250, 3f))
    }

    @Test
    fun `a single bump is ignored`() {
        val detector = ShakeDetector()
        assertFalse(detector.at(0, 4f))
        assertFalse(detector.at(250, 1f))
        assertFalse(detector.at(1_500, 4f))
    }

    @Test
    fun `jolts below the threshold are ignored`() {
        val detector = ShakeDetector()
        assertFalse(detector.at(0, 2.1f))
        assertFalse(detector.at(200, 2.1f))
        assertFalse(detector.at(400, 2.1f))
    }

    @Test
    fun `a moderate shake is enough`() {
        val detector = ShakeDetector()
        assertFalse(detector.at(0, 2.5f))
        assertTrue(detector.at(300, 2.5f))
    }

    @Test
    fun `neighbouring samples of one jolt count once`() {
        val detector = ShakeDetector()
        assertFalse(detector.at(0, 3f))
        assertFalse(detector.at(20, 3f))
        assertFalse(detector.at(40, 3f))
    }

    @Test
    fun `jolts too far apart are not a shake`() {
        val detector = ShakeDetector()
        assertFalse(detector.at(0, 3f))
        assertFalse(detector.at(900, 3f))
        assertTrue(detector.at(1_100, 3f))
    }

    @Test
    fun `a long shake fires once until the cooldown passes`() {
        val detector = ShakeDetector()
        val fired = (0L..1_800L step 200).filter { detector.at(it, 3f) }
        assertEquals(listOf(200L), fired)
        assertFalse(detector.at(2_400, 3f))
        assertTrue(detector.at(2_600, 3f))
    }

    @Test
    fun `reset drops a pending jolt`() {
        val detector = ShakeDetector()
        assertFalse(detector.at(0, 3f))
        detector.reset()
        assertFalse(detector.at(200, 3f))
        assertTrue(detector.at(400, 3f))
    }
}
