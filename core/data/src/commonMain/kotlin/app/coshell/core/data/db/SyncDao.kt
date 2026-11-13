package app.coshell.core.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import app.coshell.core.data.sync.SyncKind
import app.coshell.core.data.sync.SyncRecord
import app.coshell.core.data.sync.Version
import app.coshell.core.data.sync.remoteWins
import kotlinx.coroutines.flow.Flow

/** Rows downloaded in one pull, grouped by table. */
data class PulledRows(
    val accounts: List<AccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val budgets: List<BudgetEntity> = emptyList(),
    val manualRates: List<ManualRateEntity> = emptyList(),
    val templates: List<TemplateEntity> = emptyList(),
    val settings: List<SettingEntity> = emptyList(),
    val tags: List<TagEntity> = emptyList(),
)

/** Rows waiting to be sent to the server. */
data class PendingRows(
    val accounts: List<AccountEntity>,
    val categories: List<CategoryEntity>,
    val transactions: List<TransactionEntity>,
    val budgets: List<BudgetEntity>,
    val manualRates: List<ManualRateEntity>,
    val templates: List<TemplateEntity>,
    val settings: List<SettingEntity>,
    val tags: List<TagEntity> = emptyList(),
) {
    val size get() = accounts.size + categories.size + transactions.size + budgets.size + manualRates.size + templates.size +
        settings.size + tags.size
}

/** Queries for SyncEngine. Unlike the screen DAOs they see soft-deleted rows too. */
@Dao
abstract class SyncDao {
    // --- pending rows ---
    @Query("SELECT * FROM accounts WHERE pending_sync = 1") abstract suspend fun pendingAccounts(): List<AccountEntity>
    @Query("SELECT * FROM categories WHERE pending_sync = 1") abstract suspend fun pendingCategories(): List<CategoryEntity>
    @Query("SELECT * FROM transactions WHERE pending_sync = 1") abstract suspend fun pendingTransactions(): List<TransactionEntity>
    @Query("SELECT * FROM budgets WHERE pending_sync = 1") abstract suspend fun pendingBudgets(): List<BudgetEntity>
    @Query("SELECT * FROM manual_rates WHERE pending_sync = 1") abstract suspend fun pendingManualRates(): List<ManualRateEntity>
    @Query("SELECT * FROM templates WHERE pending_sync = 1") abstract suspend fun pendingTemplates(): List<TemplateEntity>
    @Query("SELECT * FROM settings WHERE pending_sync = 1") abstract suspend fun pendingSettings(): List<SettingEntity>
    @Query("SELECT * FROM tags WHERE pending_sync = 1") abstract suspend fun pendingTags(): List<TagEntity>

    @Query(
        """SELECT (SELECT COUNT(*) FROM accounts WHERE pending_sync = 1) + (SELECT COUNT(*) FROM categories WHERE pending_sync = 1)
           + (SELECT COUNT(*) FROM transactions WHERE pending_sync = 1) + (SELECT COUNT(*) FROM budgets WHERE pending_sync = 1)
           + (SELECT COUNT(*) FROM manual_rates WHERE pending_sync = 1) + (SELECT COUNT(*) FROM templates WHERE pending_sync = 1)
           + (SELECT COUNT(*) FROM settings WHERE pending_sync = 1) + (SELECT COUNT(*) FROM tags WHERE pending_sync = 1)""",
    )
    abstract fun observePendingCount(): Flow<Int>

    @Transaction
    open suspend fun pending() = PendingRows(
        pendingAccounts(), pendingCategories(), pendingTransactions(), pendingBudgets(), pendingManualRates(), pendingTemplates(),
        pendingSettings(), pendingTags(),
    )

