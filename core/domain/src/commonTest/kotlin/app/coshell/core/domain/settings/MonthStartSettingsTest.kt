package app.coshell.core.domain.settings

import kotlin.test.Test
import kotlin.test.assertEquals

class MonthStartSettingsTest {
    @Test
    fun `stored day is read back, anything else is the 1st`() {
        assertEquals(10, MonthStartSettings.parse("10"))
        assertEquals(31, MonthStartSettings.parse(" 31 "))
        assertEquals(1, MonthStartSettings.parse(null))
        assertEquals(1, MonthStartSettings.parse("0"))
        assertEquals(1, MonthStartSettings.parse("32"))
        assertEquals(1, MonthStartSettings.parse("payday"))
    }
}
