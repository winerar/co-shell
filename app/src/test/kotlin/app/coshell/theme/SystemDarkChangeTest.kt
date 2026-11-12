package app.coshell.theme

import app.coshell.core.domain.settings.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Test

class SystemDarkChangeTest {
    @Test
    fun `no change when the phone's mode is already shown`() {
        ThemeMode.entries.forEach { mode ->
            assertEquals(SystemDarkChange.NONE, systemDarkChange(shown = true, system = true, mode = mode))
            assertEquals(SystemDarkChange.NONE, systemDarkChange(shown = false, system = false, mode = mode))
        }
    }

    @Test
    fun `following the system reveals the switch both ways`() {
        assertEquals(SystemDarkChange.REVEAL, systemDarkChange(shown = false, system = true, mode = ThemeMode.SYSTEM))
        assertEquals(SystemDarkChange.REVEAL, systemDarkChange(shown = true, system = false, mode = ThemeMode.SYSTEM))
    }

    @Test
    fun `own theme or unread setting takes the switch silently`() {
        assertEquals(SystemDarkChange.INSTANT, systemDarkChange(shown = false, system = true, mode = ThemeMode.LIGHT))
        assertEquals(SystemDarkChange.INSTANT, systemDarkChange(shown = true, system = false, mode = ThemeMode.DARK))
        assertEquals(SystemDarkChange.INSTANT, systemDarkChange(shown = false, system = true, mode = null))
    }
}
