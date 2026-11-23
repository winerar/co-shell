package app.coshell.core.data

import app.coshell.core.data.backup.JsonBackupRepository
import app.coshell.core.data.db.createInMemoryCoShellDatabase
import app.coshell.core.data.repository.RoomAccountRepository
import app.coshell.core.data.repository.RoomBudgetRepository
import app.coshell.core.data.repository.RoomCategoryRepository
import app.coshell.core.data.repository.RoomDataWipeRepository
import app.coshell.core.data.repository.RoomExchangeRateRepository
import app.coshell.core.data.repository.RoomFamilySettings
import app.coshell.core.data.repository.RoomTagRepository
import app.coshell.core.data.repository.RoomTemplateRepository
import app.coshell.core.data.repository.RoomTransactionRepository
import app.coshell.core.data.repository.WriteStamper
import app.coshell.core.domain.BackupImportResult
import app.coshell.core.domain.SeedDefaultCategoriesUseCase
import app.coshell.core.domain.WipeAllDataUseCase
import app.coshell.core.model.Account
import app.coshell.core.model.CreditTerms
import app.coshell.core.model.MinPayment
import app.coshell.core.model.AccountType
import app.coshell.core.model.Budget
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Money
import app.coshell.core.model.PostMode
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import app.coshell.core.model.Recurrence
import app.coshell.core.model.RecurringSchedule
import app.coshell.core.model.Tag
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * 1.0 check (#383): copy → «Стереть всё» → restore on the same phone gives back exactly the same data,
 * and the restored rows go to the partner by the usual sync. Compared through a second copy, so every
 * entity the backup format knows is covered.
 */
class BackupWipeRestoreTest {
    private val ars = CurrencyCode("ARS")
    private val usd = CurrencyCode("USD")
    private val usdt = CurrencyCode("USDT")
    private var now = Instant.parse("2026-11-15T10:00:00Z")
    private val clock = object : Clock {
        override fun now() = now
    }
    private var n = 0

    private val db = createInMemoryCoShellDatabase()
    private val stamper = WriteStamper(clock) { "member-1" }
    private val accounts = RoomAccountRepository(db.accountDao(), db.transactionDao(), stamper)
    private val categories = RoomCategoryRepository(db.categoryDao(), stamper)
    private val transactions = RoomTransactionRepository(db.transactionDao(), stamper)
    private val budgets = RoomBudgetRepository(db.budgetDao(), stamper)
    private val templates = RoomTemplateRepository(db.templateDao(), stamper)
    private val tags = RoomTagRepository(db.tagDao(), stamper)
    private val rates = RoomExchangeRateRepository(db.rateDao(), stamper)
    private val settings = RoomFamilySettings(db.settingDao(), stamper)
    private val backup = JsonBackupRepository(db, stamper, clock)
    private val seed = SeedDefaultCategoriesUseCase(categories, IdGenerator { "seed-${n++}" })
    private val wipeAll = WipeAllDataUseCase(RoomDataWipeRepository(db.wipeDao(), stamper), seed)

    @Test
    fun `copy then wipe then restore gives back the same data and syncs it`() = runTest {
        fillLikeAFamily()
        val copy = backup.exportJson()

        now = Instant.parse("2026-11-15T11:00:00Z")
        assertTrue(wipeAll { true })
        assertEquals(0, db.wipeDao().transactions())

        now = Instant.parse("2026-11-15T12:00:00Z")
        assertIs<BackupImportResult.Imported>(backup.importJson(copy))

        // Same clock as the first copy: only the data could differ.
        now = Instant.parse("2026-11-15T10:00:00Z")
        assertEquals(copy, backup.exportJson())

        // The wipe already went to the partner as tombstones; the restore must follow it.
        val pending = db.syncDao().pending()
        assertEquals(setOf("t-peso", "t-rent", "t-swap"), pending.transactions.filter { !it.deleted }.map { it.id }.toSet())
        assertTrue(pending.accounts.none { it.deleted })
        assertTrue(pending.transactions.all { it.updatedAt > Instant.parse("2026-11-15T11:00:00Z").toEpochMilliseconds() })
    }

    private suspend fun fillLikeAFamily() {
        seed() // a real phone starts with the default categories
        accounts.saveAccount(Account("galicia", "Galicia", ars, AccountType.CARD, initialBalanceMinor = 120_000_00))
        accounts.saveAccount(Account("bybit", "Bybit", usdt, AccountType.CRYPTO, color = 0xFF00E5C0))
        accounts.saveAccount(Account("cash", "Наличные $", usd, excludedFromTotal = true, cardDigits = "3900", prepaid = true, credit = CreditTerms(1_000_00, 5, MinPayment.Percent(1_000))))
        categories.saveCategory(Category("kids", "Дети", CategoryType.EXPENSE, icon = "kids", color = 0xFF9C27B0))
        tags.saveTag(Tag("trip", "Барилоче", 0xFF9D7BFF))
        transactions.saveTransaction(
            Transaction(
                "t-peso", TransactionType.EXPENSE, "bybit", Money(25_50, usdt), Instant.parse("2026-11-10T19:05:00Z"),
                categoryId = "kids", note = "Кружок", labels = listOf("Барилоче"),
            ),
        )
        transactions.saveTransaction(
            Transaction(
                "t-rent", TransactionType.EXPENSE, "galicia", Money(650_000_00, ars), Instant.parse("2026-11-05T09:00:00Z"),
                receiptKey = "30685849751:442:6:117817",
            ),
        )
        transactions.saveTransaction(
            Transaction(
                "t-swap", TransactionType.TRANSFER, "cash", Money(100_00, usd), Instant.parse("2026-11-01T12:00:00Z"),
                toAccountId = "galicia", toAmount = Money(145_000_00, ars),
            ),
        )
        budgets.applyBudgets(save = listOf(Budget("b-kids", "kids", YearMonth(2026, 11), Money(80_000_00, ars), rollover = true)))
        templates.saveTemplate(
            TransactionTemplate(
                "tpl-rent", "Аренда", TransactionType.EXPENSE, "galicia", ars, Money(650_000_00, ars),
                schedule = RecurringSchedule(Recurrence.Monthly(5), LocalDate(2026, 11, 1), PostMode.AUTO),
            ),
        )
        rates.saveManualRate(ExchangeRate(LocalDate(2026, 11, 1), usd, ars, Rate.parse("1450"), RateOrigin.MANUAL))
        settings.setCurrencies(listOf(ars, usd))
        settings.setMonthStartDay(5)
    }
}
