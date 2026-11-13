package app.coshell.ui.form

import org.junit.Assert.assertEquals
import org.junit.Test

class IconColorStripsTest {
    private val keys = listOf("a", "b", "c", "d", "e", "f", "g", "h")

    @Test
    fun `strip shows the first six icons when nothing or a front icon is chosen`() {
        assertEquals(keys.take(6), stripIcons(keys, null))
        assertEquals(keys.take(6), stripIcons(keys, "c"))
    }

    @Test
    fun `a chosen icon further down takes the last place`() {
        assertEquals(listOf("a", "b", "c", "d", "e", "h"), stripIcons(keys, "h"))
    }

    @Test
    fun `an unknown icon key leaves the strip as is`() {
        assertEquals(keys.take(6), stripIcons(keys, "zzz"))
    }

    @Test
    fun `a chosen colour further down takes the last place`() {
        val colors = (1L..12L).toList()
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L), stripColors(colors, 3L, 8))
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L, 6L, 7L, 11L), stripColors(colors, 11L, 8))
        assertEquals(listOf(1L, 2L, 3L), stripColors(colors, 99L, 3))
    }
}
