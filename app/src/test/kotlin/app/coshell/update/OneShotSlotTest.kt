package app.coshell.update

import app.coshell.update.install.OneShotSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OneShotSlotTest {
    @Test
    fun `a waiting value is handed out once`() {
        val slot = OneShotSlot<String>()
        slot.put("confirm")
        assertEquals("confirm", slot.value.value)
        assertEquals("confirm", slot.take())
        assertNull(slot.take())
        assertNull(slot.value.value)
    }

    @Test
    fun `a newer value replaces an untaken one`() {
        val slot = OneShotSlot<String>()
        slot.put("old")
        slot.put("new")
        assertEquals("new", slot.take())
        assertNull(slot.take())
    }

    @Test
    fun `nothing to take when empty`() {
        assertNull(OneShotSlot<String>().take())
    }
}
