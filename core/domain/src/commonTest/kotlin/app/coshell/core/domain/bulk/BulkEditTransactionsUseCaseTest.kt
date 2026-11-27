package app.coshell.core.domain.bulk

import app.coshell.core.domain.FakeCategoryRepository
import app.coshell.core.domain.FakeTransactionRepository
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class BulkEditTransactionsUseCaseTest {
    private val ars = CurrencyCode("ARS")
    private val date = Instant.parse("2026-11-08T12:00:00Z")
    private val categories = FakeCategoryRepository()
    private val transactions = FakeTransactionRepository()
    private val edit = BulkEditTransactionsUseCase(categories, transactions)

    private fun expense(id: String, category: String? = null, labels: List<String> = emptyList()) =
        Transaction(id, TransactionType.EXPENSE, "a", Money(100, ars), date, categoryId = category, labels = labels)

    private fun income(id: String) = Transaction(id, TransactionType.INCOME, "a", Money(100, ars), date)

    @BeforeTest
    fun setUp() = runTest {
        categories.saveCategory(Category("food", "Продукты", CategoryType.EXPENSE))
        categories.saveCategory(Category("cafe", "Кафе", CategoryType.EXPENSE))
        categories.saveCategory(Category("salary", "Зарплата", CategoryType.INCOME))
        categories.saveCategory(Category("old", "Старая", CategoryType.EXPENSE, archived = true))
        listOf(expense("e1"), expense("e2", "cafe"), income("i1")).forEach { transactions.saveTransaction(it) }
    }

    @Test
    fun `sets one category on every expense and remembers the old rows`() = runTest {
        val result = edit(listOf("e1", "e2"), BulkEdit.SetCategory("food"))
        assertEquals(2, (result as BulkEditTransactionsUseCase.Result.Changed).count)
        assertEquals("food", transactions.getTransaction("e1")?.categoryId)
        assertEquals("food", transactions.getTransaction("e2")?.categoryId)
        assertEquals(listOf(null, "cafe"), result.before.map { it.categoryId })
    }

    @Test
    fun `leaves rows that already have the category and does not count them`() = runTest {
        val result = edit(listOf("e1", "e2"), BulkEdit.SetCategory("cafe"))
        assertEquals(listOf("e1"), (result as BulkEditTransactionsUseCase.Result.Changed).before.map { it.id })
    }

    @Test
    fun `says nothing to change when every row already matches`() = runTest {
        assertEquals(BulkEditTransactionsUseCase.Result.NothingToChange, edit(listOf("e2"), BulkEdit.SetCategory("cafe")))
    }

    @Test
    fun `refuses a category for mixed types and writes nothing`() = runTest {
        assertEquals(BulkEditTransactionsUseCase.Result.NotAllowed, edit(listOf("e1", "i1"), BulkEdit.SetCategory("food")))
        assertNull(transactions.getTransaction("e1")?.categoryId)
    }

    @Test
    fun `refuses an income category for expenses, an archived or an unknown one`() = runTest {
        assertEquals(BulkEditTransactionsUseCase.Result.NotAllowed, edit(listOf("e1"), BulkEdit.SetCategory("salary")))
        assertEquals(BulkEditTransactionsUseCase.Result.NotAllowed, edit(listOf("e1"), BulkEdit.SetCategory("old")))
        assertEquals(BulkEditTransactionsUseCase.Result.NotAllowed, edit(listOf("e1"), BulkEdit.SetCategory("nope")))
    }

    @Test
    fun `refuses a category when a transfer is chosen`() = runTest {
        val transfer = Transaction("t1", TransactionType.TRANSFER, "a", Money(100, ars), date, toAccountId = "b", toAmount = Money(100, ars))
        transactions.saveTransaction(transfer)
        assertEquals(BulkEditTransactionsUseCase.Result.NotAllowed, edit(listOf("t1"), BulkEdit.SetCategory("food")))
        assertNull(BulkEditRules.categoryType(listOf(transfer)))
    }

    @Test
    fun `adds tags to everything including transfers and keeps the old ones`() = runTest {
        transactions.saveTransaction(expense("e3", labels = listOf("Отпуск")))
        val result = edit(listOf("e1", "e3", "i1"), BulkEdit.AddTags(listOf("отпуск", "gifts")))
        assertEquals(3, (result as BulkEditTransactionsUseCase.Result.Changed).count)
        assertEquals(listOf("Отпуск", "gifts"), transactions.getTransaction("e3")?.labels)
        assertEquals(listOf("отпуск", "gifts"), transactions.getTransaction("e1")?.labels)
        assertEquals(listOf("e1", "e3", "i1"), result.before.map { it.id }.sorted())
    }

    @Test
    fun `adding a tag the row already has changes nothing`() = runTest {
        transactions.saveTransaction(expense("e3", labels = listOf("Отпуск")))
        assertEquals(BulkEditTransactionsUseCase.Result.NothingToChange, edit(listOf("e3"), BulkEdit.AddTags(listOf("  отпуск "))))
    }

    @Test
    fun `undo data brings the old rows back`() = runTest {
        val result = edit(listOf("e1", "e2"), BulkEdit.SetCategory("food")) as BulkEditTransactionsUseCase.Result.Changed
        result.before.forEach { transactions.saveTransaction(it) }
        assertNull(transactions.getTransaction("e1")?.categoryId)
        assertEquals("cafe", transactions.getTransaction("e2")?.categoryId)
    }
}
