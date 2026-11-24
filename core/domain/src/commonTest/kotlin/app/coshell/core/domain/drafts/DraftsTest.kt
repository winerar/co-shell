package app.coshell.core.domain.drafts

import app.coshell.core.domain.FakeAccountRepository
import app.coshell.core.domain.FakeCategoryRepository
import app.coshell.core.domain.FakeTransactionRepository
import app.coshell.core.domain.SaveTransactionUseCase
import app.coshell.core.domain.ai.LanguageModel
import app.coshell.core.domain.ai.NoteHint
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
    private val food = Category("c3", "Продукты", CategoryType.EXPENSE, icon = "food")
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
        assertEquals("a2", draft.accountId) // nothing said about paying: cash
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
        assertEquals("хлеб", draft.title)
        assertEquals("c3", draft.categoryId)
    }

    @Test
    fun `a line nothing recognises and without a number is not a draft`() {
        assertEquals(emptyList(), QuickNoteDrafts.build(listOf("тест заметки"), ctx(), ids()))
        assertEquals(true, QuickNoteDrafts.isNoise("тест заметки", ctx()))
        assertEquals(false, QuickNoteDrafts.isNoise("тест 500", ctx())) // a number: maybe a purchase named their own way
    }

    @Test
    fun `a word of an account name picks that account and leaves the title`() {
        val sber = Account("s", "Сбер. счёт AMD", CurrencyCode("AMD"), AccountType.CARD)
        val draft = QuickNoteDrafts.build(listOf("такси 2000 сбер"), ctx(accounts = listOf(galicia, sber)), ids()).single()
        assertEquals("s", draft.accountId)
        assertEquals("такси", draft.title)
        assertEquals(CurrencyCode("AMD"), draft.currency)
        val mp = Account("m", "Mercado Pago", ars, AccountType.CARD)
        assertEquals("m", QuickNoteDrafts.build(listOf("обед 12к мп"), ctx(accounts = listOf(galicia, mp)), ids()).single().accountId)
    }

    @Test
    fun `without a named currency the account in the local currency wins over the last used one`() {
        val amd = Account("s", "Сбер", CurrencyCode("AMD"), AccountType.CARD)
        val history = listOf(spent("t1", "s", "c1", "x"))
        val base = ctx(history, accounts = listOf(amd, galicia))
        val local = QuickNoteDrafts.Context(base.accounts, base.categories, base.history, base.fallback, base.known, base.now, local = ars)
        assertEquals("a1", QuickNoteDrafts.build(listOf("кофе 3500"), local, ids()).single().accountId)
        assertEquals("s", QuickNoteDrafts.build(listOf("кофе 3500"), base, ids()).single().accountId)
    }

    @Test
    fun `a line that says nothing about paying goes to cash, a card word to the card`() {
        val history = listOf(spent("t1", "a1", "c1", "x"))
        assertEquals("a2", QuickNoteDrafts.build(listOf("кофе 3500"), ctx(history), ids()).single().accountId)
        // Cash only in the currency the line gets anyway: no dollars cash for a peso coffee.
        assertEquals("a1", QuickNoteDrafts.build(listOf("кофе 3500"), ctx(history, accounts = listOf(galicia, dollars)), ids()).single().accountId)
        assertEquals("a1", QuickNoteDrafts.build(listOf("кофе 3500 безнал"), ctx(history), ids()).single().accountId)
        // Without a cash account in that currency the last used one stays.
        assertEquals("a1", QuickNoteDrafts.build(listOf("кофе 3500"), ctx(history, accounts = listOf(galicia)), ids()).single().accountId)
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
        val made = ProcessQuickNoteUseCase(accounts, categories, transactions, drafts, NoteHints(model), ids()) { now }("кофе 3500, поездка 2000")
        assertEquals(2, made.made)
        val byTitle = drafts.items.value.associateBy { it.title }
        assertEquals(DraftCategorySource.HISTORY, byTitle.getValue("кофе").categorySource)
        assertEquals("c2", byTitle.getValue("Такси").categoryId)
        assertEquals(DraftCategorySource.AI, byTitle.getValue("Такси").categorySource)
        assertEquals(true, "поездка 2000" in model.asked && "кофе" !in model.asked.substringAfter("0: "))
    }

    @Test
    fun `a line the model calls not an expense is skipped`() = runTest {
        val model = object : LanguageModel {
            override val available = true
            override suspend fun complete(system: String, prompt: String) = "[{\"i\":0,\"expense\":false}]"
        }
        val accounts = FakeAccountRepository().apply { saveAccount(galicia) }
        val categories = FakeCategoryRepository().apply { listOf(cafe, taxi).forEach { saveCategory(it) } }
        val drafts = InMemoryDraftRepository()
        val result = ProcessQuickNoteUseCase(accounts, categories, FakeTransactionRepository(), drafts, NoteHints(model), ids()) { now }("встреча 15")
        assertEquals(0, result.made)
        assertEquals(listOf("встреча 15"), result.skipped)
        assertEquals(emptyList(), drafts.items.value)
    }

    @Test
    fun `without a model nothing is guessed beyond history and names`() = runTest {
        val accounts = FakeAccountRepository().apply { saveAccount(galicia) }
        val categories = FakeCategoryRepository().apply { listOf(cafe, taxi).forEach { saveCategory(it) } }
        val drafts = InMemoryDraftRepository()
        ProcessQuickNoteUseCase(accounts, categories, FakeTransactionRepository(), drafts, null, ids()) { now }("поездка 2000")
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

    private val mp = Account("a4", "Mercado Pago", ars, AccountType.CARD)

    @Test
    fun `a cheap model is asked about every line and its account is used`() = runTest {
        val model = object : LanguageModel {
            override val available = true
            override val cheap = true
            var asked = ""
            override suspend fun complete(system: String, prompt: String): String? {
                asked = prompt
                return "[{\"i\":0,\"account\":\"Mercado Pago · ARS · card\"}]"
            }
        }
        val accounts = FakeAccountRepository().apply { saveAccount(galicia); saveAccount(mp) }
        val categories = FakeCategoryRepository().apply { listOf(cafe, taxi).forEach { saveCategory(it) } }
        val transactions = FakeTransactionRepository().apply { saveTransaction(spent("t1", "a1", "c1", "кофе")) }
        val drafts = InMemoryDraftRepository()
        ProcessQuickNoteUseCase(accounts, categories, transactions, drafts, NoteHints(model), ids()) { now }("кофе 3500")
        val draft = drafts.items.value.single()
        assertEquals("a4", draft.accountId)
        assertEquals(DraftCategorySource.HISTORY, draft.categorySource)
        assertEquals(true, "Mercado Pago · ARS · card" in model.asked)
    }

    @Test
    fun `a slow model is not asked about a line the rules finished`() {
        val lines = listOf("кофе 3500")
        val history = listOf(spent("t1", "a1", "c1", "кофе"))
        val accounts = listOf(galicia, mp)
        assertEquals(emptyList(), QuickNoteDrafts.needsModel(lines, ctx(history, accounts)))
        assertEquals(listOf(0), QuickNoteDrafts.needsModel(lines, ctx(history, accounts), broad = true))
        assertEquals(emptyList(), QuickNoteDrafts.needsModel(lines, ctx(history, listOf(galicia)), broad = true))
    }

    @Test
    fun `an account the model made up or one that contradicts the line is ignored`() {
        val history = listOf(spent("t1", "a1", "c1", "кофе"))
        fun accountOf(line: String, label: String) = QuickNoteDrafts.build(
            listOf(line), ctx(history, listOf(galicia, mp, cash)), ids(), mapOf(0 to NoteHint(null, null, account = label)),
        ).single().accountId
        assertEquals("a2", accountOf("кофе 3500", "Santander · ARS · card")) // unknown label: as if nothing was said — cash
        assertEquals("a2", accountOf("кофе 3500 нал", "Mercado Pago · ARS · card")) // the word «нал» wins over a card
        assertEquals("a4", accountOf("кофе 3500 карта", "Mercado Pago · ARS · card")) // same kind: the model chooses among cards
        assertEquals(null, accountOf("кофе 3500 usd", "Mercado Pago · ARS · card")) // a named currency the account does not have: the person chooses
    }

    private fun spentIn(id: String, account: Account, whole: Long) = Transaction(
        id = id, type = TransactionType.EXPENSE, accountId = account.id, amount = Money(whole * 100, account.currency),
        date = Instant.fromEpochMilliseconds(now - 1), categoryId = "c1", payee = "кофе",
    )

    private fun made(line: String, history: List<Transaction> = emptyList(), accounts: List<Account> = listOf(galicia, dollars)) =
        QuickNoteDrafts.build(listOf(line), ctx(history, accounts), ids()).single()

    @Test
    fun `the order of the number picks the currency when none is typed`() {
        val pesos = made("кофе 3500")
        assertEquals("a1", pesos.accountId)
        assertEquals(350_000L, pesos.amountMinor)
        val small = made("кофе 5")
        assertEquals("a3", small.accountId)
        assertEquals(usd, small.currency)
        assertEquals(500L, small.amountMinor)
    }

    @Test
    fun `past expenses beat the rough prior, a tie and one currency change nothing`() {
        val history = listOf(30L, 40L, 50L).mapIndexed { i, w -> spentIn("p$i", galicia, w) } +
            listOf(2L, 5L, 9L).mapIndexed { i, w -> spentIn("d$i", dollars, w) }
        assertEquals("a1", made("кофе 40", history).accountId) // pesos here are small: 2 digits, as the number
        assertEquals("a3", made("кофе 4", history).accountId)
        assertEquals("a1", made("кофе 400").accountId) // 3 digits: as far from dollars as from pesos, the usual choice
        assertEquals("a2", made("кофе 5", accounts = listOf(galicia, cash)).accountId) // all pesos, nothing said: cash
    }

    @Test
    fun `a typed currency or a named account wins over the order of the number`() {
        assertEquals("a1", made("кофе 5 ars").accountId)
        assertEquals("a1", made("кофе 5 galicia").accountId)
    }
}