    // --- clear the flag only if the row was not edited again while it was being sent ---
    @Query("UPDATE accounts SET pending_sync = 0 WHERE id = :id AND updated_at = :updatedAt")
    abstract suspend fun clearAccount(id: String, updatedAt: Long)
    @Query("UPDATE categories SET pending_sync = 0 WHERE id = :id AND updated_at = :updatedAt")
    abstract suspend fun clearCategory(id: String, updatedAt: Long)
    @Query("UPDATE transactions SET pending_sync = 0 WHERE id = :id AND updated_at = :updatedAt")
    abstract suspend fun clearTransaction(id: String, updatedAt: Long)
    @Query("UPDATE budgets SET pending_sync = 0 WHERE id = :id AND updated_at = :updatedAt")
    abstract suspend fun clearBudget(id: String, updatedAt: Long)
    @Query("UPDATE manual_rates SET pending_sync = 0 WHERE id = :id AND updated_at = :updatedAt")
    abstract suspend fun clearManualRate(id: String, updatedAt: Long)
    @Query("UPDATE templates SET pending_sync = 0 WHERE id = :id AND updated_at = :updatedAt")
    abstract suspend fun clearTemplate(id: String, updatedAt: Long)
    @Query("UPDATE settings SET pending_sync = 0 WHERE id = :id AND updated_at = :updatedAt")
    abstract suspend fun clearSetting(id: String, updatedAt: Long)
    @Query("UPDATE tags SET pending_sync = 0 WHERE id = :id AND updated_at = :updatedAt")
    abstract suspend fun clearTag(id: String, updatedAt: Long)

    /**
     * Clears the flags of one pushed chunk in one transaction (#131): row by row, 10 000 imported
     * operations meant 10 000 commits and as many re-counts of [observePendingCount].
     */
    @Transaction
    open suspend fun clearSent(records: List<SyncRecord>) = records.forEach { r ->
        when (r.kind) {
            SyncKind.ACCOUNT -> clearAccount(r.id, r.updatedAt)
            SyncKind.CATEGORY -> clearCategory(r.id, r.updatedAt)
            SyncKind.TRANSACTION -> clearTransaction(r.id, r.updatedAt)
            SyncKind.BUDGET -> clearBudget(r.id, r.updatedAt)
            SyncKind.MANUAL_RATE -> clearManualRate(r.id, r.updatedAt)
            SyncKind.TEMPLATE -> clearTemplate(r.id, r.updatedAt)
            SyncKind.SETTING -> clearSetting(r.id, r.updatedAt)
            SyncKind.TAG -> clearTag(r.id, r.updatedAt)
        }
    }

    // --- local versions of incoming rows (including deleted) ---
    @Query("SELECT * FROM accounts WHERE id IN (:ids)") abstract suspend fun accounts(ids: List<String>): List<AccountEntity>
    @Query("SELECT * FROM categories WHERE id IN (:ids)") abstract suspend fun categories(ids: List<String>): List<CategoryEntity>
    @Query("SELECT * FROM transactions WHERE id IN (:ids)") abstract suspend fun transactions(ids: List<String>): List<TransactionEntity>
    @Query("SELECT * FROM budgets WHERE id IN (:ids)") abstract suspend fun budgets(ids: List<String>): List<BudgetEntity>
    @Query("SELECT * FROM manual_rates WHERE id IN (:ids)") abstract suspend fun manualRates(ids: List<String>): List<ManualRateEntity>
    @Query("SELECT * FROM templates WHERE id IN (:ids)") abstract suspend fun templates(ids: List<String>): List<TemplateEntity>
    @Query("SELECT * FROM settings WHERE id IN (:ids)") abstract suspend fun settings(ids: List<String>): List<SettingEntity>
    @Query("SELECT * FROM tags WHERE id IN (:ids)") abstract suspend fun tags(ids: List<String>): List<TagEntity>

    @Upsert abstract suspend fun upsertAccounts(rows: List<AccountEntity>)
    @Upsert abstract suspend fun upsertCategories(rows: List<CategoryEntity>)
    @Upsert abstract suspend fun upsertTransactions(rows: List<TransactionEntity>)
    @Upsert abstract suspend fun upsertBudgets(rows: List<BudgetEntity>)
    @Upsert abstract suspend fun upsertManualRates(rows: List<ManualRateEntity>)
    @Upsert abstract suspend fun upsertTemplates(rows: List<TemplateEntity>)
    @Upsert abstract suspend fun upsertSettings(rows: List<SettingEntity>)
    @Upsert abstract suspend fun upsertTags(rows: List<TagEntity>)

    @Query("SELECT COUNT(*) FROM transactions WHERE deleted = 0") abstract suspend fun liveTransactionCount(): Int

    // --- state ---
    @Query("SELECT value FROM sync_state WHERE `key` = :key") abstract suspend fun state(key: String): String?
    @Query("SELECT value FROM sync_state WHERE `key` = :key") abstract fun observeState(key: String): Flow<String?>
    @Upsert abstract suspend fun setState(row: SyncStateEntity)

