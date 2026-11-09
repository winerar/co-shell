package app.coshell.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TabDirectionTest {
    @Test
    fun movingRightAlongTheBarSlidesFromTheRight() {
        assertEquals(1, tabDirection(0, 2))
    }

    @Test
    fun movingLeftSlidesFromTheLeft() {
        assertEquals(-1, tabDirection(3, 1))
    }

    @Test
    fun notATabSwitchWhenEitherSideIsAScreen() {
        assertNull(tabDirection(null, 1))
        assertNull(tabDirection(2, null))
    }
}
