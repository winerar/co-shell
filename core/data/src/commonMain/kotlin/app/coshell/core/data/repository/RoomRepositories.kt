package app.coshell.core.data.repository

import app.coshell.core.data.db.AccountDao
import app.coshell.core.data.db.CategoryDao
import app.coshell.core.data.db.TransactionDao
import app.coshell.core.data.db.toEntity
import app.coshell.core.data.db.toModel
import app.coshell.core.domain.repository.AccountRepository
import app.coshell.core.domain.repository.CategoryRepository
import app.coshell.core.domain.repository.TransactionRepository
import app.coshell.core.model.Account
import app.coshell.core.model.AccountId
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryId
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

// These repositories serve local edits made by the user, so every write stamps
// `updatedAt = now` (last-write-wins input for sync). Sync will write through the DAOs
// directly, keeping the remote timestamp. Constructed by Hilt in :app (DataModule).

class RoomAccountRepository(
    private val dao: AccountDao,
    private val clock: Clock,
) : AccountRepository {
    override fun observeAccounts(includeArchived: Boolean): Flow<List<Account>> =
        dao.observeAll(includeArchived).map { list -> list.map { it.toModel() } }

    override suspend fun getAccount(id: AccountId): Account? = dao.get(id.value)?.toModel()

    override suspend fun upsert(account: Account) =
        dao.upsert(account.copy(meta = account.meta.copy(updatedAt = clock.now())).toEntity())

    override suspend fun delete(id: AccountId) =
        dao.markDeleted(id.value, clock.now().toEpochMilliseconds())
}

class RoomCategoryRepository(
    private val dao: CategoryDao,
    private val clock: Clock,
) : CategoryRepository {
    override fun observeCategories(): Flow<List<Category>> =
        dao.observeAll().map { list -> list.map { it.toModel() } }

    override suspend fun getCategory(id: CategoryId): Category? = dao.get(id.value)?.toModel()

    override suspend fun upsert(category: Category) =
        dao.upsert(category.copy(meta = category.meta.copy(updatedAt = clock.now())).toEntity())

    override suspend fun delete(id: CategoryId) =
        dao.markDeleted(id.value, clock.now().toEpochMilliseconds())
}

class RoomTransactionRepository(
    private val dao: TransactionDao,
    private val clock: Clock,
) : TransactionRepository {
    override fun observeTransactions(): Flow<List<Transaction>> =
        dao.observeAll().map { list -> list.map { it.toModel() } }

    override fun observeTransactions(accountId: AccountId): Flow<List<Transaction>> =
        dao.observeForAccount(accountId.value).map { list -> list.map { it.toModel() } }

    override suspend fun getTransaction(id: TransactionId): Transaction? = dao.get(id.value)?.toModel()

    override suspend fun findImported(source: String, externalId: String): Transaction? =
        dao.findImported(source, externalId)?.toModel()

    override suspend fun upsert(transaction: Transaction) = dao.upsert(
        transaction.copy(meta = transaction.meta.copy(updatedAt = clock.now())).toEntity(),
    )

    override suspend fun delete(id: TransactionId) =
        dao.markDeleted(id.value, clock.now().toEpochMilliseconds())
}
