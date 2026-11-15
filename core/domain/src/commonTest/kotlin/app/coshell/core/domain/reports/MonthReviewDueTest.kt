package app.coshell.core.domain.reports

import app.coshell.core.domain.settings.MonthReviewSettings
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MonthReviewDueTest {
    private val october = YearMonth(2026, 10)

    @Test
    fun `the first seven days of the month offer the month before`() {
        assertEquals(october, monthReviewDue(LocalDate(2026, 11, 1), startDay = 1, dismissed = null))
        assertEquals(october, monthReviewDue(LocalDate(2026, 11, 7), startDay = 1, dismissed = null))
        assertNull(monthReviewDue(LocalDate(2026, 11, 8), startDay = 1, dismissed = null))
        assertNull(monthReviewDue(LocalDate(2026, 10, 31), startDay = 1, dismissed = null))
    }

    @Test
    fun `hidden — gone until the next month`() {
        assertNull(monthReviewDue(LocalDate(2026, 11, 2), startDay = 1, dismissed = october))
        assertEquals(YearMonth(2026, 11), monthReviewDue(LocalDate(2026, 12, 1), startDay = 1, dismissed = october))
    }

    @Test
    fun `a month from payday counts its days from that day`() {
        // «November» runs 10 November .. 9 December: from the 10th to the 16th the review is of «October».
        assertEquals(october, monthReviewDue(LocalDate(2026, 11, 10), startDay = 10, dismissed = null))
        assertEquals(october, monthReviewDue(LocalDate(2026, 11, 16), startDay = 10, dismissed = null))
        assertNull(monthReviewDue(LocalDate(2026, 11, 17), startDay = 10, dismissed = null))
        assertNull(monthReviewDue(LocalDate(2026, 11, 5), startDay = 10, dismissed = null))
    }

    @Test
    fun `across the new year`() {
        assertEquals(YearMonth(2026, 12), monthReviewDue(LocalDate(2027, 1, 3), startDay = 1, dismissed = null))
    }

    @Test
    fun `the stored month reads back, a broken one is never hidden`() {
        assertEquals(october, MonthReviewSettings.parse("2026-10"))
        assertNull(MonthReviewSettings.parse("октябрь"))
        assertNull(MonthReviewSettings.parse(null))
    }
}
