package app.coshell.transactions.edit

import app.coshell.core.domain.ai.LanguageModel
import app.coshell.core.domain.ai.PayeeCategoryAi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** «Категория для нового получателя через ИИ» (#594): asked after a pause, never over a manual pick, only a name of the list is used. */
class TransactionEditAiCategoryTest : TransactionEditTestBase() {
    private class Model(var answer: String?, override val available: Boolean = true) : LanguageModel {
        var calls = 0
        override suspend fun complete(system: String, prompt: String): String? { calls++; return answer }
    }

    private fun ai(model: Model) = PayeeCategoryAi(model)

    @Test
    fun `a note nothing knows gets the category the model names, marked as AI`() = runTest {
        seed()
        val vm = viewModel(categoryAi = ai(Model("""{"category":"Кафе"}""")))
        vm.onNoteChange("Starbucks")
        assertNull(vm.state.value.categoryId)
        advanceTimeBy(701)
        val state = vm.state.value
        assertEquals("cafe", state.categoryId)
        assertTrue(state.categoryGuessed && state.categoryByAi)
    }

    @Test
    fun `a manual pick during the pause wins and clears the AI mark`() = runTest {
        seed()
        val vm = viewModel(categoryAi = ai(Model("""{"category":"Кафе"}""")))
        vm.onNoteChange("Starbucks")
        vm.onCategorySelect("food")
        advanceTimeBy(701)
        assertEquals("food", vm.state.value.categoryId)
        assertFalse(vm.state.value.categoryByAi)
        vm.onNoteChange("Starbucks 2")
        advanceTimeBy(701)
        assertEquals("food", vm.state.value.categoryId)
    }

    @Test
    fun `typing on asks again for the new text only`() = runTest {
        seed()
        val model = Model("""{"category":"Кафе"}""")
        val vm = viewModel(categoryAi = ai(model))
        vm.onNoteChange("Star")
        advanceTimeBy(300)
        vm.onNoteChange("Starbucks")
        advanceTimeBy(701)
        assertEquals(1, model.calls)
        assertEquals("cafe", vm.state.value.categoryId)
    }

    @Test
    fun `a name outside the list, a short note, no model and an income ask for nothing or get nothing`() = runTest {
        seed()
        val outside = viewModel(categoryAi = ai(Model("""{"category":"Космос"}""")))
        outside.onNoteChange("Starbucks")
        advanceTimeBy(701)
        assertNull(outside.state.value.categoryId)

        val short = Model("""{"category":"Кафе"}""")
        viewModel(categoryAi = ai(short)).onNoteChange("ab")
        advanceTimeBy(701)
        assertEquals(0, short.calls)

        val none = Model("""{"category":"Кафе"}""", available = false)
        val noModel = viewModel(categoryAi = ai(none))
        noModel.onNoteChange("Starbucks")
        advanceTimeBy(701)
        assertEquals(0, none.calls)
        assertNull(noModel.state.value.categoryId)

        val income = Model("""{"category":"Кафе"}""")
        val vm = viewModel(type = "INCOME", categoryAi = ai(income))
        vm.onNoteChange("Starbucks")
        advanceTimeBy(701)
        assertEquals(0, income.calls)
    }
}
