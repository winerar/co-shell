package app.coshell.importer.wallet

import app.coshell.backup.DocumentStorage
import app.coshell.core.domain.EnsureAdjustmentCategoriesUseCase
import app.coshell.core.domain.importer.ImportBatch
import app.coshell.core.domain.importer.ImportRepository
import app.coshell.core.domain.importer.ImportWrite
import app.coshell.core.domain.importer.wallet.AccountChoice
import app.coshell.core.domain.importer.wallet.UnpairedChoice
import app.coshell.core.domain.importer.wallet.WalletImportUseCase
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.SystemCategories
import app.coshell.core.model.Transaction
import app.coshell.testing.FakeAccountRepository
import app.coshell.testing.FakeCategoryRepository
import app.coshell.testing.MainDispatcherRule
import java.io.IOException
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WalletImportViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val accounts = FakeAccountRepository()
    private val categories = FakeCategoryRepository()
    private val imports = FakeImportRepository(accounts)
    private val storage = MapStorage()
    private var backupOk = true
    private var backups = 0
    private var n = 0
    private val useCase = WalletImportUseCase(accounts, imports, EnsureAdjustmentCategoriesUseCase(categories), ids = { "id${n++}" })

    // Lazy: init {} starts collecting in viewModelScope, which must see the rule's Main dispatcher.
    private val vm by lazy {
        WalletImportViewModel(
            storage, accounts, categories, imports, useCase,
            preImportBackup = { backups++; backupOk }, timeZone = TimeZone.UTC, moneyFormat = MoneyFormat.RU,
        ).also { it.work = Dispatchers.Main }
    }

    private val csv = """
        account;category;currency;amount;ref_currency_amount;note;date;transfer;payee;labels
        Карта USD;Еда;USD;-10.00;-3800.00;;2026-11-08 11:23:53;false;;
        Карта USD;Зарплата;USD;500.00;190000.00;;2026-11-01 10:00:00;false;;
        Карта USD;TRANSFER;USD;-20.00;-7600.00;;2026-10-18 10:05:42;true;;
    """.trimIndent()

    private fun pick() {
        storage.files["content://w"] = csv
        vm.onFilePicked("content://w")
    }

    private fun walkToPreview() {
        pick()
        repeat(4) { vm.next() } // summary → accounts → categories → transfers → preview
    }

    @Test
    fun `a picked file opens the summary with suggestions`() {
        pick()
        val s = vm.state.value
        assertEquals(WalletImportStep.SUMMARY, s.step)
        assertEquals(3, s.draft?.summary?.records)
        assertEquals(AccountChoice.CreateNew("Карта USD", CurrencyCode("USD")), s.draft?.accountChoices?.get("Карта USD"))
        assertTrue(WalletImportStep.TRANSFERS in s.steps)
    }

    @Test
    fun `a file that is not a Wallet export is reported`() {
        storage.files["content://x"] = "hello;world\n1;2"
        vm.onFilePicked("content://x")
        assertEquals(WalletImportError.NOT_WALLET, vm.state.value.error)
        assertEquals(WalletImportStep.START, vm.state.value.step)

        storage.failReads = true
        vm.onFilePicked("content://x")
        assertEquals(WalletImportError.READ_FAILED, vm.state.value.error)
    }

    @Test
    fun `an invalid wallet balance blocks the accounts step`() {
        pick()
        vm.next()
        vm.typeBalance("Карта USD", "abc")
        assertFalse(vm.state.value.canGoNext)
        vm.typeBalance("Карта USD", "470,00")
        assertTrue(vm.state.value.canGoNext)
    }

    @Test
    fun `preview counts new operations and the import saves them after a backup`() {
        walkToPreview()
        val plan = vm.state.value.plan
        assertNotNull(plan)
        assertEquals(WalletImportStep.PREVIEW, vm.state.value.step)
        assertEquals(3, plan!!.newCount)

        vm.next()
        assertEquals(WalletImportStep.DONE, vm.state.value.step)
        assertEquals(1, backups)
        assertEquals(3, imports.saved.size)
        // The unpaired transfer became a balance adjustment, so the system category now exists.
        assertNotNull(categories.items.value[SystemCategories.adjustmentId(app.coshell.core.model.CategoryType.EXPENSE)])
    }

    @Test
    fun `nothing is saved when the backup fails`() {
        backupOk = false
        walkToPreview()
        vm.next()
        assertEquals(WalletImportError.BACKUP_FAILED, vm.state.value.error)
        assertEquals(WalletImportStep.PREVIEW, vm.state.value.step)
        assertTrue(imports.saved.isEmpty())
    }

    @Test
    fun `importing the same file again finds nothing new`() {
        walkToPreview()
        vm.next()
        walkToPreview()
        assertEquals(0, vm.state.value.plan?.newCount)
        assertEquals(3, vm.state.value.plan?.alreadyImported)
        assertFalse(vm.state.value.canGoNext)
    }

    @Test
    fun `skipping the unpaired transfer leaves it out`() {
        pick()
        val line = vm.state.value.draft!!.unpaired.single().line
        vm.chooseUnpaired(line, UnpairedChoice.Skip)
        repeat(4) { vm.next() }
        assertEquals(2, vm.state.value.plan?.newCount)
        assertEquals(1, vm.state.value.plan?.skipped)
    }

    @Test
    fun `undo right after the import removes the batch and starts over`() {
        walkToPreview()
        vm.next()
        vm.confirmUndo()
        assertTrue(imports.saved.isEmpty())
        assertEquals(WalletImportStep.START, vm.state.value.step)
        assertEquals(3, vm.state.value.undone)
    }

    @Test
    fun `an earlier batch is undone only after confirmation`() {
        walkToPreview()
        vm.next()
        val batch = vm.state.value.batches.single()
        vm.requestUndo(batch)
        assertEquals(batch, vm.state.value.pendingUndo)
        vm.cancelUndo()
        assertEquals(3, imports.saved.size)
        vm.requestUndo(batch)
        vm.confirmUndo()
        assertNull(vm.state.value.pendingUndo)
        assertTrue(imports.saved.isEmpty())
    }

    @Test
    fun `back walks the steps and leaves from the first one`() {
        pick()
        vm.next()
        assertTrue(vm.back())
        assertEquals(WalletImportStep.SUMMARY, vm.state.value.step)
        assertTrue(vm.back())
        assertEquals(WalletImportStep.START, vm.state.value.step)
        assertFalse(vm.back())
    }
}

