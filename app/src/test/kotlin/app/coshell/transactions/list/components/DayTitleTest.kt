package app.coshell.transactions.list.components

import java.time.LocalDate
import java.util.Locale
import org.junit.Test
import org.junit.Assert.assertEquals

class DayTitleTest {
    private val ru = Locale.forLanguageTag("ru")

    @Test
    fun `this year - day, month and weekday`() {
        assertEquals("9 ноября, пн", dayTitle(LocalDate.of(2026, 11, 9), currentYear = 2026, locale = ru))
    }

    @Test
    fun `another year - the year is shown`() {
        assertEquals("31 декабря 2025, ср", dayTitle(LocalDate.of(2025, 12, 31), currentYear = 2026, locale = ru))
    }
}
