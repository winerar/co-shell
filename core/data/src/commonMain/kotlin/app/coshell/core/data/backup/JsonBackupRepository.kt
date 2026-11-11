package app.coshell.core.data.backup

import app.coshell.core.data.db.BudgetEntity
import app.coshell.core.data.db.CategoryEntity
import app.coshell.core.data.db.CoShellDatabase
import app.coshell.core.data.db.TemplateEntity
import app.coshell.core.data.db.TransactionEntity
import app.coshell.core.data.repository.Stamp
import app.coshell.core.data.repository.WriteStamper
import app.coshell.core.data.repository.toManualEntity
import app.coshell.core.data.repository.toEntity
import app.coshell.core.data.repository.toModel
import app.coshell.core.domain.BackupImportResult
import app.coshell.core.domain.BackupRepository
import app.coshell.core.model.Account
import app.coshell.core.model.Budget
import app.coshell.core.model.Category
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionTemplate
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    // Both directions run on Default: thousands of rows (Wallet import) must not block the UI thread.
    override suspend fun exportJson(): String = withContext(Dispatchers.Default) {
        val file = BackupFile(
            exportedAt = clock.now().toString(),
            accounts = database.accountDao().getAll().map { it.toModel().toDto() },
            categories = database.categoryDao().getAll().map { it.toModel().toDto() },
            transactions = database.transactionDao().getAll().map { it.toModel().toDto() },
            budgets = database.budgetDao().getAll().map { it.toModel().toDto() },
            manualRates = database.rateDao().getAllManual().map { it.toModel().toManualDto() },
            templates = database.templateDao().getAll().map { it.toModel().toDto() },
        )
        json.encodeToString(BackupFile.serializer(), file)
    }

    override suspend fun importJson(json: String): BackupImportResult = withContext(Dispatchers.Default) {
        importOnCaller(json)
    }

    private suspend fun importOnCaller(json: String): BackupImportResult {
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
                file.manualRates.map { it.toModel() },
                file.templates.map { it.toModel() },
            )
        } catch (e: IllegalArgumentException) {
            return BackupImportResult.Corrupted(e.message ?: "invalid record")
        }

        val stamp = stamper.stamp()
        val local = mergeLocalDuplicates(models, stamp)
        database.backupDao().upsertAll(
            accounts = models.accounts.map { it.toEntity(stamp) },
            categories = local.categories + models.categories.map { it.toEntity(stamp) },
            transactions = local.transactions + models.transactions.map { it.toEntity(stamp) },
            budgets = local.budgets + models.budgets.map { it.toEntity(stamp) },
            manualRates = models.manualRates.map { it.toManualEntity(stamp) },
            templates = local.templates + models.templates.map { it.toEntity(stamp) },
        )
        return BackupImportResult.Imported(
            models.accounts.size, models.categories.size, models.transactions.size, models.budgets.size,
            models.manualRates.size, models.templates.size,
        )
    }

    /**
     * Local rows to rewrite so that categories from the backup replace their local duplicates
     * ([findDuplicateCategories]): duplicates are soft-deleted, and local operations, budgets, templates and
     * subcategories that pointed at them move to the backup's category. Rows the backup itself
     * contains are left out — the backup overwrites them anyway.
     */
    private suspend fun mergeLocalDuplicates(models: Models, stamp: Stamp): LocalRows {
        val localCategories = database.categoryDao().getAll()
        val duplicates = findDuplicateCategories(localCategories.map { it.toModel() }, models.categories)
        if (duplicates.isEmpty()) return LocalRows()
        val incomingCategories = models.categories.map { it.id }.toSet()
        val incomingTransactions = models.transactions.map { it.id }.toSet()
        val incomingBudgets = models.budgets.map { it.id }.toSet()
        val incomingTemplates = models.templates.map { it.id }.toSet()
        fun replacement(categoryId: String?) = categoryId?.let(duplicates::get)

        val categories = localCategories.mapNotNull { category ->
            val newParent = replacement(category.parentId)
            when {
                category.id in duplicates -> category.copy(deleted = true).touched(stamp)
                category.id !in incomingCategories && newParent != null ->
                    category.copy(parentId = newParent).touched(stamp)
                else -> null
            }
        }
        val transactions = database.transactionDao().getAll()
            .filter { it.id !in incomingTransactions && replacement(it.categoryId) != null }
            .map { it.copy(categoryId = replacement(it.categoryId)).touched(stamp) }
        val budgets = database.budgetDao().getAll()
            .filter { it.id !in incomingBudgets && replacement(it.categoryId) != null }
            .map { it.copy(categoryId = duplicates.getValue(it.categoryId)).touched(stamp) }
        val templates = database.templateDao().getAll()
            .filter { it.id !in incomingTemplates && replacement(it.categoryId) != null }
            .map { it.copy(categoryId = replacement(it.categoryId)).touched(stamp) }
        return LocalRows(categories, transactions, budgets, templates)
    }

    private fun CategoryEntity.touched(stamp: Stamp) =
        copy(updatedAt = stamp.now, updatedBy = stamp.by, pendingSync = true)

    private fun TransactionEntity.touched(stamp: Stamp) =
        copy(updatedAt = stamp.now, updatedBy = stamp.by, pendingSync = true)

    private fun BudgetEntity.touched(stamp: Stamp) =
        copy(updatedAt = stamp.now, updatedBy = stamp.by, pendingSync = true)

    private fun TemplateEntity.touched(stamp: Stamp) =
        copy(updatedAt = stamp.now, updatedBy = stamp.by, pendingSync = true)

    private data class LocalRows(
        val categories: List<CategoryEntity> = emptyList(),
        val transactions: List<TransactionEntity> = emptyList(),
        val budgets: List<BudgetEntity> = emptyList(),
        val templates: List<TemplateEntity> = emptyList(),
    )

    private data class Models(
        val accounts: List<Account>,
        val categories: List<Category>,
        val transactions: List<Transaction>,
        val budgets: List<Budget>,
        val manualRates: List<ExchangeRate>,
        val templates: List<TransactionTemplate>,
    )
}
