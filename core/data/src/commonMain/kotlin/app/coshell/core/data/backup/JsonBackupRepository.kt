package app.coshell.core.data.backup

import app.coshell.core.data.db.CoShellDatabase
import app.coshell.core.data.repository.WriteStamper
import app.coshell.core.data.repository.toEntity
import app.coshell.core.data.repository.toModel
import app.coshell.core.domain.BackupImportResult
import app.coshell.core.domain.BackupRepository
import app.coshell.core.model.Account
import app.coshell.core.model.Budget
import app.coshell.core.model.Category
import app.coshell.core.model.Transaction
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Clock

class JsonBackupRepository(
    private val database: CoShellDatabase,
    private val stamper: WriteStamper,
    private val clock: Clock = Clock.System,
) : BackupRepository {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true // newer app versions may add fields
        encodeDefaults = true
    }

    override suspend fun exportJson(): String {
        val file = BackupFile(
            exportedAt = clock.now().toString(),
            accounts = database.accountDao().getAll().map { it.toModel().toDto() },
            categories = database.categoryDao().getAll().map { it.toModel().toDto() },
            transactions = database.transactionDao().getAll().map { it.toModel().toDto() },
            budgets = database.budgetDao().getAll().map { it.toModel().toDto() },
        )
        return json.encodeToString(BackupFile.serializer(), file)
    }

    override suspend fun importJson(json: String): BackupImportResult {
        val header = runCatching { this.json.parseToJsonElement(json).jsonObject }.getOrNull()
            ?: return BackupImportResult.NotABackup
        if (header["format"]?.jsonPrimitive?.content != BackupFile.FORMAT) return BackupImportResult.NotABackup
        val version = runCatching { header["version"]?.jsonPrimitive?.int }.getOrNull()
            ?: return BackupImportResult.NotABackup
        if (version > BackupFile.CURRENT_VERSION) return BackupImportResult.UnsupportedVersion(version)

        val file = try {
            this.json.decodeFromString(BackupFile.serializer(), json)
        } catch (e: SerializationException) {
            return BackupImportResult.Corrupted(e.message ?: "invalid JSON")
        }
        // Converting to models runs every domain invariant before anything is written.
        val models = try {
            Models(
                file.accounts.map { it.toModel() },
                file.categories.map { it.toModel() },
                file.transactions.map { it.toModel() },
                file.budgets.map { it.toModel() },
            )
        } catch (e: IllegalArgumentException) {
            return BackupImportResult.Corrupted(e.message ?: "invalid record")
        }

        val stamp = stamper.stamp()
        database.backupDao().upsertAll(
            accounts = models.accounts.map { it.toEntity(stamp) },
            categories = models.categories.map { it.toEntity(stamp) },
            transactions = models.transactions.map { it.toEntity(stamp) },
            budgets = models.budgets.map { it.toEntity(stamp) },
        )
        return BackupImportResult.Imported(
            models.accounts.size, models.categories.size, models.transactions.size, models.budgets.size,
        )
    }

    private data class Models(
        val accounts: List<Account>,
        val categories: List<Category>,
        val transactions: List<Transaction>,
        val budgets: List<Budget>,
    )
}
