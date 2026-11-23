package app.coshell.core.domain.ai

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** The model's share of quick notes (#586): one request, a category only from the list, doubtful answers dropped. */
class NoteHintsTest {
    private class Fake(override val available: Boolean = true, val answer: String? = null) : LanguageModel {
        var calls = 0
        var lastPrompt = ""
        override suspend fun complete(system: String, prompt: String): String? {
            calls++
            lastPrompt = prompt
            return answer
        }
    }

    private val categories = listOf("Продукты", "Кафе", "Такси")

    @Test
    fun `all lines go in one request and the category must be from the list`() = runTest {
        val model = Fake(answer = """Вот: [{"i":0,"title":"Disco","category":"продукты"},{"i":1,"title":"Лента","category":"Космос"}] готово""")
        val hints = NoteHints(model).read(listOf("диско вчера", "лента"), categories)
        assertEquals(1, model.calls)
        assertEquals(NoteHint("Disco", "Продукты"), hints[0])
        assertEquals(NoteHint("Лента", null), hints[1])
    }

    @Test
    fun `a long note goes in parts and answers land on their own lines`() = runTest {
        val model = Fake(answer = """[{"i":1,"title":"Coto","category":"Продукты"}]""")
        val hints = NoteHints(model).read((0 until 12).map { "line $it" }, categories)
        assertEquals(2, model.calls)
        assertEquals(12, hints.size)
        assertEquals(NoteHint("Coto", "Продукты"), hints[1])
        assertEquals(NoteHint("Coto", "Продукты"), hints[11]) // index 1 of the second part
        assertNull(hints[10])
    }

    @Test
    fun `the model may say a line is not an expense`() = runTest {
        val model = Fake(answer = """[{"i":0,"expense":false},{"i":1,"title":"Coto","category":"Продукты","expense":true}]""")
        val hints = NoteHints(model).read(listOf("тест", "coto"), categories)
        assertEquals(NoteHint(null, null, expense = false), hints[0])
        assertEquals(NoteHint("Coto", "Продукты"), hints[1])
        assertEquals(true, "expense" in model.lastPrompt)
    }

    @Test
    fun `titles with digits, repeated or out of range indexes are dropped`() = runTest {
        val model = Fake(answer = """[{"i":0,"title":"кофе 3500"},{"i":5,"title":"x"},{"i":1,"title":"Coto","category":"Кафе"},{"i":1,"title":"again"}]""")
        val hints = NoteHints(model).read(listOf("a", "b"), categories)
        assertNull(hints[0])
        assertEquals(NoteHint("Coto", "Кафе"), hints[1])
    }

    @Test
    fun `no model, no lines or a broken answer leave everything empty`() = runTest {
        assertEquals(listOf<NoteHint?>(null), NoteHints(Fake(available = false)).read(listOf("a"), categories))
        assertEquals(emptyList(), NoteHints(Fake()).read(emptyList(), categories))
        assertEquals(listOf<NoteHint?>(null, null), NoteHints(Fake(answer = "не знаю")).read(listOf("a", "b"), categories))
        assertEquals(listOf<NoteHint?>(null), NoteHints(Fake(answer = "[{oops")).read(listOf("a"), categories))
        assertEquals(listOf<NoteHint?>(null), NoteHints(Fake(answer = null)).read(listOf("a"), categories))
    }

    @Test
    fun `the prompt lists the categories and numbers the lines`() = runTest {
        val model = Fake(answer = "[]")
        NoteHints(model).read(listOf("диско", "лента"), categories)
        assertEquals(true, model.lastPrompt.contains("Продукты | Кафе | Такси"))
        assertEquals(true, model.lastPrompt.contains("0: диско"))
        assertEquals(true, model.lastPrompt.contains("1: лента"))
    }
}
