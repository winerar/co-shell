package app.coshell.templates.list

import app.coshell.core.model.TransactionType
import app.coshell.templates.TemplateCatalog
import app.coshell.templates.TemplateItem
import app.coshell.templates.strip.STRIP_LIMIT
import app.coshell.templates.strip.stripOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TemplateListTest {
    private fun item(id: String, category: String, type: TransactionType = TransactionType.EXPENSE, account: String = "Galicia") =
        TemplateItem(id = id, name = id, type = type, accountName = account, categoryName = category)

    private val items = listOf(
        item("Свет", "Коммуналка"),
        item("Аренда", "Аренда", account = "Наличные"),
        item("Газ", "Коммуналка"),
        item("Зарплата", "Работа", TransactionType.INCOME),
        item("Маме", "", TransactionType.TRANSFER),
    )
    private val snapshot = TemplateCatalog.Snapshot(items, counts = mapOf("Газ" to 5, "Маме" to 2))

    @Test
    fun `sections go by type, inside — by category then name`() {
        val state = templateList(snapshot, query = "", filter = null)
        assertEquals(listOf(TransactionType.EXPENSE, TransactionType.INCOME, TransactionType.TRANSFER), state.sections.map { it.type })
        assertEquals(listOf("Аренда", "Газ", "Свет"), state.sections.first().items.map { it.id })
        assertEquals(5, state.total)
        assertEquals(mapOf(TransactionType.EXPENSE to 3, TransactionType.INCOME to 1, TransactionType.TRANSFER to 1), state.counts)
    }

    @Test
    fun `frequent shows only used templates, most used first`() {
        assertEquals(listOf("Газ", "Маме"), templateList(snapshot, "", null).frequent.map { it.id })
        assertEquals(listOf("Газ"), templateList(snapshot, "", TransactionType.EXPENSE).frequent.map { it.id })
        assertTrue(templateList(snapshot, "св", null).frequent.isEmpty())
    }

    @Test
    fun `search looks at the name, the category and the account`() {
        fun found(query: String) = templateList(snapshot, query, null).sections.flatMap { it.items }.map { it.id }
        assertEquals(listOf("Свет"), found(" све "))
        assertEquals(listOf("Газ", "Свет"), found("КОММУН"))
        assertEquals(listOf("Аренда"), found("налич"))
        val none = templateList(snapshot, "zzz", null)
        assertTrue(none.nothingFound)
        assertEquals(5, none.total)
    }

    @Test
    fun `the type filter narrows the list and forgets a type without templates`() {
        val income = templateList(snapshot, "", TransactionType.INCOME)
        assertEquals(listOf("Зарплата"), income.sections.single().items.map { it.id })

        val noIncome = TemplateCatalog.Snapshot(items.filter { it.type != TransactionType.INCOME })
        val state = templateList(noIncome, "", TransactionType.INCOME)
        assertNull(state.filter)
        assertEquals(4, state.sections.sumOf { it.items.size })
    }

    @Test
    fun `no templates at all is the empty state, not «nothing found»`() {
        val state = templateList(TemplateCatalog.Snapshot(), "", null)
        assertTrue(state.isEmpty)
        assertFalse(state.nothingFound)
    }

    @Test
    fun `the strip shows the most used first and is capped`() {
        val many = (1..12).map { item("t$it", "c") }
        val strip = stripOf(TemplateCatalog.Snapshot(many, counts = mapOf("t7" to 3, "t2" to 1)))
        assertEquals(12, strip.total)
        assertEquals(STRIP_LIMIT, strip.items.size)
        assertEquals(listOf("t7", "t2", "t1", "t3"), strip.items.take(4).map { it.id })
    }
}
