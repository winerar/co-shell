package app.coshell.core.designsystem.amount

import org.junit.Assert.assertEquals
import org.junit.Test

class HiddenAmountsTest {
    private val nb = '\u00A0'
    private val narrow = '\u202F'

    @Test
    fun `hides the number and keeps the code`() {
        assertEquals("••••${nb}USD", maskAmounts("12${narrow}480,00${nb}USD"))
        assertEquals("••••${nb}ARS", maskAmounts("−32${narrow}000${nb}ARS"))
        assertEquals("••••${nb}USDT", maskAmounts("+13,10${nb}USDT"))
        assertEquals("•••• ARS", maskAmounts("-4 500,00 ARS"))
        assertEquals("Курс на 9 ноября", maskAmounts("Курс на 9 ноября"))
    }

    @Test
    fun `hides every sum inside a sentence`() {
        val text = "потрачено 1${narrow}240${nb}USD из 2${narrow}000${nb}USD · 62 %"
        assertEquals("потрачено ••••${nb}USD из ••••${nb}USD · 62 %", maskAmounts(text))
        assertEquals("≈ ••••${nb}ARS · ••••${nb}AMD", maskAmounts("≈ 15${narrow}974${narrow}400${nb}ARS · 4${narrow}836${narrow}000${nb}AMD"))
    }

    @Test
    fun `hides a bare number whole and leaves plain text alone`() {
        assertEquals(HIDDEN_NUMBER, maskAmounts("1${narrow}240,50"))
        assertEquals("Без категории", maskAmounts("Без категории"))
        assertEquals("", maskAmounts(""))
    }
}
