package app.coshell.core.designsystem.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Gesture rules of the segment swipe (#173, scheme А): neighbours first, the edges go to the bottom tabs. */
class SegmentSwipeTest {
    @Test
    fun `a drag to the left opens the next segment, to the right the previous`() {
        assertEquals(2, segmentSwipeTarget(current = 1, drag = -100f, threshold = 80f, count = 3))
        assertEquals(0, segmentSwipeTarget(current = 1, drag = 100f, threshold = 80f, count = 3))
    }

    @Test
    fun `a short drag or one past the ends selects nothing`() {
        assertNull(segmentSwipeTarget(current = 1, drag = -79f, threshold = 80f, count = 3))
        assertNull(segmentSwipeTarget(current = 0, drag = 200f, threshold = 80f, count = 3))
        assertNull(segmentSwipeTarget(current = 2, drag = -200f, threshold = 80f, count = 3))
    }

    @Test
    fun `a drag towards a neighbour is claimed`() {
        assertTrue(claimsSegmentSwipe(dx = -20f, dy = 5f, current = 0, count = 2))
        assertTrue(claimsSegmentSwipe(dx = 20f, dy = -5f, current = 1, count = 2))
    }

    @Test
    fun `outward from the first or last segment the drag is left to the bottom tabs`() {
        assertFalse(claimsSegmentSwipe(dx = 20f, dy = 0f, current = 0, count = 2))
        assertFalse(claimsSegmentSwipe(dx = -20f, dy = 0f, current = 1, count = 2))
    }

    @Test
    fun `the track keeps the drag even at the edges`() {
        assertTrue(claimsSegmentSwipe(dx = 20f, dy = 0f, current = 0, count = 2, atEdges = true))
    }

    @Test
    fun `a drag steeper than 30 degrees is a scroll`() {
        assertFalse(claimsSegmentSwipe(dx = -20f, dy = 12f, current = 0, count = 2))
        assertTrue(claimsSegmentSwipe(dx = -20f, dy = 11f, current = 0, count = 2))
        assertFalse(claimsSegmentSwipe(dx = 0f, dy = 20f, current = 0, count = 2, atEdges = true))
    }
}
