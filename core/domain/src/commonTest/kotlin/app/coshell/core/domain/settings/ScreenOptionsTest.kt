package app.coshell.core.domain.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class ScreenOptionsTest {
    @Test
    fun `defaults keep what the app showed before the options existed`() {
        val o = ScreenOptions()
        assertEquals(listOf(true, true, true), listOf(o.opsShowAccount, o.opsShowTags, o.opsShowNote))
        assertEquals(listOf(false, false), listOf(o.accountsByCurrency, o.accountsShowArchived))
        assertEquals(ReportsStart.MONTH, o.reportsStart)
        assertEquals(true, o.reportsCompare)
        assertEquals(true, o.haptics)
    }

    @Test
    fun `an unknown or missing reports start is the month`() {
        assertEquals(ReportsStart.YEAR, ReportsStart.parse("YEAR"))
        assertEquals(ReportsStart.MONTH, ReportsStart.parse("DECADE"))
        assertEquals(ReportsStart.MONTH, ReportsStart.parse(null))
    }

    @Test
    fun `the store changes one option and keeps the others`() = runTest {
        val store = InMemoryScreenOptions()
        store.update { it.copy(opsShowNote = false) }
        val o = store.options.first()
        assertEquals(false, o.opsShowNote)
        assertEquals(true, o.opsShowTags)
    }
}
