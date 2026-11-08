package app.coshell.core.domain

import app.coshell.core.domain.repository.AccountRepository
import app.coshell.core.domain.repository.CategoryRepository
import app.coshell.core.domain.repository.TransactionRepository
import app.coshell.core.model.Account
import app.coshell.core.model.AccountId
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryId
import app.coshell.core.model.SyncMeta
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.time.Instant

val testMeta = SyncMeta(updatedAt = Instant.fromEpochSeconds(0))

class FakeAccountRepository : AccountRepository {
    val items = MutableStateFlow<Map<AccountId, Account>>(emptyMap())
    override fun observeAccounts(includeArchived: Boolean): Flow<List<Account>> = items.map { map ->
        map.values.filter { !it.meta.deleted && (includeArchived || !it.archived) }
    }
    override suspend fun getAccount(id: AccountId) = items.value[id]?.takeUnless { it.meta.deleted }
    override suspend fun upsert(account: Account) = items.update { it + (account.id to account) }
    override suspend fun delete(id: AccountId) = items.update { map ->
        map[id]?.let { map + (id to it.copy(meta = it.meta.copy(deleted = true))) } ?: map
    }
}

class FakeCategoryRepository : CategoryRepository {
    val items = MutableStateFlow<Map<CategoryId, Category>>(emptyMap())
    override fun observeCategories(): Flow<List<Category>> = items.map { it.values.toList() }
    override suspend fun getCategory(id: CategoryId) = items.value[id]
    override suspend fun upsert(category: Category) = items.update { it + (category.id to category) }
    override suspend fun delete(id: CategoryId) = items.update { it - id }
}

class FakeTransactionRepository : TransactionRepository {
    val items = MutableStateFlow<Map<TransactionId, Transaction>>(emptyMap())
    override fun observeTransactions(): Flow<List<Transaction>> = items.map { it.values.toList() }
    override fun observeTransactions(accountId: AccountId) = items.map { map ->
        map.values.filter { it.accountId == accountId || it.toAccountId == accountId }
    }
    override suspend fun getTransaction(id: TransactionId) = items.value[id]
    override suspend fun findImported(source: String, externalId: String) = items.value.values
        .firstOrNull { it.importSource == source && it.importExternalId == externalId }
    override suspend fun upsert(transaction: Transaction) =
        items.update { it + (transaction.id to transaction) }
    override suspend fun delete(id: TransactionId) = items.update { it - id }
}
