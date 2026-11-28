package app.coshell.widget

import app.coshell.core.model.TransactionType
import app.coshell.templates.TemplateItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

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
    fun `as many templates as fit the width, in the app order`() {
        val items = (1..6).map { item("$it") }
        assertEquals(listOf("1"), WidgetLayouts.chips(items, 110).map { it.id })
        assertEquals(listOf("1"), WidgetLayouts.chips(items, 179).map { it.id })
        assertEquals(listOf("1", "2"), WidgetLayouts.chips(items, 180).map { it.id })
        assertEquals(listOf("1", "2", "3"), WidgetLayouts.chips(items, 250).map { it.id })
        assertEquals(listOf("1", "2", "3", "4"), WidgetLayouts.chips(items, 320).map { it.id })
        assertEquals(WidgetLayouts.CHIPS, WidgetLayouts.chips(items, 500).size)
        assertEquals(listOf("1"), WidgetLayouts.chips(listOf(item("1")), 320).map { it.id })
        assertTrue(WidgetLayouts.chips(emptyList(), 320).isEmpty())
    }

    @Test
    fun `size ranges start at each width where the count changes`() {
        assertEquals(listOf(1, 2, 3, 4), WidgetLayouts.chipWidthsDp.map { WidgetLayouts.chipCount(it) })
        assertEquals(WidgetLayouts.QUICK_MIN_WIDTH_DP, WidgetLayouts.chipWidthsDp.first())
    }

    @Test
    fun `the grid puts the larger half on top`() {
        assertEquals(1, WidgetLayouts.firstRow(2))
        assertEquals(2, WidgetLayouts.firstRow(3))
        assertEquals(2, WidgetLayouts.firstRow(4))
        assertEquals(3, WidgetLayouts.firstRow(5))
    }

    @Test
    fun `a chip shows the amount or asks for it`() {
        assertEquals("3 500 ARS", WidgetLayouts.amountLine(item("1", "3 500 ARS"), "ask"))
        assertEquals("ask", WidgetLayouts.amountLine(item("2"), "ask"))
    }

    @Test
    fun `both widgets ask the launcher for a single row and the coded widths`() {
        // #821: minHeight 74 dp made launchers place 2x2 and 4x2 instead of one row.
        for ((file, width) in listOf("widget_quick_info.xml" to WidgetLayouts.QUICK_MIN_WIDTH_DP, "widget_templates_info.xml" to WidgetLayouts.TEMPLATES_MIN_WIDTH_DP)) {
            val xml = File("src/main/res/xml/$file").readText()
            assertTrue(file, xml.contains("android:minHeight=\"${WidgetLayouts.ONE_ROW_DP}dp\""))
            assertTrue(file, xml.contains("android:minResizeHeight=\"${WidgetLayouts.ONE_ROW_DP}dp\""))
            assertTrue(file, xml.contains("android:minWidth=\"${width}dp\""))
            assertTrue(file, xml.contains("android:targetCellHeight=\"1\""))
            assertTrue(file, xml.contains("android:minResizeWidth=\"${WidgetLayouts.QUICK_MIN_WIDTH_DP}dp\""))
        }
    }
}
