package app.coshell.core.domain.reports

import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReportPeriodTest {
    @Test
    fun `a month follows the family's start day`() {
        val p = ReportPeriod.month(YearMonth(2026, 11), startDay = 10)

        assertEquals(LocalDate(2026, 11, 10), p.start)
        assertEquals(LocalDate(2026, 12, 10), p.endExclusive)
        assertEquals(30, p.days)
    }

    @Test
    fun `a quarter is the three months that hold the given one`() {
        val p = ReportPeriod.quarter(YearMonth(2026, 11))

        assertEquals(YearMonth(2026, 10), p.firstMonth)
        assertEquals(YearMonth(2026, 12), p.lastMonth)
        assertEquals(4, p.quarter)
        assertEquals(LocalDate(2026, 10, 1), p.start)
        assertEquals(LocalDate(2027, 1, 1), p.endExclusive)
    }

    @Test
    fun `a year runs from the January start day to the next one`() {
        val p = ReportPeriod.year(2026, startDay = 5)

        assertEquals(LocalDate(2026, 1, 5), p.start)
        assertEquals(LocalDate(2027, 1, 5), p.endExclusive)
        assertEquals(YearMonth(2026, 12), p.lastMonth)
    }

    @Test
    fun `arrows step by the period's own kind`() {
        assertEquals(ReportPeriod.month(YearMonth(2026, 10)), ReportPeriod.month(YearMonth(2026, 11)).previous())
        assertEquals(ReportPeriod.month(YearMonth(2027, 1)), ReportPeriod.month(YearMonth(2026, 12)).shift(1))
        assertEquals(ReportPeriod.quarter(YearMonth(2026, 9)), ReportPeriod.quarter(YearMonth(2026, 11)).previous())
        assertEquals(ReportPeriod.quarter(YearMonth(2027, 2)), ReportPeriod.quarter(YearMonth(2026, 11)).shift(1))
        assertEquals(ReportPeriod.year(2025), ReportPeriod.year(2026).previous())
    }

    @Test
    fun `a custom period steps by as many days as it has and accepts the dates either way`() {
        val p = ReportPeriod.custom(LocalDate(2026, 11, 10), LocalDate(2026, 11, 1))

        assertEquals(LocalDate(2026, 11, 1), p.start)
        assertEquals(10, p.days)
        assertEquals(ReportPeriod.custom(LocalDate(2026, 10, 22), LocalDate(2026, 10, 31)), p.previous())
    }

    @Test
    fun `columns - six months to a month or quarter, twelve for a year, the touched months of a custom one`() {
        assertEquals(
            (6..11).map { YearMonth(2026, it) },
            ReportPeriod.month(YearMonth(2026, 11)).columnMonths().map { it.month },
        )
        assertEquals(
            (7..12).map { YearMonth(2026, it) },
            ReportPeriod.quarter(YearMonth(2026, 11)).columnMonths().map { it.month },
        )
        assertEquals(12, ReportPeriod.year(2026).columnMonths().size)
        assertEquals(
            listOf(YearMonth(2026, 9), YearMonth(2026, 10)),
            ReportPeriod.custom(LocalDate(2026, 9, 20), LocalDate(2026, 10, 5)).columnMonths().map { it.month },
        )
        assertEquals(12, ReportPeriod.custom(LocalDate(2024, 1, 1), LocalDate(2026, 10, 5)).columnMonths().size)
    }

    @Test
    fun `days passed and running`() {
        val p = ReportPeriod.month(YearMonth(2026, 11))

        assertEquals(11, p.daysPassed(LocalDate(2026, 11, 11)))
        assertTrue(p.isRunning(LocalDate(2026, 11, 30)))
        assertFalse(p.isRunning(LocalDate(2026, 12, 1)))
        assertEquals(30, p.daysPassed(LocalDate(2027, 1, 1)))
    }
}
