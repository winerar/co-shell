package app.coshell.core.domain.ai

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

/** The category of an unknown payee (#594): only a name from the person's list is ever used. */
class PayeeCategoryAiTest {
    private class Fake(override val available: Boolean = true, val answer: String? = null) : LanguageModel {
        var calls = 0
        var prompt = ""
        override suspend fun complete(system: String, prompt: String): String? { calls++; this.prompt = prompt; return answer }
    }

    private val categories = listOf("Продукты", "Кафе", "Такси")

    @Test
    fun `a name from the list is returned whatever the case and the words around it`() = runTest {
        val model = Fake(answer = """Вот: {"category":"кафе"} готово""")
        assertEquals("Кафе", PayeeCategoryAi(model).suggest("Starbucks", categories))
        assertEquals(true, "Starbucks" in model.prompt && "Продукты | Кафе | Такси" in model.prompt)
    }

    @Test
    fun `a name outside the list, null, junk or a failure give nothing`() = runTest {
        assertNull(PayeeCategoryAi(Fake(answer = """{"category":"Космос"}""")).suggest("Shell", categories))
        assertNull(PayeeCategoryAi(Fake(answer = """{"category":null}""")).suggest("Shell", categories))
        assertNull(PayeeCategoryAi(Fake(answer = "не знаю")).suggest("Shell", categories))
        assertNull(PayeeCategoryAi(Fake(answer = null)).suggest("Shell", categories))
    }

    @Test
    fun `no model, no text or no categories means no request`() = runTest {
        val model = Fake(answer = """{"category":"Кафе"}""")
        assertNull(PayeeCategoryAi(Fake(available = false)).suggest("Starbucks", categories))
        assertNull(PayeeCategoryAi(model).suggest("  ", categories))
        assertNull(PayeeCategoryAi(model).suggest("Starbucks", emptyList()))
        assertEquals(0, model.calls)
        assertNull(PayeeCategoryAi(NoLanguageModel).suggest("Starbucks", categories))
    }
}
