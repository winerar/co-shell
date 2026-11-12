package app.coshell.core.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query(
        """SELECT * FROM accounts WHERE deleted = 0 AND (:includeArchived OR archived = 0)
           ORDER BY archived, sort_order, name""",
    )
    fun observe(includeArchived: Boolean): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id AND deleted = 0")
    suspend fun get(id: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE deleted = 0")
    suspend fun getAll(): List<AccountEntity>

    @Upsert
    suspend fun upsert(entities: List<AccountEntity>)

    @Query("UPDATE accounts SET deleted = 1, pending_sync = 1, updated_at = :now, updated_by = :by WHERE id = :id")
    suspend fun markDeleted(id: String, now: Long, by: String?)
}

@Dao
interface CategoryDao {
    @Query(
        """SELECT * FROM categories WHERE deleted = 0 AND (:includeArchived OR archived = 0)
           AND (:type IS NULL OR type = :type) ORDER BY type, sort_order, name""",
    )
    fun observe(type: String?, includeArchived: Boolean): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id AND deleted = 0")
    suspend fun get(id: String): CategoryEntity?

    @Query("SELECT * FROM categories WHERE deleted = 0")
    suspend fun getAll(): List<CategoryEntity>

    @Query("SELECT COUNT(*) FROM categories WHERE deleted = 0")
    suspend fun count(): Int

    @Upsert
    suspend fun upsert(entities: List<CategoryEntity>)

    @Query("UPDATE categories SET deleted = 1, pending_sync = 1, updated_at = :now, updated_by = :by WHERE id = :id")
    suspend fun markDeleted(id: String, now: Long, by: String?)
}

@Dao
interface TransactionDao {
    @Query(
        """SELECT * FROM transactions WHERE deleted = 0
           AND (:accountId IS NULL OR account_id = :accountId OR to_account_id = :accountId)
           AND (:categoryId IS NULL OR category_id = :categoryId)
           AND (:fromMs IS NULL OR date >= :fromMs)
           AND (:toMs IS NULL OR date < :toMs)
           ORDER BY date DESC, id DESC
           LIMIT :limit""",
    )
    /** [limit] -1 means no limit (SQLite). */
    fun observe(accountId: String?, categoryId: String?, fromMs: Long?, toMs: Long?, limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id AND deleted = 0")
    suspend fun get(id: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE deleted = 0")
    suspend fun getAll(): List<TransactionEntity>

    @Query("SELECT COUNT(*) FROM transactions WHERE deleted = 0 AND (account_id = :accountId OR to_account_id = :accountId)")
    suspend fun countForAccount(accountId: String): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE deleted = 0 AND category_id = :categoryId")
    suspend fun countForCategory(categoryId: String): Int

    /**
     * Effect of live transactions on each account: expenses and outgoing transfers subtract
     * `amount`, incomes add it, incoming transfers add `to_amount`. An account may appear twice.
     */
    @Query(
        """SELECT account_id, SUM(CASE type WHEN 'INCOME' THEN amount_minor ELSE -amount_minor END) AS delta_minor
           FROM transactions WHERE deleted = 0 GROUP BY account_id
           UNION ALL
           SELECT to_account_id AS account_id, SUM(to_amount_minor) AS delta_minor
           FROM transactions WHERE deleted = 0 AND type = 'TRANSFER' GROUP BY to_account_id""",
    )
    fun observeBalanceDeltas(): Flow<List<BalanceDelta>>

    @Upsert
    suspend fun upsert(entities: List<TransactionEntity>)

    @Query("UPDATE transactions SET deleted = 1, pending_sync = 1, updated_at = :now, updated_by = :by WHERE id = :id")
    suspend fun markDeleted(id: String, now: Long, by: String?)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE deleted = 0")
    fun observeAll(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE deleted = 0")
    suspend fun getAll(): List<BudgetEntity>

    @Upsert
    suspend fun upsert(entities: List<BudgetEntity>)

    @Query("UPDATE budgets SET deleted = 1, pending_sync = 1, updated_at = :now, updated_by = :by WHERE id IN (:ids)")
    suspend fun markDeleted(ids: List<String>, now: Long, by: String?)

    /** Deletes first: a saved row may reuse the id of a deleted one. */
    @Transaction
    suspend fun apply(save: List<BudgetEntity>, deleteIds: List<String>, now: Long, by: String?) {
        if (deleteIds.isNotEmpty()) markDeleted(deleteIds, now, by)
        if (save.isNotEmpty()) upsert(save)
    }
}

/** Multi-table writes that must be atomic. */
@Dao
interface BackupDao {
    @Upsert suspend fun upsertAccounts(entities: List<AccountEntity>)
    @Upsert suspend fun upsertCategories(entities: List<CategoryEntity>)
    @Upsert suspend fun upsertTransactions(entities: List<TransactionEntity>)
    @Upsert suspend fun upsertBudgets(entities: List<BudgetEntity>)
    @Upsert suspend fun upsertManualRates(entities: List<ManualRateEntity>)
    @Upsert suspend fun upsertTemplates(entities: List<TemplateEntity>)
    @Upsert suspend fun upsertSettings(entities: List<SettingEntity>)

    @Transaction
    suspend fun upsertAll(
        accounts: List<AccountEntity>,
        categories: List<CategoryEntity>,
        transactions: List<TransactionEntity>,
        budgets: List<BudgetEntity>,
        manualRates: List<ManualRateEntity>,
        templates: List<TemplateEntity>,
        settings: List<SettingEntity> = emptyList(),
    ) {
        upsertAccounts(accounts)
        upsertCategories(categories)
        upsertTransactions(transactions)
        upsertBudgets(budgets)
        upsertManualRates(manualRates)
        upsertTemplates(templates)
        upsertSettings(settings)
    }
}
