package app.coshell.widget

import app.coshell.core.model.TransactionType
import app.coshell.templates.TemplateItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WidgetLayoutsTest {
    private fun item(id: String, amount: String = "") = TemplateItem(id = id, name = "T$id", type = TransactionType.EXPENSE, accountName = "A", amount = amount)

    @Test
    fun `the quick widget shows icons only on 2x1 and words from 3x1`() {
        assertEquals(QuickMode.ICONS, WidgetLayouts.quickMode(110))
        assertEquals(QuickMode.ICONS, WidgetLayouts.quickMode(146))
        assertEquals(QuickMode.WORDS, WidgetLayouts.quickMode(220))
        assertEquals(QuickMode.WORDS, WidgetLayouts.quickMode(294))
    }

    @Test
    fun `the templates widget is a grid only when two rows fit`() {
        assertFalse(WidgetLayouts.isGrid(74))
        assertFalse(WidgetLayouts.isGrid(109))
        assertTrue(WidgetLayouts.isGrid(110))
        assertTrue(WidgetLayouts.isGrid(160))
    }

    @Test
    fun `three templates at most in the app order`() {
        val items = listOf(item("1"), item("2"), item("3"), item("4"))
        assertEquals(listOf("1", "2", "3"), WidgetLayouts.chips(items).map { it.id })
        assertEquals(listOf("1"), WidgetLayouts.chips(listOf(item("1"))).map { it.id })
        assertTrue(WidgetLayouts.chips(emptyList()).isEmpty())
    }

    @Test
    fun `a chip shows the amount or asks for it`() {
        assertEquals("3 500 ARS", WidgetLayouts.amountLine(item("1", "3 500 ARS"), "ask"))
        assertEquals("ask", WidgetLayouts.amountLine(item("2"), "ask"))
    }
}
