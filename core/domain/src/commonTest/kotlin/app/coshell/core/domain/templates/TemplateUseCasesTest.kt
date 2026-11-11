package app.coshell.core.domain.templates

import app.coshell.core.domain.FakeAccountRepository
import app.coshell.core.domain.FakeCategoryRepository
import app.coshell.core.domain.FakeTemplateRepository
import app.coshell.core.domain.templates.SaveTemplateUseCase.Reason
import app.coshell.core.domain.templates.SaveTemplateUseCase.Result
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

class TemplateUseCasesTest {
    private val ars = CurrencyCode("ARS")
    private val usd = CurrencyCode("USD")
    private val now = Instant.parse("2026-11-11T15:00:00Z")
    private val clock = object : Clock {
        override fun now() = now
    }

    private val accounts = FakeAccountRepository()
    private val categories = FakeCategoryRepository()
    private val templates = FakeTemplateRepository()
    private val save = SaveTemplateUseCase(accounts, categories, templates)
    private var nextId = 0
    private val saveAs = SaveAsTemplateUseCase(templates, save, IdGenerator { "tpl-${nextId++}" })
    private val apply = ApplyTemplateUseCase(accounts, categories, templates, clock)

    private val spanish = TransactionTemplate(
        "spanish", "Испанский", TransactionType.EXPENSE, "galicia", ars, Money(40_000_00, ars),
        categoryId = "edu", note = "Испанский, Марта",
    )
    private val toCash = TransactionTemplate("move", "В наличные", TransactionType.TRANSFER, "galicia", ars, toAccountId = "cash")

    @BeforeTest
    fun setUp() = runTest {
        accounts.saveAccount(Account("galicia", "Galicia", ars))
        accounts.saveAccount(Account("cash", "Наличные", usd))
        categories.saveCategory(Category("edu", "Образование", CategoryType.EXPENSE))
        categories.saveCategory(Category("salary", "Зарплата", CategoryType.INCOME))
    }

    // --- save

    @Test
    fun `valid template is saved`() = runTest {
        assertEquals(Result.Saved, save(spanish))
        assertEquals(spanish, templates.getTemplate("spanish"))
    }

    @Test
    fun `template must point at live account, target and category of the right kind`() = runTest {
        assertEquals(Result.Invalid(Reason.ACCOUNT_NOT_FOUND), save(spanish.copy(accountId = "nope")))
        assertEquals(Result.Invalid(Reason.CURRENCY_MISMATCH), save(spanish.copy(accountId = "cash")))
        assertEquals(Result.Invalid(Reason.TARGET_ACCOUNT_NOT_FOUND), save(toCash.copy(toAccountId = "nope")))
        assertEquals(Result.Invalid(Reason.CATEGORY_NOT_FOUND), save(spanish.copy(categoryId = "nope")))
        assertEquals(Result.Invalid(Reason.CATEGORY_TYPE_MISMATCH), save(spanish.copy(categoryId = "salary")))
        assertTrue(templates.items.value.isEmpty())
    }

    @Test
    fun `archived account or category cannot go into a template`() = runTest {
        categories.saveCategory(Category("edu", "Образование", CategoryType.EXPENSE, archived = true))
        assertEquals(Result.Invalid(Reason.CATEGORY_NOT_FOUND), save(spanish))
        accounts.saveAccount(Account("galicia", "Galicia", ars, archived = true))
        assertEquals(Result.Invalid(Reason.ACCOUNT_NOT_FOUND), save(spanish))
    }

    @Test
    fun `transfer template may carry any category`() = runTest {
        assertEquals(Result.Saved, save(toCash.copy(categoryId = "salary")))
    }

    // --- save as template

    private val groceries = Transaction(
        "op", TransactionType.EXPENSE, "galicia", Money(32_000_00, ars), Instant.parse("2026-11-01T10:00:00Z"),
        categoryId = "edu", note = "Coto", originalAmount = Money(22_00, usd),
    )

