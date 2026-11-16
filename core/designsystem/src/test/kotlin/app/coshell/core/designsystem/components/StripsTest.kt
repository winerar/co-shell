package app.coshell.core.designsystem.components

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class StripsTest {
    private val keys = listOf("a", "b", "c", "d", "e", "f", "g", "h")

    @Test
    fun `strip shows the first icons when nothing or a front icon is chosen`() {
        assertEquals(keys.take(6), stripIcons(keys, null, 6))
        assertEquals(keys.take(6), stripIcons(keys, "c", 6))
    }

    @Test
    fun `a chosen icon further down takes the last place`() {
        assertEquals(listOf("a", "b", "c", "d", "e", "h"), stripIcons(keys, "h", 6))
    }

    @Test
    fun `an unknown icon key leaves the strip as is`() {
        assertEquals(keys.take(6), stripIcons(keys, "zzz", 6))
    }

    @Test
    fun `a chosen colour further down takes the last place`() {
        val colors = (1L..12L).toList()
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L), stripColors(colors, 3L, 8))
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L, 6L, 7L, 11L), stripColors(colors, 11L, 8))
        assertEquals(listOf(1L, 2L, 3L), stripColors(colors, 99L, 3))
    }

    @Test
    fun `as many circles fit as the width allows next to the chip`() {
        // 296 dp: chip 44 + 16 = 60, circles of 40 with 8 gaps -> (296 - 60 + 8) / 48 = 5
        assertEquals(5, stripSlots(296.dp, 40.dp))
        assertEquals(4, stripSlots(296.dp, 44.dp))
    }
}
