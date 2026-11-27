package app.coshell.core.domain.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DayOfWeek
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

    @Test
    fun `the week starts as chosen and keeps all seven days`() {
        val mon = WeekStart.MONDAY.days(DayOfWeek.SUNDAY)
        assertEquals(DayOfWeek.entries, mon)
        val sun = WeekStart.SUNDAY.days(DayOfWeek.MONDAY)
        assertEquals(listOf(DayOfWeek.SUNDAY, DayOfWeek.MONDAY), sun.take(2))
        assertEquals(DayOfWeek.SATURDAY, sun.last())
        assertEquals(DayOfWeek.SATURDAY, WeekStart.SYSTEM.days(DayOfWeek.SATURDAY).first())
        assertEquals(7, WeekStart.SYSTEM.days(DayOfWeek.WEDNESDAY).distinct().size)
        assertEquals(WeekStart.SYSTEM, WeekStart.parse("???"))
    }

    @Test
    fun `the two tiles are never the same one`() {
        val o = ScreenOptions()
        assertEquals(HomeTile.SPENDING to HomeTile.BUDGET, o.tileFirst to o.tileSecond)
        val swapped = o.withFirst(HomeTile.BUDGET)
        assertEquals(HomeTile.BUDGET to HomeTile.SPENDING, swapped.tileFirst to swapped.tileSecond)
        val third = o.withSecond(HomeTile.UNCATEGORIZED)
        assertEquals(HomeTile.SPENDING to HomeTile.UNCATEGORIZED, third.tileFirst to third.tileSecond)
        assertEquals(HomeTile.UNCATEGORIZED to HomeTile.SPENDING, third.withSecond(HomeTile.SPENDING).let { it.tileFirst to it.tileSecond })
    }

    @Test
    fun `bar tabs swap instead of repeating`() {
        val o = ScreenOptions()
        assertEquals(BarTab.TRANSACTIONS to BarTab.REPORTS, o.barSecond to o.barThird)
        val swapped = o.withBarSecond(BarTab.REPORTS)
        assertEquals(BarTab.REPORTS to BarTab.TRANSACTIONS, swapped.barSecond to swapped.barThird)
        val accounts = o.withBarThird(BarTab.ACCOUNTS)
        assertEquals(BarTab.TRANSACTIONS to BarTab.ACCOUNTS, accounts.barSecond to accounts.barThird)
        assertEquals(BarTab.ACCOUNTS to BarTab.TRANSACTIONS, accounts.withBarSecond(BarTab.ACCOUNTS).let { it.barSecond to it.barThird })
    }

    @Test
    fun `a stored bar tab reads back and an unknown one falls back`() {
        assertEquals(BarTab.GOALS, BarTab.parse("GOALS", BarTab.REPORTS))
        assertEquals(BarTab.REPORTS, BarTab.parse("CALENDAR", BarTab.REPORTS))
        assertEquals(BarTab.TRANSACTIONS, BarTab.parse(null, BarTab.TRANSACTIONS))
    }
}
