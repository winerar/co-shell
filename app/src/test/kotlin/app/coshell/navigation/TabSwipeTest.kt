package app.coshell.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TabSwipeTest {
    @Test
    fun `swipe left opens the next tab, right the previous one`() {
        assertEquals(2, tabSwipeTarget(current = 1, drag = -100f, threshold = 80f, count = 4))
        assertEquals(0, tabSwipeTarget(current = 1, drag = 100f, threshold = 80f, count = 4))
    }

    @Test
    fun `short drag or past the ends does nothing`() {
        assertNull(tabSwipeTarget(current = 1, drag = -79f, threshold = 80f, count = 4))
        assertNull(tabSwipeTarget(current = 0, drag = 200f, threshold = 80f, count = 4))
        assertNull(tabSwipeTarget(current = 3, drag = -200f, threshold = 80f, count = 4))
    }
}
