package app.coshell.core.domain.drafts

import app.coshell.core.domain.FakeAccountRepository
import app.coshell.core.domain.FakeCategoryRepository
import app.coshell.core.domain.FakeTransactionRepository
import app.coshell.core.domain.SaveTransactionUseCase
import app.coshell.core.domain.ai.LanguageModel
import app.coshell.core.domain.ai.NoteHints
import app.coshell.core.model.Account
import app.coshell.core.model.AccountType
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Draft
import app.coshell.core.model.DraftCategorySource
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class DraftsTest {
    private val ars = CurrencyCode("ARS")
    private val usd = CurrencyCode("USD")
    private val now = 1_700_000_000_000L
    private val day = 86_400_000L
    private val galicia = Account("a1", "Galicia", ars, AccountType.CARD)
    private val cash = Account("a2", "Наличные", ars, AccountType.CASH)
    private val dollars = Account("a3", "Dollars", usd, AccountType.CASH)
    private val cafe = Category("c1", "Кафе", CategoryType.EXPENSE)
    private val taxi = Category("c2", "Такси", CategoryType.EXPENSE)
    private val food = Category("c3", "Продукты", CategoryType.EXPENSE)
    private val salary = Category("c4", "Зарплата", CategoryType.INCOME)

    private fun spent(id: String, account: String, category: String, payee: String) = Transaction(
        id = id, type = TransactionType.EXPENSE, accountId = account, amount = Money(100, ars),
        date = Instant.fromEpochMilliseconds(now - 1), categoryId = category, payee = payee,
    )

    private fun ctx(history: List<Transaction> = emptyList(), accounts: List<Account> = listOf(galicia, cash)) = QuickNoteDrafts.Context(
        accounts = accounts, categories = listOf(cafe, taxi, food, salary), history = history, fallback = ars, known = setOf(ars, usd), now = now,
    )

    private fun ids() = object : IdGenerator {
        var n = 0
        override fun newId() = "d${n++}"
    }

    @Test
    fun `lines split on newlines semicolons and comma-space but not inside a number`() {
        assertEquals(listOf("кофе 3500", "такси 2000", "хлеб 15,50 usd"), QuickNoteDrafts.lines("кофе 3500, такси 2000\n\n хлеб 15,50 usd "))
        assertEquals(listOf("a 1", "b 2"), QuickNoteDrafts.lines("a 1;b 2"))
    }

    @Test
    fun `a line with an amount becomes a draft with the category from past entries`() {
        val history = listOf(spent("t1", "a1", "c1", "кофе"))
        val draft = QuickNoteDrafts.build(listOf("кофе 3500"), ctx(history), ids()).single()
        assertEquals(350000L, draft.amountMinor)
        assertEquals(ars, draft.currency)
        assertEquals("кофе", draft.title)
        assertEquals("c1", draft.categoryId)
        assertEquals(DraftCategorySource.HISTORY, draft.categorySource)
        assertEquals("a1", draft.accountId)
        assertEquals(now, draft.dateMillis)
    }

    @Test
    fun `yesterday moves the date and the card word picks the card`() {
        val history = listOf(spent("t1", "a2", "c3", "x"))
        val draft = QuickNoteDrafts.build(listOf("вчера хлеб 850 карта"), ctx(history), ids()).single()
        assertEquals(now - day, draft.dateMillis)
        assertEquals("a1", draft.accountId) // the card, though cash was used last
        val plain = QuickNoteDrafts.build(listOf("хлеб 850"), ctx(history), ids()).single()
        assertEquals("a2", plain.accountId) // the account used last
    }

    @Test
    fun `a line without a number is a draft without an amount`() {
        val draft = QuickNoteDrafts.build(listOf("купил хлеб"), ctx(), ids()).single()
        assertNull(draft.amountMinor)
        assertEquals("купил хлеб", draft.title)
    }

    @Test
    fun `a named currency picks the account of that currency, none means no account`() {
        val withDollars = QuickNoteDrafts.build(listOf("netflix 6,99 usd"), ctx(accounts = listOf(galicia, dollars)), ids()).single()
        assertEquals("a3", withDollars.accountId)
        assertEquals(699L, withDollars.amountMinor)
        val without = QuickNoteDrafts.build(listOf("netflix 6,99 usd"), ctx(), ids()).single()
        assertNull(without.accountId)
        assertEquals(usd, without.currency)
    }

    @Test
    fun `archived accounts are never picked`() {
        val draft = QuickNoteDrafts.build(listOf("кофе 100"), ctx(accounts = listOf(galicia.copy(archived = true))), ids()).single()
        assertNull(draft.accountId)
    }

    @Test
    fun `the model fills the category only for lines nothing could guess`() = runTest {
        val model = object : LanguageModel {
            override val available = true
            var asked = ""
            override suspend fun complete(system: String, prompt: String): String? {
                asked = prompt
                return "[{\"i\":0,\"title\":\"Такси\",\"category\":\"Такси\"}]"
            }
        }
        val accounts = FakeAccountRepository().apply { saveAccount(galicia) }
        val categories = FakeCategoryRepository().apply { listOf(cafe, taxi, food).forEach { saveCategory(it) } }
        val transactions = FakeTransactionRepository().apply { saveTransaction(spent("t1", "a1", "c1", "кофе")) }
        val drafts = InMemoryDraftRepository()
        val made = ProcessQuickNoteUseCase(accounts, categories, transactions, drafts, NoteHints(model), ids(), { now })("кофе 3500, поездка 2000")
        assertEquals(2, made)
        val byTitle = drafts.items.value.associateBy { it.title }
        assertEquals(DraftCategorySource.HISTORY, byTitle.getValue("кофе").categorySource)
        assertEquals("c2", byTitle.getValue("Такси").categoryId)
        assertEquals(DraftCategorySource.AI, byTitle.getValue("Такси").categorySource)
        assertEquals(true, "поездка 2000" in model.asked && "кофе" !in model.asked.substringAfter("0: "))
    }

    @Test
    fun `without a model nothing is guessed beyond history and names`() = runTest {
        val accounts = FakeAccountRepository().apply { saveAccount(galicia) }
        val categories = FakeCategoryRepository().apply { listOf(cafe, taxi).forEach { saveCategory(it) } }
        val drafts = InMemoryDraftRepository()
        ProcessQuickNoteUseCase(accounts, categories, FakeTransactionRepository(), drafts, null, ids(), { now })("поездка 2000")
        assertNull(drafts.items.value.single().categoryId)
    }

    @Test
    fun `check names what is missing in order amount account category`() {
        val accounts = mapOf("a1" to galicia, "a3" to dollars)
        val categories = mapOf("c1" to cafe, "c4" to salary)
        val ready = Draft("d", "x", ars, 100, "a1", "c1", null, now, now)
        assertNull(DraftCheck.missing(ready, accounts, categories))
        assertEquals(DraftMissing.AMOUNT, DraftCheck.missing(ready.copy(amountMinor = null), accounts, categories))
        assertEquals(DraftMissing.ACCOUNT, DraftCheck.missing(ready.copy(accountId = "a3"), accounts, categories)) // another currency
        assertEquals(DraftMissing.ACCOUNT, DraftCheck.missing(ready.copy(accountId = null), accounts, categories))
        assertEquals(DraftMissing.CATEGORY, DraftCheck.missing(ready.copy(categoryId = "c4"), accounts, categories)) // an income category
        assertEquals(DraftMissing.CATEGORY, DraftCheck.missing(ready.copy(categoryId = null), accounts, categories))
    }

    @Test
    fun `posting takes the ready drafts only and leaves the rest`() = runTest {
        val accounts = FakeAccountRepository().apply { saveAccount(galicia) }
        val categories = FakeCategoryRepository().apply { saveCategory(cafe) }
        val transactions = FakeTransactionRepository()
        val drafts = InMemoryDraftRepository()
        drafts.saveDrafts(listOf(
            Draft("ok", "кофе", ars, 350000, "a1", "c1", DraftCategorySource.HISTORY, now - day, now),
            Draft("no", "хлеб", ars, null, "a1", "c1", null, now, now),
        ))
        val result = PostDraftsUseCase(accounts, categories, drafts, SaveTransactionUseCase(accounts, categories, transactions), ids())(listOf("ok", "no"))
        assertEquals(1, result.transactionIds.size)
        assertEquals(1, result.left)
        val saved = transactions.items.value.values.single()
        assertEquals(350000L, saved.amount.amountMinor)
        assertEquals("кофе", saved.note)
        assertEquals(now - day, saved.date.toEpochMilliseconds())
        assertEquals(listOf("no"), drafts.items.value.map { it.id })
    }
}
