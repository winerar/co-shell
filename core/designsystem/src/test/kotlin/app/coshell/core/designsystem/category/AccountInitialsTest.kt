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
        assertEquals(Color(0xFF111611), inkOn(Color(0xFFFFDD2D)))
    }
}
