package app.coshell.transactions.list

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectionTest {
    @Test
    fun `long press starts the mode with that row`() {
        val s = Selection().longPress("a")
        assertTrue(s.active)
        assertEquals(setOf("a"), s.ids)
        assertEquals("a", s.single)
    }

    @Test
    fun `taps add and remove rows, the last one ends the mode`() {
        val s = Selection().longPress("a").toggle("b")
        assertEquals(2, s.count)
        assertNull(s.single) // «Дублировать» only for one row
        val back = s.toggle("a").toggle("b")
        assertFalse(back.active)
    }

    @Test
    fun `long press while selecting toggles like a tap`() {
        assertEquals(setOf("a", "b"), Selection().longPress("a").longPress("b").ids)
        assertFalse(Selection().longPress("a").longPress("a").active)
    }

    @Test
    fun `rows that left the list drop out`() {
        val s = Selection(setOf("a", "b"))
        assertEquals(setOf("b"), s.keepOnly(setOf("b", "c")).ids)
        assertTrue(s.keepOnly(setOf("a", "b", "c")) === s)
    }
}
