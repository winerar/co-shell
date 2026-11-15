package app.coshell.transactions.list

import app.coshell.core.domain.search.PeriodRange
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MemberPresetTest {
    private val october = MemberPreset("m2", LocalDate(2026, 10, 1), LocalDate(2026, 11, 1), PeriodRange.Step.MONTH)

    @Test
    fun `encode and decode give the same preset`() {
        assertEquals(october, MemberPreset.decode(october.encode()))
        val me = october.copy(memberId = null)
        assertEquals(me, MemberPreset.decode(me.encode()))
    }

    @Test
    fun `anything else is not a preset`() {
        assertNull(MemberPreset.decode(null))
        assertNull(MemberPreset.decode(""))
        assertNull(MemberPreset.decode("m2|2026-10-01|2026-11-01"))
        assertNull(MemberPreset.decode("m2|2026-10-01|2026-11-01|WEEK"))
        assertNull(MemberPreset.decode("m2|2026-11-01|2026-10-01|MONTH"))
    }

    @Test
    fun `a member opens their operations in the period and nothing else`() {
        val filter = october.toFilter()
        assertEquals(ListFilter.Who.Member("m2"), filter.who)
        assertEquals(PeriodRange(LocalDate(2026, 10, 1), LocalDate(2026, 11, 1), PeriodRange.Step.MONTH), filter.period)
        assertEquals(ListFilter(who = filter.who, period = filter.period), filter)
    }

    @Test
    fun `no member means me`() {
        assertEquals(ListFilter.Who.Me, october.copy(memberId = null).toFilter().who)
    }
}
