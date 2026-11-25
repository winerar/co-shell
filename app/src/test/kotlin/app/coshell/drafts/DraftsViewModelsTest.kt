package app.coshell.drafts

import app.coshell.ai.cloud.CloudLimitNotice
import app.coshell.core.domain.SaveTransactionUseCase
import app.coshell.core.domain.ai.LanguageModel
import app.coshell.core.domain.ai.NoteHints
import app.coshell.core.domain.drafts.DraftMissing
import app.coshell.core.domain.drafts.InMemoryDraftRepository
import app.coshell.core.domain.drafts.PostDraftsUseCase
import app.coshell.core.domain.drafts.ProcessQuickNoteUseCase
import app.coshell.core.model.Account
import app.coshell.core.model.AccountType
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Draft
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.MoneyFormat
import app.coshell.drafts.list.DraftsViewModel
import app.coshell.drafts.notes.NotesViewModel
import app.coshell.testing.FakeAccountRepository
import app.coshell.testing.FakeCategoryRepository
import app.coshell.testing.FakeTransactionRepository
import app.coshell.testing.MainDispatcherRule
import app.coshell.undo.UndoCenter
import app.coshell.undo.UndoOffer
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** The two screens of quick notes (#586): the note and its drafts. */
class DraftsViewModelsTest {
    @get:Rule val main = MainDispatcherRule()

    private val ars = CurrencyCode("ARS")
    private val now = Instant.parse("2026-11-21T15:00:00Z")
    private val accounts = FakeAccountRepository()
    private val categories = FakeCategoryRepository()
    private val transactions = FakeTransactionRepository()
    private val drafts = InMemoryDraftRepository()
    private val undo = UndoCenter()
    private var counter = 0
    private val ids = IdGenerator { "id${counter++}" }

    private class MemoryNote(var value: String = "") : NoteStore {
        override suspend fun text() = value
        override suspend fun save(text: String) { value = text }
    }

    private suspend fun seed() {
        accounts.saveAccount(Account("card", "Galicia", ars, AccountType.CARD))
        categories.saveCategory(Category("cafe", "Кафе", CategoryType.EXPENSE))
    }

    private class FakeModel(override val cheap: Boolean = false, override val limited: Boolean = false) : LanguageModel {
        override val available = true
        override suspend fun complete(system: String, prompt: String): String? = null
        var warms = 0
        override suspend fun warm() { warms++ }
    }

    @Test
    fun `the model starts loading once there is something to process, only once`() = runTest {
        seed()
        val model = FakeModel()
        val vm = notes(MemoryNote(""), model)
        assertEquals(0, model.warms)
        vm.onTextChange("кофе 3500")
        vm.onTextChange("кофе 3500\nтакси 2000")
        assertEquals(1, model.warms)
        val typed = FakeModel()
        notes(MemoryNote("кофе 3500"), typed)
        assertEquals(1, typed.warms)
    }

    private fun notes(store: NoteStore, model: FakeModel = FakeModel(), notice: CloudLimitNotice = CloudLimitNotice(), hints: NoteHints? = null) = NotesViewModel(
        store, ProcessQuickNoteUseCase(accounts, categories, transactions, drafts, hints, ids) { now.toEpochMilliseconds() }, model, notice,
    )

    private fun draftsViewModel() = DraftsViewModel(
        drafts, accounts, categories, transactions,
        PostDraftsUseCase(accounts, categories, drafts, SaveTransactionUseCase(accounts, categories, transactions), ids),
        undo, MoneyFormat.RU, object : Clock { override fun now() = now }, TimeZone.UTC, CloudLimitNotice(),
    )

    private fun draft(id: String, amount: Long? = 3_500_00, category: String? = "cafe") =
        Draft(id, "кофе", ars, amount, "card", category, null, now.toEpochMilliseconds(), now.toEpochMilliseconds())

    @Test
    fun `the note keeps what was typed and counts the lines`() = runTest {
        seed()
        val store = MemoryNote("кофе 3500")
        val vm = notes(store)
        assertEquals(1, vm.state.value.lineCount)
        vm.onTextChange("кофе 3500\nтакси 2000, хлеб 850")
        assertEquals(3, vm.state.value.lineCount)
        assertEquals("кофе 3500\nтакси 2000, хлеб 850", store.value)
    }

    @Test
    fun `processing makes a draft per line, empties the note and says done`() = runTest {
        seed()
        val store = MemoryNote("кофе 3500\nтакси 2000")
        val vm = notes(store)
        val done = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.done.toList(done) }
        vm.process()
        assertEquals(2, drafts.items.value.size)
        assertEquals("", store.value)
        assertEquals("", vm.state.value.text)
        assertFalse(vm.state.value.processing)
        assertEquals(1, done.size)
    }

    @Test
    fun `a line that is not about money stays in the note`() = runTest {
        seed()
        val store = MemoryNote("кофе 3500\nтест заметки")
        val vm = notes(store)
        val done = mutableListOf<Unit>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.done.toList(done) }
        vm.process()
        assertEquals(1, drafts.items.value.size)
        assertEquals("тест заметки", store.value)
        assertEquals(1, vm.state.value.skipped)
        assertEquals(1, done.size)
    }

    @Test
    fun `an empty note is not processed`() = runTest {
        seed()
        val vm = notes(MemoryNote(""))
        vm.process()
        assertTrue(drafts.items.value.isEmpty())
    }

    @Test
    fun `the list marks ready drafts ticked and says what the others lack`() = runTest {
        seed()
        drafts.saveDrafts(listOf(draft("a"), draft("b", amount = null), draft("c", category = null)))
        val vm = draftsViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }
        val rows = vm.state.value.rows.associateBy { it.id }
        assertTrue(rows.getValue("a").checked)
        assertNull(rows.getValue("a").missing)
        assertEquals(DraftMissing.AMOUNT, rows.getValue("b").missing)
        assertFalse(rows.getValue("b").checked)
        assertEquals(DraftMissing.CATEGORY, rows.getValue("c").missing)
        assertEquals(1, vm.state.value.postCount)
        assertEquals(1, vm.state.value.readyCount)
    }

    @Test
    fun `unticking a ready draft keeps it out of the posting`() = runTest {
        seed()
        drafts.saveDrafts(listOf(draft("a"), draft("b")))
        val vm = draftsViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }
        vm.onToggle("a")
        assertEquals(1, vm.state.value.postCount)
        vm.post()
        assertEquals(listOf("a"), drafts.items.value.map { it.id })
        assertEquals(1, transactions.items.value.size)
    }

    @Test
    fun `posting offers undo that takes the operations back and returns the drafts`() = runTest {
        seed()
        drafts.saveDrafts(listOf(draft("a"), draft("b")))
        val vm = draftsViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }
        var offer: UndoOffer? = null
        undo.offers.onEach { offer = it }.launchIn(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        vm.post()
        assertEquals(2, transactions.items.value.size)
        assertTrue(drafts.items.value.isEmpty())
        assertNotNull(offer)
        assertEquals(listOf<Any>(2), offer!!.args)
        offer!!.undo()
        assertTrue(transactions.items.value.isEmpty())
        assertEquals(listOf("a", "b"), drafts.items.value.map { it.id })
    }

    @Test
    fun `ticked drafts of any kind are deleted with undo, ready ones stay`() = runTest {
        seed()
        drafts.saveDrafts(listOf(draft("a"), draft("b", amount = null), draft("c", amount = null)))
        val vm = draftsViewModel()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect {} }
        var offer: UndoOffer? = null
        undo.offers.onEach { offer = it }.launchIn(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        vm.onToggle("a") // untick the ready one
        vm.onToggle("b") // tick the unready ones
        vm.onToggle("c")
        assertEquals(2, vm.state.value.deleteCount)
        assertEquals(0, vm.state.value.postCount)
        vm.delete()
        assertEquals(listOf("a"), drafts.items.value.map { it.id })
        assertTrue(transactions.items.value.isEmpty())
        assertEquals(listOf<Any>(2), offer!!.args)
        offer!!.undo()
        assertEquals(setOf("a", "b", "c"), drafts.items.value.map { it.id }.toSet())
    }

    @Test
    fun `the cloud line shows only while the cloud answers`() = runTest {
        seed()
        assertFalse(notes(MemoryNote("кофе 3500")).state.value.cloud)
        assertTrue(notes(MemoryNote("кофе 3500"), FakeModel(cheap = true)).state.value.cloud)
    }

    @Test
    fun `a limit hit while processing is handed to the drafts screen once`() = runTest {
        seed()
        val notice = CloudLimitNotice()
        val model = FakeModel(cheap = true, limited = true)
        val vm = notes(MemoryNote("кофе 3500"), model, notice, NoteHints(model))
        vm.process()
        assertTrue(notice.take())
        assertFalse(notice.take())
    }
}