private class MapStorage : DocumentStorage {
    val files = mutableMapOf<String, String>()
    var failReads = false
    override suspend fun writeText(uri: String, text: String) { files[uri] = text }
    override suspend fun readText(uri: String): String = if (failReads) throw IOException("no") else files.getValue(uri)
}

/** Keeps saved operations in memory and passes saved accounts to [accounts]. */
private class FakeImportRepository(private val accounts: FakeAccountRepository) : ImportRepository {
    private val ops = MutableStateFlow<List<Transaction>>(emptyList())
    val saved get() = ops.value

    override suspend fun knownExternalIds(source: String) = ops.value.mapNotNull { it.importExternalId }.toSet()
    override suspend fun save(write: ImportWrite) {
        write.accounts.forEach { accounts.saveAccount(it) }
        ops.value += write.transactions
    }
    override fun observeBatches() = ops.map { list ->
        list.groupBy { it.importBatchId!! }.map { (id, rows) ->
            ImportBatch(id, "wallet", rows.size, rows.minOf { it.date }, rows.maxOf { it.date }, Instant.fromEpochMilliseconds(0))
        }
    }
    override suspend fun undoBatch(batchId: String): Int {
        val (gone, kept) = ops.value.partition { it.importBatchId == batchId }
        ops.value = kept
        return gone.size
    }
}
