package app.coshell.core.data

import app.coshell.core.data.backup.JsonBackupRepository
import app.coshell.core.data.db.createInMemoryCoShellDatabase
import app.coshell.core.data.repository.RoomAccountRepository
import app.coshell.core.data.repository.RoomBudgetRepository
import app.coshell.core.data.repository.RoomCategoryRepository
import app.coshell.core.data.repository.RoomTransactionRepository
import app.coshell.core.data.repository.WriteStamper
import app.coshell.core.domain.BackupImportResult
import app.coshell.core.model.Account
import app.coshell.core.model.AccountType
import app.coshell.core.model.Budget
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Instant

class BackupTest {
    private val usd = CurrencyCode("USD")
    private val rub = CurrencyCode("RUB")
    private val stamper = WriteStamper()

    private class Repos {
        val db = createInMemoryCoShellDatabase()
        val stamper = WriteStamper()
        val accounts = RoomAccountRepository(db.accountDao(), db.transactionDao(), stamper)
        val categories = RoomCategoryRepository(db.categoryDao(), stamper)
        val transactions = RoomTransactionRepository(db.transactionDao(), stamper)
        val budgets = RoomBudgetRepository(db.budgetDao(), stamper)
        val backup = JsonBackupRepository(db, stamper)
    }

    @Test
    fun `export then import into an empty database restores everything`() = runTest {
        val source = Repos()
        source.accounts.saveAccount(Account("usd", "Bybit", usd, AccountType.CRYPTO, initialBalanceMinor = 10_00))
        source.accounts.saveAccount(Account("rub", "Тинькофф", rub))
        source.categories.saveCategory(Category("kids", "Дети", CategoryType.EXPENSE, icon = "kids", color = 0xFF9C27B0))
        val transfer = Transaction(
            "t1", TransactionType.TRANSFER, "rub", Money(150_000_00, rub), Instant.parse("2026-10-14T11:39:39Z"),
            toAccountId = "usd", toAmount = Money(1790_00, usd), labels = listOf("обмен"),
        )
        val expense = Transaction(
            "t2", TransactionType.EXPENSE, "rub", Money(2000_00, rub), Instant.parse("2026-11-07T09:21:33Z"),
            categoryId = "kids", note = "Коляска: аванс",
        )
        source.transactions.saveTransaction(transfer)
        source.transactions.saveTransaction(expense)
        source.budgets.saveBudget(Budget("b", "kids", YearMonth(2026, 11), Money(30_000_00, rub)))
        source.transactions.saveTransaction(
            Transaction("gone", TransactionType.EXPENSE, "rub", Money(1, rub), Instant.parse("2026-11-01T00:00:00Z")),
        )
        source.transactions.deleteTransaction("gone")

        val json = source.backup.exportJson()

        val target = Repos()
        val result = target.backup.importJson(json)
        assertEquals(BackupImportResult.Imported(accounts = 2, categories = 1, transactions = 2, budgets = 1), result)
        assertEquals(
            source.accounts.observeAccountsWithBalance().first(),
            target.accounts.observeAccountsWithBalance().first(),
        )
        assertEquals(listOf(expense, transfer), target.transactions.observeTransactions().first())
        assertEquals(source.categories.getCategory("kids"), target.categories.getCategory("kids"))
        assertEquals(1, target.budgets.observeBudgets(YearMonth(2026, 11)).first().size)

        // Importing the same file again does not duplicate anything.
        target.backup.importJson(json)
        assertEquals(2, target.transactions.observeTransactions().first().size)
    }

    @Test
    fun `rejects files that are not backups or are from a newer version`() = runTest {
        val repos = Repos()
        assertEquals(BackupImportResult.NotABackup, repos.backup.importJson("hello"))
        assertEquals(BackupImportResult.NotABackup, repos.backup.importJson("""{"format":"other","version":1}"""))
        assertEquals(
            BackupImportResult.UnsupportedVersion(99),
            repos.backup.importJson("""{"format":"co-shell-backup","version":99,"exportedAt":"x"}"""),
        )
    }

    @Test
    fun `invalid records abort the whole import`() = runTest {
        val repos = Repos()
        val json = """
            {"format":"co-shell-backup","version":1,"exportedAt":"2026-11-08T00:00:00Z",
             "accounts":[{"id":"a","name":"A","currency":"USD","type":"CARD"}],
             "transactions":[{"id":"t","type":"EXPENSE","accountId":"a",
                "amount":{"minor":-5,"currency":"USD"},"date":"2026-11-08T00:00:00Z"}]}
        """.trimIndent()
        assertIs<BackupImportResult.Corrupted>(repos.backup.importJson(json))
        assertEquals(emptyList(), repos.accounts.observeAccounts().first())
    }

    @Test
    fun `restoring over seeded defaults merges categories with the same name instead of duplicating`() = runTest {
        val source = Repos()
        source.accounts.saveAccount(Account("rub", "Тинькофф", rub))
        source.categories.saveCategory(Category("old-food", "Продукты", CategoryType.EXPENSE))
        source.categories.saveCategory(Category("old-gift-in", "Подарки", CategoryType.INCOME))
        source.categories.saveCategory(Category("old-fruit", "Фрукты", CategoryType.EXPENSE, parentId = "old-food"))
        val json = source.backup.exportJson()

        // Fresh install: defaults were seeded with new ids, and an operation was entered before restoring.
        val target = Repos()
        target.categories.saveCategory(Category("new-food", "продукты ", CategoryType.EXPENSE))
        target.categories.saveCategory(Category("new-gift-out", "Подарки", CategoryType.EXPENSE))
        target.categories.saveCategory(Category("new-gift-in", "Подарки", CategoryType.INCOME))
        target.categories.saveCategory(Category("new-fruit", "Фрукты", CategoryType.EXPENSE, parentId = "new-food"))
        target.categories.saveCategory(Category("new-veg", "Овощи", CategoryType.EXPENSE, parentId = "new-food"))
        target.accounts.saveAccount(Account("rub", "Тинькофф", rub))
        target.transactions.saveTransaction(
            Transaction(
                "t", TransactionType.EXPENSE, "rub", Money(100, rub), Instant.parse("2026-11-08T00:00:00Z"),
                categoryId = "new-fruit",
            ),
        )
        target.budgets.saveBudget(Budget("b", "new-food", YearMonth(2026, 11), Money(1000, rub)))

        target.backup.importJson(json)

        val ids = target.categories.observeCategories(includeArchived = true).first().map { it.id }.toSet()
        // The expense "Подарки" has no twin in the backup (the backup's one is income) — it stays.
        assertEquals(setOf("old-food", "old-gift-in", "old-fruit", "new-gift-out", "new-veg"), ids)
        assertEquals("old-food", target.categories.getCategory("new-veg")?.parentId)
        assertEquals("old-fruit", target.transactions.getTransaction("t")?.categoryId)
        assertEquals("old-food", target.budgets.observeBudgets(YearMonth(2026, 11)).first().single().categoryId)

        // Restoring again changes nothing.
        target.backup.importJson(json)
        assertEquals(ids, target.categories.observeCategories(includeArchived = true).first().map { it.id }.toSet())
    }
}
