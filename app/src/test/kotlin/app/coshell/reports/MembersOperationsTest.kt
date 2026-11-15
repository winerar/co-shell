package app.coshell.reports

import app.coshell.core.domain.reports.ReportPeriod
import app.coshell.core.domain.search.PeriodRange
import app.coshell.transactions.list.MemberPreset
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MembersOperationsTest {
    private fun state(period: ReportPeriod?) = ReportsUiState(period = period)

    @Test
    fun `a calendar month goes as a month`() {
        assertEquals(
            MemberPreset("m2", LocalDate(2026, 10, 1), LocalDate(2026, 11, 1), PeriodRange.Step.MONTH),
            state(ReportPeriod.month(YearMonth(2026, 10))).membersOperations("m2"),
        )
    }

    @Test
    fun `quarter and year keep their step`() {
        assertEquals(PeriodRange.Step.THREE_MONTHS, state(ReportPeriod.quarter(YearMonth(2026, 10))).membersOperations(null)?.step)
        assertEquals(PeriodRange.Step.YEAR, state(ReportPeriod.year(2026)).membersOperations(null)?.step)
    }

    @Test
    fun `a family month from the 5th goes as its days`() {
        val preset = state(ReportPeriod.month(YearMonth(2026, 10), startDay = 5)).membersOperations(null)
        assertEquals(PeriodRange.Step.DAYS, preset?.step)
        assertEquals(LocalDate(2026, 10, 5), preset?.start)
        assertEquals(LocalDate(2026, 11, 5), preset?.endExclusive)
    }

    @Test
    fun `custom days stay days`() {
        val preset = state(ReportPeriod.custom(LocalDate(2026, 10, 1), LocalDate(2026, 10, 20))).membersOperations("m2")
        assertEquals(PeriodRange.Step.DAYS, preset?.step)
        assertEquals(LocalDate(2026, 10, 21), preset?.endExclusive)
    }

    @Test
    fun `nothing while the report loads`() {
        assertNull(state(null).membersOperations("m2"))
    }
}
