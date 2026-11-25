package app.coshell.core.domain.ai

import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlin.time.Instant

/** The family's past entries as hints for the model (#583 «оболочка» п. 3): similar first, then frequent; only live expense categories. */
class HistoryExamplesTest {
    private val ars = CurrencyCode("ARS")
    private val date = Instant.parse("2026-11-13T21:40:00Z")
    private val categories = listOf(
        Category("food", "Продукты", CategoryType.EXPENSE),
        Category("cafe", "Кафе", CategoryType.EXPENSE),
        Category("taxi", "Такси", CategoryType.EXPENSE),
        Category("old", "Старая", CategoryType.EXPENSE, archived = true),
        Category("salary", "Зарплата", CategoryType.INCOME),
    )
    private var n = 0

    private fun tx(category: String?, payee: String? = null, note: String = "", type: TransactionType = TransactionType.EXPENSE) = Transaction(
        id = "t${n++}", type = type, accountId = "a", amount = Money.ofMajor(10, currency = ars), date = date,
        categoryId = category, payee = payee, note = note,
    )

    @Test
    fun `similar names come first, then the most frequent`() {
        val history = listOf(
            tx("cafe", payee = "Starbucks"), tx("cafe", payee = "Starbucks"), tx("cafe", payee = "Starbucks"),
            tx("taxi", payee = "Uber"), tx("taxi", payee = "Uber"),
            tx("food", payee = "Carrefour Express"),
        )
        val picked = HistoryExamples.pick(history, categories, listOf("carrefor 3500"))
        assertEquals(
            listOf(HistoryExample("Carrefour Express", "Продукты"), HistoryExample("Starbucks", "Кафе"), HistoryExample("Uber", "Такси")),
            picked,
        )
    }

    @Test
    fun `one pair per name with its most frequent category, the newest spelling shown`() {
        val history = listOf(tx("food", payee = "DISCO 12"), tx("cafe", payee = "disco"), tx("food", payee = "Disco"))
        assertEquals(listOf(HistoryExample("DISCO 12", "Продукты")), HistoryExamples.pick(history, categories, emptyList()))
    }

    @Test
    fun `income, archived or missing categories and empty names are left out`() {
        val history = listOf(
            tx("salary", payee = "Работа", type = TransactionType.INCOME),
            tx("old", payee = "Киоск"),
            tx("gone", payee = "Аптека"),
            tx("food", payee = null, note = ""),
            tx("food", payee = null, note = "\nКото\nмолоко — 1500"),
        )
        assertEquals(listOf(HistoryExample("Кото", "Продукты")), HistoryExamples.pick(history, categories, emptyList()))
    }

    @Test
    fun `no more than the limit and the separators of the prompt never leak from a name`() {
        val history = (0 until 30).map { tx("food", payee = "Магазин ${'А' + it}|x→y") }
        val picked = HistoryExamples.pick(history, categories, emptyList(), max = 5)
        assertEquals(5, picked.size)
        assertTrue(picked.none { '|' in it.label || '→' in it.label })
        assertEquals("", HistoryExamples.promptLine(emptyList()))
        assertTrue(HistoryExamples.promptLine(picked.take(2)).endsWith("Магазин А x y → Продукты | Магазин Б x y → Продукты"))
    }

    private class Fake(val answer: String) : LanguageModel {
        override val available = true
        var prompt = ""
        override suspend fun complete(system: String, prompt: String): String { this.prompt = prompt; return answer }
    }

    @Test
    fun `the examples reach both prompts and are optional`() = runTest {
        val examples = listOf(HistoryExample("Disco", "Продукты"))
        val payee = Fake("""{"category":"Продукты"}""")
        assertEquals("Продукты", PayeeCategoryAi(payee).suggest("Disco Norte", listOf("Продукты", "Кафе"), examples))
        assertTrue("Disco → Продукты" in payee.prompt)
        PayeeCategoryAi(payee).suggest("Disco Norte", listOf("Продукты", "Кафе"))
        assertTrue("Past entries" !in payee.prompt)

        val notes = Fake("""[{"i":0,"title":"Disco","category":"Продукты"}]""")
        NoteHints(notes).read(listOf("диско 5000"), listOf("Продукты"), examples = examples)
        assertTrue("Disco → Продукты" in notes.prompt)
    }
}
