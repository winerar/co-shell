package app.coshell.core.data.repository

import app.coshell.core.data.db.AccountDao
import app.coshell.core.data.db.BudgetDao
import app.coshell.core.data.db.CategoryDao
import app.coshell.core.data.db.TransactionDao
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.budgets.BudgetRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.TransactionFilter
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.model.Account
import app.coshell.core.model.AccountWithBalance
import app.coshell.core.model.Budget
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlin.time.Clock

/**
 * Supplies the sync stamp for local writes. `memberId` is `null` until auth exists (stage 4).
 */
class WriteStamper(
    private val clock: Clock = Clock.System,
    private val memberId: () -> String? = { null },
) {
    internal fun stamp() = Stamp(clock.now().toEpochMilliseconds(), memberId())
}

class RoomAccountRepository(
    private val accounts: AccountDao,
    private val transactions: TransactionDao,
    private val stamper: WriteStamper,
) : AccountRepository {

    override fun observeAccounts(includeArchived: Boolean): Flow<List<Account>> =
        accounts.observe(includeArchived).map { list -> list.map { it.toModel() } }

    override fun observeAccountsWithBalance(includeArchived: Boolean): Flow<List<AccountWithBalance>> =
        combine(observeAccounts(includeArchived), transactions.observeBalanceDeltas()) { list, deltas ->
            val byAccount = deltas.groupBy({ it.accountId }, { it.deltaMinor })
            list.map { account ->
                val balance = byAccount[account.id].orEmpty()
                    .fold(account.initialBalance) { acc, delta -> acc + Money(delta, account.currency) }
                AccountWithBalance(account, balance)
            }
        }

    override suspend fun getAccount(id: String): Account? = accounts.get(id)?.toModel()

    /** A personal account without an owner belongs to whoever saves it. */
    override suspend fun saveAccount(account: Account) {
        val stamp = stamper.stamp()
        val owned = if (!account.isShared && account.ownerMemberId == null) account.copy(ownerMemberId = stamp.by) else account
        accounts.upsert(listOf(owned.toEntity(stamp)))
    }

    override suspend fun deleteAccount(id: String) {
        val stamp = stamper.stamp()
        accounts.markDeleted(id, stamp.now, stamp.by)
    }
}

class RoomCategoryRepository(
    private val categories: CategoryDao,
    private val stamper: WriteStamper,
) : CategoryRepository {

    override fun observeCategories(type: CategoryType?, includeArchived: Boolean): Flow<List<Category>> =
        categories.observe(type?.name, includeArchived).map { list -> list.map { it.toModel() } }

    override suspend fun getCategory(id: String): Category? = categories.get(id)?.toModel()

    override suspend fun saveCategory(category: Category) = saveCategories(listOf(category))

    override suspend fun saveCategories(categories: List<Category>) {
        val stamp = stamper.stamp()
        this.categories.upsert(categories.map { it.toEntity(stamp) })
    }

    override suspend fun deleteCategory(id: String) {
        val stamp = stamper.stamp()
        categories.markDeleted(id, stamp.now, stamp.by)
    }

    override suspend fun countCategories(): Int = categories.count()
}

class RoomTransactionRepository(
    private val transactions: TransactionDao,
    private val stamper: WriteStamper,
) : TransactionRepository {

    override fun observeTransactions(filter: TransactionFilter): Flow<List<Transaction>> =
        transactions.observe(
            accountId = filter.accountId,
            categoryId = filter.categoryId,
            fromMs = filter.from?.toEpochMilliseconds(),
            toMs = filter.to?.toEpochMilliseconds(),
            limit = filter.limit ?: -1,
        ).map { list -> list.map { it.toModel() } }

    override suspend fun getTransaction(id: String): Transaction? = transactions.get(id)?.toModel()

    /** The author is whoever created the operation; editing by the other member keeps it. */
    override suspend fun saveTransaction(transaction: Transaction) {
        val stamp = stamper.stamp()
        val existing = transactions.get(transaction.id)
        val author = if (existing != null) existing.authorMemberId else transaction.authorMemberId ?: stamp.by
        transactions.upsert(listOf(transaction.copy(authorMemberId = author).toEntity(stamp)))
    }

    override suspend fun deleteTransaction(id: String) {
        val stamp = stamper.stamp()
        transactions.markDeleted(id, stamp.now, stamp.by)
    }

    override suspend fun countForAccount(accountId: String): Int = transactions.countForAccount(accountId)

    override suspend fun countForCategory(categoryId: String): Int = transactions.countForCategory(categoryId)
}

class RoomBudgetRepository(
    private val budgets: BudgetDao,
    private val stamper: WriteStamper,
) : BudgetRepository {

    override fun observeBudgets(): Flow<List<Budget>> =
        budgets.observeAll().map { list -> list.map { it.toModel() } }

    override suspend fun getBudgets(): List<Budget> = budgets.getAll().map { it.toModel() }

    override suspend fun applyBudgets(save: List<Budget>, deleteIds: List<String>) {
        val stamp = stamper.stamp()
        budgets.apply(save.map { it.toEntity(stamp) }, deleteIds, stamp.now, stamp.by)
    }
}
