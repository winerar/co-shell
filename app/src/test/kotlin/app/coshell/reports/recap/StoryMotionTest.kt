package app.coshell.reports.recap

import org.junit.Assert.assertEquals
import org.junit.Test

class StoryMotionTest {
    @Test
    fun `going on slides forward and going back slides back`() {
        assertEquals(1, storyDirection(from = 1, to = 2, last = 5))
        assertEquals(-1, storyDirection(from = 3, to = 2, last = 5))
    }

    @Test
    fun `the loop from the last story to the first goes on`() {
        assertEquals(1, storyDirection(from = 5, to = 0, last = 5))
    }

    @Test
    fun `a single story never goes back`() {
        assertEquals(1, storyDirection(from = 0, to = 0, last = 0))
    }
}
