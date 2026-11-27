package app.coshell.hints.gestures

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** The cheat sheet of gestures (#797) keeps the groups of its board `Gest797` and no line twice. */
class GesturesScreenTest {
    @Test
    fun `five groups in the order of the board`() {
        assertEquals(5, GestureGroups.size)
        assertEquals(GestureGroups.size, GestureGroups.map { it.title }.distinct().size)
    }

    @Test
    fun `every gesture has its own words and every group has gestures`() {
        val all = GestureGroups.flatMap { it.gestures }
        assertTrue(GestureGroups.all { it.gestures.isNotEmpty() })
        assertEquals(all.size, all.map { it.text }.distinct().size)
        assertEquals(12, all.size)
    }
}
