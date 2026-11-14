package app.coshell.core.domain

import app.coshell.core.model.Account
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class CategoryAndDeleteUseCasesTest {
    private var counter = 0
    private val ids = IdGenerator { "id${counter++}" }

    @Test
    fun `seeds default categories only once`() = runTest {
        val categories = FakeCategoryRepository()
        val seed = SeedDefaultCategoriesUseCase(categories, ids)
        assertTrue(seed())
        val count = categories.countCategories()
        assertTrue(categories.items.value.values.any { it.type == CategoryType.INCOME })
        assertFalse(seed())
        assertEquals(count, categories.countCategories())
    }

    @Test
    fun `default categories have unique names within a type in every language`() {
        listOf("ru", "en", "es").forEach { language ->
            DefaultCategories.create(ids, language).groupBy { it.type }.values.forEach { list ->
                assertEquals(list.size, list.map { it.name }.toSet().size)
            }
        }
    }

    @Test
    fun `default categories are named in the app language, Russian for an unknown one`() = runTest {
        val categories = FakeCategoryRepository()
        SeedDefaultCategoriesUseCase(categories, ids) { "es" }()
        assertTrue(categories.items.value.values.any { it.name == "Supermercado" })
        assertEquals("Groceries", DefaultCategories.create(ids, "en").first().name)
        assertEquals("Продукты", DefaultCategories.create(ids, "fr").first().name)
        val icons = listOf("ru", "en", "es").map { lang -> DefaultCategories.create(ids, lang).map { it.icon to it.type } }
        assertEquals(1, icons.toSet().size)
    }

    @Test
    fun `account with transactions is not deleted`() = runTest {
        val usd = CurrencyCode("USD")
        val accounts = FakeAccountRepository()
        val transactions = FakeTransactionRepository()
        val delete = DeleteAccountUseCase(accounts, transactions)
        accounts.saveAccount(Account("a", "A", usd))
        accounts.saveAccount(Account("b", "B", usd))
        transactions.saveTransaction(
            Transaction(
                "t", TransactionType.TRANSFER, "a", Money(1, usd), Instant.fromEpochSeconds(0),
                toAccountId = "b", toAmount = Money(1, usd),
            ),
        )
        assertEquals(DeleteAccountUseCase.Result.HAS_TRANSACTIONS, delete("b"))
        transactions.deleteTransaction("t")
        assertEquals(DeleteAccountUseCase.Result.DELETED, delete("b"))
        assertNull(accounts.getAccount("b"))
    }
}
