package app.coshell.shortcuts

import app.coshell.core.model.TransactionType
import app.coshell.templates.TemplateItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShortcutLinkTest {
    @Test
    fun `a type opens a new operation of it`() {
        assertEquals(ShortcutLink.NewOperation(TransactionType.INCOME), ShortcutLink.of("INCOME", null))
        assertEquals(ShortcutLink.NewOperation(TransactionType.TRANSFER), ShortcutLink.of("TRANSFER", ""))
    }

    @Test
    fun `a template wins over a type`() {
        assertEquals(ShortcutLink.FromTemplate("t1"), ShortcutLink.of("EXPENSE", "t1"))
    }

    @Test
    fun `unknown or missing values lead nowhere`() {
        assertNull(ShortcutLink.of(null, null))
        assertNull(ShortcutLink.of("SPLIT", null))
        assertNull(ShortcutLink.of("expense", " "))
    }

    private fun item(id: String, amount: String = "") = TemplateItem(id = id, name = "T$id", type = TransactionType.EXPENSE, accountName = "A", amount = amount)

    @Test
    fun `the two most used templates become shortcuts`() {
        val items = listOf(item("a"), item("b"), item("c"), item("d"))
        val picked = LauncherShortcuts.pick(items, mapOf("a" to 1, "b" to 5, "c" to 3))
        assertEquals(listOf("b", "c"), picked.map { it.id })
    }

    @Test
    fun `templates never used are not shortcuts`() {
        assertEquals(emptyList<TemplateItem>(), LauncherShortcuts.pick(listOf(item("a")), emptyMap()))
    }

    @Test
    fun `the label carries the amount when the template has one`() {
        assertEquals("Ta · 3 500 ARS", LauncherShortcuts.label(item("a", "3 500 ARS"), "%1\$s · %2\$s"))
        assertEquals("Tb", LauncherShortcuts.label(item("b"), "%1\$s · %2\$s"))
    }
}
