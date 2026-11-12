package app.coshell.core.data.integrity

import app.coshell.core.data.db.AccountEntity
import app.coshell.core.data.db.BudgetEntity
import app.coshell.core.data.db.CategoryEntity
import app.coshell.core.data.db.TransactionEntity
import app.coshell.core.data.db.createInMemoryCoShellDatabase
import app.coshell.core.data.integrity.IntegrityProblem.Kind
import app.coshell.core.data.sync.FakeRemoteStore
import app.coshell.core.data.sync.SyncEngine
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IntegrityCheckTest {
    private val db = createInMemoryCoShellDatabase()
    private val check = IntegrityCheck(db.integrityDao())

    private fun account(id: String, currency: String = "ARS", deleted: Boolean = false) = AccountEntity(
        id = id, name = id, currency = currency, type = "CASH", initialBalanceMinor = 0, isShared = true,
        ownerMemberId = null, archived = false, sortOrder = 0, updatedAt = 1, updatedBy = null, deleted = deleted,
    )

    private fun category(id: String, parent: String? = null, deleted: Boolean = false) = CategoryEntity(
        id = id, name = id, type = "EXPENSE", icon = null, color = null, parentId = parent, archived = false,
        sortOrder = 0, updatedAt = 1, updatedBy = null, deleted = deleted,
    )

    private fun tx(
        id: String,
        account: String = "cash",
        currency: String = "ARS",
        category: String? = "food",
        to: String? = null,
        toCurrency: String? = null,
        deleted: Boolean = false,
    ) = TransactionEntity(
        id = id, type = if (to != null) "TRANSFER" else "EXPENSE", accountId = account, amountMinor = 100,
        currency = currency, toAccountId = to, toAmountMinor = to?.let { 100 }, toCurrency = toCurrency,
        categoryId = if (to != null) null else category, date = 1, note = "", payee = null, labels = "",
        originalAmountMinor = null, originalCurrency = null, authorMemberId = null, importSource = null,
        importExternalId = null, updatedAt = 1, updatedBy = null, deleted = deleted,
    )

    private suspend fun seed() {
        val sync = db.syncDao()
        sync.upsertAccounts(listOf(account("cash"), account("usd", "USD"), account("gone", deleted = true)))
        sync.upsertCategories(listOf(category("food"), category("folder"), category("sub", "folder"), category("old", deleted = true)))
    }

    @Test
    fun `consistent data has no problems`() = runTest {
        seed()
        db.syncDao().upsertTransactions(listOf(tx("t1"), tx("t2", to = "usd", toCurrency = "USD"), tx("t3", account = "gone", deleted = true)))
        assertTrue(check.run().isEmpty())
    }

    @Test
    fun `finds rows that point at deleted or unknown records`() = runTest {
        seed()
        db.syncDao().upsertTransactions(
            listOf(
                tx("onGone", account = "gone"),
                tx("onUnknown", account = "nobody"),
                tx("toGone", to = "gone", toCurrency = "ARS"),
                tx("inOld", category = "old"),
                tx("wrongCurrency", currency = "USD"),
                tx("wrongTarget", to = "usd", toCurrency = "ARS"),
            ),
        )
        db.syncDao().upsertCategories(listOf(category("orphan", parent = "old")))
        db.syncDao().upsertBudgets(listOf(BudgetEntity("b1", "old", "2026-11", 100, "ARS", updatedAt = 1, updatedBy = null)))

        val found = check.run().associate { it.kind to it.ids.toSet() }

        assertEquals(setOf("onGone", "onUnknown"), found[Kind.TRANSACTION_ON_MISSING_ACCOUNT])
        assertEquals(setOf("toGone"), found[Kind.TRANSFER_TO_MISSING_ACCOUNT])
        assertEquals(setOf("inOld"), found[Kind.TRANSACTION_ON_MISSING_CATEGORY])
        assertEquals(setOf("wrongCurrency"), found[Kind.CURRENCY_NOT_OF_ACCOUNT])
        assertEquals(setOf("wrongTarget"), found[Kind.TRANSFER_CURRENCY_NOT_OF_ACCOUNT])
        assertEquals(setOf("b1"), found[Kind.BUDGET_ON_MISSING_CATEGORY])
        assertEquals(setOf("orphan"), found[Kind.CATEGORY_IN_MISSING_FOLDER])
    }

    @Test
    fun `sync keeps the findings and the time they were first seen`() = runTest {
        seed()
        db.syncDao().upsertTransactions(listOf(tx("onGone", account = "gone")))
        var now = Instant.parse("2026-11-12T10:00:00Z")
        val clock = object : Clock { override fun now() = now }
        val engine = SyncEngine(db.syncDao(), FakeRemoteStore(), clock, check)

        engine.sync()
        val first = db.syncDao().state(SyncEngine.INTEGRITY)
        assertEquals("${now.toEpochMilliseconds()}|transaction on a missing account: 1 (onGone)", first)
        now = Instant.parse("2026-11-12T11:00:00Z")
        engine.sync()
        assertEquals(first, db.syncDao().state(SyncEngine.INTEGRITY))
    }

    @Test
    fun `a problem reads as one short line`() {
        val line = IntegrityProblem(Kind.TRANSFER_TO_MISSING_ACCOUNT, listOf("a", "b", "c", "d")).describe()
        assertEquals("transfer to a missing account: 4 (a, b, c)", line)
    }
}
