package app.coshell.core.data.scale

import androidx.room.Room
import app.coshell.core.data.db.CoShellDatabase
import app.coshell.core.data.db.configureCommon
import app.coshell.core.data.repository.RoomAccountRepository
import app.coshell.core.data.repository.RoomCategoryRepository
import app.coshell.core.data.repository.RoomImportRepository
import app.coshell.core.data.repository.RoomTransactionRepository
import app.coshell.core.data.repository.WriteStamper
import app.coshell.core.data.sync.FakeRemoteStore
import app.coshell.core.data.sync.SyncEngine
import app.coshell.core.domain.EnsureAdjustmentCategoriesUseCase
import app.coshell.core.domain.importer.wallet.WalletAccountMatcher
import app.coshell.core.domain.importer.wallet.WalletCategoryMatcher
import app.coshell.core.domain.importer.wallet.WalletCsvParser
import app.coshell.core.domain.importer.wallet.WalletImportChoices
import app.coshell.core.domain.importer.wallet.WalletImportUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.measureTimedValue

/**
 * #131: a 10 000-row Wallet export end to end on a file database — parse, plan, save, list, sync.
 * Checks that nothing is lost at scale and prints the timings (JVM on a desktop CPU: a phone is
 * several times slower; numbers and targets — docs/agents/notes.md, performance.md).
 */
class ImportScaleTest {
    private val dir = File(System.getProperty("java.io.tmpdir"), "coshell-scale-${System.nanoTime()}").apply { mkdirs() }
    private val file = File(dir, "coshell.db")
    private val db: CoShellDatabase = Room.databaseBuilder<CoShellDatabase>(file.absolutePath).configureCommon()
    private val stamper = WriteStamper { "member-a" }
    private val accounts = RoomAccountRepository(db.accountDao(), db.transactionDao(), stamper)
    private val transactions = RoomTransactionRepository(db.transactionDao(), stamper)
    private val imports = RoomImportRepository(db.importDao(), stamper)
    private val categories = RoomCategoryRepository(db.categoryDao(), stamper)
    private val useCase = WalletImportUseCase(accounts, imports, EnsureAdjustmentCategoriesUseCase(categories))

    @AfterTest
    fun tearDown() { db.close(); dir.deleteRecursively() }

    @Test
    fun `ten thousand Wallet rows import, list and sync without loss`() = runBlocking {
        val csv = WalletExportGenerator.generate(ROWS)

        val (parsed, parseTime) = measureTimedValue { WalletCsvParser.parse(csv) }
        assertEquals(emptyList(), parsed.errors)
        assertEquals(ROWS, parsed.records.size)
        val records = parsed.records
        val choices = WalletImportChoices(
            accounts = WalletAccountMatcher.suggest(WalletAccountMatcher.accounts(records), emptyList()),
            categories = WalletCategoryMatcher.suggest(WalletCategoryMatcher.keys(records), emptyList()),
            timeZone = TimeZone.UTC,
        )
        val (plan, planTime) = measureTimedValue { useCase.preview(records, choices) }
        assertEquals(emptyList(), plan.issues)
        val (_, saveTime) = measureTimedValue { useCase.save(plan) }
        val operations = plan.write.transactions.size

        val (list, listTime) = measureTimedValue { transactions.observeTransactions().first() }
        assertEquals(operations, list.size)
        val (balances, balanceTime) = measureTimedValue { accounts.observeAccountsWithBalance().first() }
        assertEquals(plan.balancesAfter, balances.associate { it.account.id to it.balance })

        val (again, reimportTime) = measureTimedValue { useCase.preview(records, choices) }
        assertEquals(0, again.newCount, "a second import of the same file adds nothing")

        val server = FakeRemoteStore()
        val (synced, syncTime) = measureTimedValue { SyncEngine(db.syncDao(), server).sync() }
        assertTrue(synced.pushed >= operations)
        assertEquals(0, db.syncDao().observePendingCount().first())

        db.close() // flush the WAL into the main file before measuring it
        val sizeKb = dir.walk().filter { it.isFile }.sumOf { it.length() } / 1024
        println(
            "SCALE rows=$ROWS operations=$operations csv=${csv.length / 1024}KB parse=$parseTime plan=$planTime " +
                "save=$saveTime list=$listTime balances=$balanceTime reimportPlan=$reimportTime " +
                "sync=$syncTime (pushes=${server.pushCalls}) db=${sizeKb}KB",
        )
    }

    private companion object {
        const val ROWS = 10_000
    }
}
