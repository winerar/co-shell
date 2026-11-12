package app.coshell.core.domain.search

import app.coshell.core.domain.search.PeriodRange.Preset
import app.coshell.core.domain.search.PeriodRange.Step
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

class PeriodRangeTest {
    private val today = LocalDate(2026, 11, 12)

    @Test
    fun `presets are counted from today`() {
        assertEquals(PeriodRange(LocalDate(2026, 11, 1), LocalDate(2026, 12, 1), Step.MONTH), Preset.THIS_MONTH.range(today))
        assertEquals(PeriodRange(LocalDate(2026, 10, 1), LocalDate(2026, 11, 1), Step.MONTH), Preset.LAST_MONTH.range(today))
        assertEquals(PeriodRange(LocalDate(2026, 9, 1), LocalDate(2026, 12, 1), Step.THREE_MONTHS), Preset.THREE_MONTHS.range(today))
        assertEquals(PeriodRange(LocalDate(2026, 1, 1), LocalDate(2027, 1, 1), Step.YEAR), Preset.THIS_YEAR.range(today))
        assertEquals(PeriodRange(LocalDate(2025, 1, 1), LocalDate(2026, 1, 1), Step.YEAR), Preset.LAST_YEAR.range(today))
        assertEquals(LocalDate(2026, 10, 31), Preset.LAST_MONTH.range(today).lastDay)
    }

    @Test
    fun `arrows move by the period's own length`() {
        assertEquals(Preset.LAST_MONTH.range(today), Preset.THIS_MONTH.range(today).shifted(-1))
        assertEquals(PeriodRange(LocalDate(2026, 6, 1), LocalDate(2026, 9, 1), Step.THREE_MONTHS), Preset.THREE_MONTHS.range(today).shifted(-1))
        assertEquals(Preset.LAST_YEAR.range(today), Preset.THIS_YEAR.range(today).shifted(-1))
        assertEquals(PeriodRange.days(LocalDate(2026, 11, 8), LocalDate(2026, 11, 14)), PeriodRange.days(LocalDate(2026, 11, 1), LocalDate(2026, 11, 7)).shifted(1))
        assertEquals(PeriodRange(LocalDate(2027, 1, 1), LocalDate(2027, 2, 1), Step.MONTH), Preset.THIS_MONTH.range(today).shifted(2))
    }

    @Test
    fun `own days include both ends and whole months step by months`() {
        val week = PeriodRange.days(LocalDate(2026, 11, 7), LocalDate(2026, 11, 1))
        assertEquals(LocalDate(2026, 11, 1), week.start)
        assertEquals(LocalDate(2026, 11, 8), week.endExclusive)
        assertEquals(Step.DAYS, week.step)
        assertTrue(LocalDate(2026, 11, 7) in week)
        assertFalse(LocalDate(2026, 11, 8) in week)

        assertEquals(Preset.LAST_MONTH.range(today), PeriodRange.days(LocalDate(2026, 10, 1), LocalDate(2026, 10, 31)))
        assertEquals(Step.THREE_MONTHS, PeriodRange.days(LocalDate(2026, 11, 1), LocalDate(2027, 1, 31)).step)
        assertEquals(Step.YEAR, PeriodRange.days(LocalDate(2026, 1, 1), LocalDate(2026, 12, 31)).step)
        assertEquals(Step.DAYS, PeriodRange.days(LocalDate(2026, 3, 1), LocalDate(2027, 2, 28)).step)
    }

    @Test
    fun `the lit preset is found back, an empty period is refused`() {
        assertEquals(Preset.THREE_MONTHS, Preset.THREE_MONTHS.range(today).presetOn(today))
        assertNull(Preset.THIS_MONTH.range(today).shifted(-2).presetOn(today))
        assertFailsWith<IllegalArgumentException> { PeriodRange(today, today, Step.DAYS) }
    }
}
