package app.coshell.ui.form

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChoiceSearchTest {
    private val options = listOf(ChoiceOption(1, "Galicia", "ARS"), ChoiceOption(2, "Binance", "USDT"), ChoiceOption(3, "Ameria", "AMD"))

    @Test
    fun `search appears from nine options`() {
        assertFalse(ChoiceSearch.shows(8))
        assertTrue(ChoiceSearch.shows(9))
    }

    @Test
    fun `blank query keeps everything`() = assertEquals(options, ChoiceSearch.filter(options, "  "))

    @Test
    fun `finds by label ignoring case`() = assertEquals(listOf(2), ChoiceSearch.filter(options, "bin").map { it.value })

    @Test
    fun `finds by subtitle`() = assertEquals(listOf(2), ChoiceSearch.filter(options, "usdt").map { it.value })

    @Test
    fun `nothing found gives an empty list`() = assertTrue(ChoiceSearch.filter(options, "zzz").isEmpty())
}