    @Test
    fun `operation becomes a template without its date and receipt`() = runTest {
        assertEquals(Result.Saved, saveAs(groceries, "  Продукты Coto ", rememberAmount = true))
        assertEquals(
            TransactionTemplate(
                "tpl-0", "Продукты Coto", TransactionType.EXPENSE, "galicia", ars, Money(32_000_00, ars),
                categoryId = "edu", note = "Coto", sortOrder = 0,
            ),
            templates.getTemplate("tpl-0"),
        )
    }

    @Test
    fun `amount is remembered only when asked, new templates go to the end`() = runTest {
        save(spanish.copy(sortOrder = 4))
        saveAs(groceries, "Coto", rememberAmount = false)
        val saved = templates.getTemplate("tpl-0")!!
        assertNull(saved.amount)
        assertEquals(ars, saved.currency)
        assertEquals(5, saved.sortOrder)
    }

    @Test
    fun `transfer keeps its target account but not the arriving amount`() = runTest {
        val transfer = Transaction(
            "op", TransactionType.TRANSFER, "galicia", Money(145_000_00, ars), now, toAccountId = "cash", toAmount = Money(100_00, usd),
        )
        saveAs(transfer, "Обмен", rememberAmount = true)
        val saved = templates.getTemplate("tpl-0")!!
        assertEquals("cash", saved.toAccountId)
        assertEquals(Money(145_000_00, ars), saved.amount)
    }

    // --- apply

    @Test
    fun `template fills a draft dated today`() = runTest {
        save(spanish)
        assertEquals(
            TransactionDraft(
                TransactionType.EXPENSE, now, "galicia", Money(40_000_00, ars), "edu", note = "Испанский, Марта",
                templateId = "spanish",
            ),
            apply("spanish"),
        )
    }

    @Test
    fun `template without an amount leaves it empty`() = runTest {
        save(spanish.copy(amount = null))
        val draft = apply("spanish")!!
        assertNull(draft.amount)
        assertTrue(draft.gaps.isEmpty())
    }

    @Test
    fun `missing template gives no draft`() = runTest {
        assertNull(apply("nope"))
    }

    @Test
    fun `archived category and account are left for the user to choose`() = runTest {
        save(spanish)
        categories.saveCategory(Category("edu", "Образование", CategoryType.EXPENSE, archived = true))
        assertEquals(setOf(TemplateGap.CATEGORY), apply("spanish")!!.gaps)

        accounts.saveAccount(Account("galicia", "Galicia", ars, archived = true))
        val draft = apply("spanish")!!
        assertEquals(setOf(TemplateGap.ACCOUNT, TemplateGap.CATEGORY), draft.gaps)
        assertNull(draft.accountId)
        assertNull(draft.categoryId)
        assertNull(draft.amount, "the amount was in the lost account's currency")
        assertEquals("Испанский, Марта", draft.note)
    }

    @Test
    fun `deleted account, target and category do not fail`() = runTest {
        save(toCash.copy(categoryId = "edu"))
        accounts.deleteAccount("galicia")
        accounts.deleteAccount("cash")
        categories.deleteCategory("edu")
        val draft = apply("move")!!
        assertEquals(setOf(TemplateGap.ACCOUNT, TemplateGap.TARGET_ACCOUNT, TemplateGap.CATEGORY), draft.gaps)
        assertNull(draft.toAccountId)
    }

    @Test
    fun `account that changed currency counts as missing`() = runTest {
        save(spanish)
        accounts.saveAccount(Account("galicia", "Galicia", usd))
        assertEquals(setOf(TemplateGap.ACCOUNT), apply("spanish")!!.gaps)
    }

    @Test
    fun `transfer draft keeps both accounts`() = runTest {
        save(toCash)
        val draft = apply("move")!!
        assertEquals("galicia" to "cash", draft.accountId to draft.toAccountId)
        assertTrue(draft.gaps.isEmpty())
    }
}
