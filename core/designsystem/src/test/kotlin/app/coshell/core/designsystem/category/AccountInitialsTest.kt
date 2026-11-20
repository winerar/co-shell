package app.coshell.core.designsystem.category

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

class AccountInitialsTest {
    @Test
    fun oneWordTakesTwoLetters() = assertEquals("GA", accountInitials("Galicia"))

    @Test
    fun twoWordsTakeFirstLetters() = assertEquals("MP", accountInitials("Mercado Pago"))

    @Test
    fun symbolsAndEmptyNames() {
        assertEquals("1U", accountInitials("$1 · USD"))
        assertEquals("·", accountInitials(""))
        assertEquals("ТБ", accountInitials("Т-Банк"))
    }

    @Test
    fun inkReadsOnFill() {
        assertEquals(Color.White, inkOn(Color(0xFF0A2896)))
        assertEquals(Color(0xFF0B0B0B), inkOn(Color(0xFFFFDD2D)))
        // Mid-tone brand colours (owner 2026-11-20): white read only 3:1 on them, the dark ink 6:1 — the ink flips by contrast.
        assertEquals(Color(0xFF0B0B0B), inkOn(Color(0xFFF26E22)))
        assertEquals(Color(0xFF0B0B0B), inkOn(Color(0xFF00B1EA)))
    }

    @Test
    fun readableInkFlipsOnlyWhenShort() {
        assertEquals(Color.White, readableInk(Color(0xFF0A2896), Color.White))
        assertEquals(Color(0xFF0B0B0B), readableInk(Color(0xFFB8FF2E), Color.White))
        assertEquals(Color.White, readableInk(Color(0xFF1A1A1A), Color(0xFF333333)))
    }
}