    /**
     * Applies downloaded rows that win over local ones ([remoteWins]) and moves the cursor, in one
     * transaction: a local edit saved during the merge is either seen here or made after it.
     * Returns how many rows changed.
     */
    @Transaction
    open suspend fun merge(rows: PulledRows, cursorKey: String, cursor: String?): Int {
        var changed = 0
        fun <T> winners(remote: List<T>, local: List<T>, id: (T) -> String, v: (T) -> Version): List<T> {
            val byId = local.associateBy(id)
            return remote.filter { remoteWins(byId[id(it)]?.let(v), v(it)) }.also { changed += it.size }
        }
        upsertAccounts(winners(rows.accounts, accounts(rows.accounts.map { it.id }), { it.id }) { Version(it.updatedAt, it.updatedBy) })
        upsertCategories(winners(rows.categories, categories(rows.categories.map { it.id }), { it.id }) { Version(it.updatedAt, it.updatedBy) })
        upsertTransactions(winners(rows.transactions, transactions(rows.transactions.map { it.id }), { it.id }) { Version(it.updatedAt, it.updatedBy) })
        upsertBudgets(winners(rows.budgets, budgets(rows.budgets.map { it.id }), { it.id }) { Version(it.updatedAt, it.updatedBy) })
        upsertManualRates(winners(rows.manualRates, manualRates(rows.manualRates.map { it.id }), { it.id }) { Version(it.updatedAt, it.updatedBy) })
        upsertTemplates(winners(rows.templates, templates(rows.templates.map { it.id }), { it.id }) { Version(it.updatedAt, it.updatedBy) })
        upsertSettings(winners(rows.settings, settings(rows.settings.map { it.id }), { it.id }) { Version(it.updatedAt, it.updatedBy) })
        upsertTags(winners(rows.tags, tags(rows.tags.map { it.id }), { it.id }) { Version(it.updatedAt, it.updatedBy) })
        if (cursor != null) setState(SyncStateEntity(cursorKey, cursor))
        return changed
    }

    // --- replace local data with the family's (joining a family) ---
    @Query("DELETE FROM accounts") abstract suspend fun deleteAccounts()
    @Query("DELETE FROM categories") abstract suspend fun deleteCategories()
    @Query("DELETE FROM transactions") abstract suspend fun deleteTransactions()
    @Query("DELETE FROM budgets") abstract suspend fun deleteBudgets()
    @Query("DELETE FROM manual_rates") abstract suspend fun deleteManualRates()
    @Query("DELETE FROM templates") abstract suspend fun deleteTemplates()
    @Query("DELETE FROM settings") abstract suspend fun deleteSettings()
    @Query("DELETE FROM tags") abstract suspend fun deleteTags()
    @Query("DELETE FROM sync_state") abstract suspend fun deleteState()

    @Transaction
    open suspend fun wipeSyncedData() {
        deleteAccounts(); deleteCategories(); deleteTransactions(); deleteBudgets(); deleteManualRates(); deleteTemplates()
        deleteSettings(); deleteTags(); deleteState()
    }

    /** Marks every row as not yet sent — used when this phone's data must go to a (new) family. */
    @Query("UPDATE accounts SET pending_sync = 1") abstract suspend fun markAccountsPending()
    @Query("UPDATE categories SET pending_sync = 1") abstract suspend fun markCategoriesPending()
    @Query("UPDATE transactions SET pending_sync = 1") abstract suspend fun markTransactionsPending()
    @Query("UPDATE budgets SET pending_sync = 1") abstract suspend fun markBudgetsPending()
    @Query("UPDATE manual_rates SET pending_sync = 1") abstract suspend fun markManualRatesPending()
    @Query("UPDATE templates SET pending_sync = 1") abstract suspend fun markTemplatesPending()
    @Query("UPDATE settings SET pending_sync = 1") abstract suspend fun markSettingsPending()
    @Query("UPDATE tags SET pending_sync = 1") abstract suspend fun markTagsPending()

    @Transaction
    open suspend fun markAllPending() {
        markAccountsPending(); markCategoriesPending(); markTransactionsPending(); markBudgetsPending(); markManualRatesPending()
        markTemplatesPending(); markSettingsPending(); markTagsPending()
        deleteState()
    }
}
