package app.coshell.core.domain.settings

import app.coshell.core.domain.settings.ReportCard.CAPITAL
import app.coshell.core.domain.settings.ReportCard.MEMBERS
import app.coshell.core.domain.settings.ReportCard.MONTHS
import app.coshell.core.domain.settings.ReportCard.SPENDING
import app.coshell.core.domain.settings.ReportCard.UNUSUAL
import kotlin.test.Test
import kotlin.test.assertEquals

class ReportLayoutTest {
    @Test
    fun `default shows every card in the old feed order`() {
        assertEquals(ReportCard.entries, ReportLayout().visible)
        assertEquals(emptyList(), ReportLayout().hiddenCards)
    }

    @Test
    fun `a hidden card leaves the feed and comes back at the end`() {
        val hidden = ReportLayout().withShown(UNUSUAL, false)
        assertEquals(listOf(UNUSUAL), hidden.hiddenCards)
        assertEquals(ReportCard.entries - UNUSUAL, hidden.visible)
        val back = hidden.withShown(UNUSUAL, true)
        assertEquals(ReportCard.entries - UNUSUAL + UNUSUAL, back.visible)
        assertEquals(emptySet(), back.hidden)
    }

    @Test
    fun `move counts places among the shown cards only`() {
        val layout = ReportLayout().withShown(UNUSUAL, false)
        val moved = layout.moveShown(MONTHS, 0)
        assertEquals(listOf(MONTHS, SPENDING), moved.visible.take(2))
        assertEquals(listOf(UNUSUAL), moved.hiddenCards)
    }

    @Test
    fun `move out of range or of a hidden card changes nothing`() {
        val layout = ReportLayout().withShown(MEMBERS, false)
        assertEquals(layout, layout.moveShown(MEMBERS, 0))
        assertEquals(layout, layout.moveShown(SPENDING, 99))
    }

    @Test
    fun `encode and parse keep order and hidden`() {
        val layout = ReportLayout().moveShown(CAPITAL, 0).withShown(SPENDING, false)
        assertEquals(layout, ReportLayout.parse(layout.encode()))
    }

    @Test
    fun `parse drops unknown names and adds missing cards at the end`() {
        val parsed = ReportLayout.parse("MONTHS,FUTURE|FUTURE,SPENDING")
        assertEquals(MONTHS, parsed.order.first())
        assertEquals(ReportCard.entries.toSet(), parsed.order.toSet())
        assertEquals(setOf(SPENDING), parsed.hidden)
        assertEquals(ReportLayout(), ReportLayout.parse("  "))
    }
}
