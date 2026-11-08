package app.coshell.backup

import app.coshell.core.domain.BackupImportResult
import app.coshell.core.domain.BackupRepository
import app.coshell.testing.MainDispatcherRule
import java.io.IOException
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class BackupViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val backup = FakeBackupRepository()
    private val storage = FakeDocumentStorage()

    // 2026-11-08 01:00 UTC is still 2026-11-07 in Buenos Aires (UTC-3).
    private val clock = object : Clock {
        override fun now() = Instant.parse("2026-11-08T01:00:00Z")
    }
    private val vm = BackupViewModel(backup, storage, clock, TimeZone.of("America/Argentina/Buenos_Aires"))

    @Test
    fun `file name uses the local date`() {
        assertEquals("co-shell-2026-11-07.json", vm.suggestedFileName())
        assertEquals("co-shell-2026-01-05.json", backupFileName(LocalDate(2026, 1, 5)))
    }

    @Test
    fun `export writes the json to the picked file`() {
        vm.exportTo("content://out")

        assertEquals(mapOf("content://out" to "{backup}"), storage.files)
        assertEquals(BackupMessage.Exported, vm.state.value.message)
        assertFalse(vm.state.value.isBusy)
    }

    @Test
    fun `export reports a write failure`() {
        storage.failWrites = true
        vm.exportTo("content://out")
        assertEquals(BackupMessage.WriteFailed, vm.state.value.message)
    }

    @Test
    fun `import waits for confirmation`() {
        storage.files["content://in"] = "{file}"
        vm.onImportFilePicked("content://in")

        assertEquals("content://in", vm.state.value.pendingImportUri)
        assertNull(backup.imported)

        vm.confirmImport()
        assertEquals("{file}", backup.imported)
        assertNull(vm.state.value.pendingImportUri)
        assertEquals(BackupMessage.Imported(1, 2, 3), vm.state.value.message)
    }

    @Test
    fun `cancelled import changes nothing`() {
        vm.onImportFilePicked("content://in")
        vm.cancelImport()
        vm.confirmImport()

        assertNull(backup.imported)
        assertNull(vm.state.value.pendingImportUri)
        assertNull(vm.state.value.message)
    }

    @Test
    fun `import errors become messages`() {
        vm.onImportFilePicked("content://missing")
        vm.confirmImport()
        assertEquals(BackupMessage.ReadFailed, vm.state.value.message)

        assertEquals(BackupMessage.NotABackup, BackupImportResult.NotABackup.toMessage())
        assertEquals(BackupMessage.UnsupportedVersion(9), BackupImportResult.UnsupportedVersion(9).toMessage())
        assertEquals(BackupMessage.Corrupted, BackupImportResult.Corrupted("bad").toMessage())
    }
}

private class FakeBackupRepository : BackupRepository {
    var imported: String? = null

    override suspend fun exportJson() = "{backup}"

    override suspend fun importJson(json: String): BackupImportResult {
        imported = json
        return BackupImportResult.Imported(accounts = 1, categories = 2, transactions = 3, budgets = 0)
    }
}

private class FakeDocumentStorage : DocumentStorage {
    val files = mutableMapOf<String, String>()
    var failWrites = false

    override suspend fun writeText(uri: String, text: String) {
        if (failWrites) throw IOException("read-only")
        files[uri] = text
    }

    override suspend fun readText(uri: String) = files[uri] ?: throw IOException("not found")
}
