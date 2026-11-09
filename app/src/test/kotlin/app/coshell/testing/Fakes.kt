package app.coshell.testing

import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.TransactionFilter
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.model.Account
import app.coshell.core.model.AccountWithBalance
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/** Runs `viewModelScope` on an unconfined test dispatcher. */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(UnconfinedTestDispatcher())
    override fun finished(description: Description) = Dispatchers.resetMain()
}

class FakeAccountRepository : AccountRepository {
    val items = MutableStateFlow<Map<String, Account>>(emptyMap())
    override fun observeAccounts(includeArchived: Boolean): Flow<List<Account>> =
        items.map { all -> all.values.filter { includeArchived || !it.archived }.sortedBy { it.sortOrder } }
    override fun observeAccountsWithBalance(includeArchived: Boolean): Flow<List<AccountWithBalance>> =
        observeAccounts(includeArchived).map { list -> list.map { AccountWithBalance(it, it.initialBalance) } }
    override suspend fun getAccount(id: String) = items.value[id]
    override suspend fun saveAccount(account: Account) { items.value += account.id to account }
    override suspend fun deleteAccount(id: String) { items.value -= id }
}

class FakeCategoryRepository : CategoryRepository {
    val items = MutableStateFlow<Map<String, Category>>(emptyMap())
    override fun observeCategories(type: CategoryType?, includeArchived: Boolean): Flow<List<Category>> =
        items.map { all ->
            all.values.filter { (type == null || it.type == type) && (includeArchived || !it.archived) }
                .sortedBy { it.sortOrder }
        }
    override suspend fun getCategory(id: String) = items.value[id]
    override suspend fun saveCategory(category: Category) { items.value += category.id to category }
    override suspend fun deleteCategory(id: String) { items.value -= id }
    override suspend fun countCategories() = items.value.size
    override suspend fun saveCategories(categories: List<Category>) = categories.forEach { saveCategory(it) }
}

class FakeTransactionRepository : TransactionRepository {
    val items = MutableStateFlow<Map<String, Transaction>>(emptyMap())
    override fun observeTransactions(filter: TransactionFilter): Flow<List<Transaction>> =
        items.map { all -> all.values.sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.id }).let { list -> filter.limit?.let(list::take) ?: list } }
    override suspend fun getTransaction(id: String) = items.value[id]
    override suspend fun saveTransaction(transaction: Transaction) { items.value += transaction.id to transaction }
    override suspend fun deleteTransaction(id: String) { items.value -= id }
    override suspend fun countForAccount(accountId: String) =
        items.value.values.count { it.accountId == accountId || it.toAccountId == accountId }
    override suspend fun countForCategory(categoryId: String) =
        items.value.values.count { it.categoryId == categoryId }
}
