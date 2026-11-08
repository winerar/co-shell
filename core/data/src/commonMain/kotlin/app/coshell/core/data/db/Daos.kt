package app.coshell.core.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE deleted = 0 AND (:includeArchived OR archived = 0) ORDER BY name")
    fun observeAll(includeArchived: Boolean): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id AND deleted = 0")
    suspend fun get(id: String): AccountEntity?

    @Upsert
    suspend fun upsert(entity: AccountEntity)

    @Query("UPDATE accounts SET deleted = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun markDeleted(id: String, updatedAt: Long)
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE deleted = 0 ORDER BY name")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id AND deleted = 0")
    suspend fun get(id: String): CategoryEntity?

    @Upsert
    suspend fun upsert(entity: CategoryEntity)

    @Query("UPDATE categories SET deleted = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun markDeleted(id: String, updatedAt: Long)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE deleted = 0 ORDER BY date DESC, updated_at DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query(
        "SELECT * FROM transactions WHERE deleted = 0 AND (account_id = :accountId OR to_account_id = :accountId) " +
            "ORDER BY date DESC, updated_at DESC",
    )
    fun observeForAccount(accountId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id AND deleted = 0")
    suspend fun get(id: String): TransactionEntity?

    @Query(
        "SELECT * FROM transactions WHERE deleted = 0 AND import_source = :source " +
            "AND import_external_id = :externalId",
    )
    suspend fun findImported(source: String, externalId: String): TransactionEntity?

    @Query("SELECT COUNT(*) FROM transactions WHERE deleted = 1")
    suspend fun countDeleted(): Int

    @Upsert
    suspend fun upsert(entity: TransactionEntity)

    @Query("UPDATE transactions SET deleted = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun markDeleted(id: String, updatedAt: Long)
}
