package app.coshell.core.data

import app.coshell.core.data.db.CoShellDatabase
import app.coshell.core.data.db.createInMemoryCoShellDatabase
import app.coshell.core.data.repository.RoomCategoryRepository
import app.coshell.core.data.repository.RoomDataWipeRepository
import app.coshell.core.data.repository.RoomImportRepository
import app.coshell.core.data.repository.WriteStamper
import app.coshell.core.domain.DataCounts
import app.coshell.core.domain.SeedDefaultCategoriesUseCase
import app.coshell.core.domain.WipeAllDataUseCase
import app.coshell.core.domain.importer.ImportWrite
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

class DataWipeTest {
    private val usd = CurrencyCode("USD")
    private val clock = object : Clock {
        override fun now() = Instant.parse("2026-11-10T12:00:00Z")
    }
    private val db: CoShellDatabase = createInMemoryCoShellDatabase()
    private val stamper = WriteStamper(clock) { "member-1" }
    private val imports = RoomImportRepository(db.importDao(), stamper)
    private val categories = RoomCategoryRepository(db.categoryDao(), stamper)
    private val wipe = RoomDataWipeRepository(db.wipeDao(), stamper)
    private var n = 0
    private val useCase = WipeAllDataUseCase(wipe, SeedDefaultCategoriesUseCase(categories, IdGenerator { "seed-${n++}" }))

    @AfterTest
    fun tearDown() = db.close()

    private fun write(vararg ext: String) = ImportWrite(
        accounts = listOf(Account("a", "Карта", usd)),
        categories = listOf(Category("c", "Еда", CategoryType.EXPENSE)),
        transactions = ext.map {
            Transaction(
                id = "t-$it", type = TransactionType.EXPENSE, accountId = "a", amount = Money(100, usd),
                date = Instant.parse("2026-11-01T10:00:00Z"), categoryId = "c",
                importSource = "wallet", importExternalId = it, importBatchId = "b1",
            )
        },
    )

    @Test
    fun `wipe tombstones everything for sync, frees import ids and seeds categories again`() = runTest {
        imports.save(write("w1", "w2"))
        assertEquals(DataCounts(accounts = 1, transactions = 2, categories = 1, manualRates = 0), wipe.counts())

        assertTrue(useCase { true })

        val counts = wipe.counts()
        assertEquals(0, counts.accounts)
        assertEquals(0, counts.transactions)
        assertTrue(counts.categories > 1, "default categories are back")
        assertEquals(emptySet(), imports.knownExternalIds("wallet"))
        val pending = db.syncDao().pending()
        assertTrue(pending.accounts.single().deleted)
        assertTrue(pending.transactions.all { it.deleted && it.updatedBy == "member-1" })

        // The same Wallet file imports in full again.
        imports.save(write("w1", "w2").copy(transactions = write("w1", "w2").transactions.map { it.copy(id = it.id + "-new") }))
        assertEquals(2, wipe.counts().transactions)
    }

    @Test
    fun `failed copy deletes nothing`() = runTest {
        imports.save(write("w1"))
        assertFalse(useCase { false })
        assertEquals(1, wipe.counts().transactions)
    }
}
