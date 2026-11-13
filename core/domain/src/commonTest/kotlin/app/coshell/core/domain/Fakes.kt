package app.coshell.core.domain

import app.coshell.core.domain.budgets.BudgetRepository
import app.coshell.core.model.Account
import app.coshell.core.model.Budget
import app.coshell.core.model.AccountWithBalance
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.domain.templates.TemplateRepository
import app.coshell.core.domain.tags.TagRepository
import app.coshell.core.domain.tags.TagUsage
import app.coshell.core.model.Tag
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionTemplate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeAccountRepository : AccountRepository {
    val items = MutableStateFlow<Map<String, Account>>(emptyMap())
    override fun observeAccounts(includeArchived: Boolean): Flow<List<Account>> =
        items.map { it.values.filter { a -> includeArchived || !a.archived } }
    override fun observeAccountsWithBalance(includeArchived: Boolean): Flow<List<AccountWithBalance>> =
        error("not used")
    override suspend fun getAccount(id: String) = items.value[id]
    override suspend fun saveAccount(account: Account) { items.value += account.id to account }
    override suspend fun deleteAccount(id: String) { items.value -= id }
}

class FakeCategoryRepository : CategoryRepository {
    val items = MutableStateFlow<Map<String, Category>>(emptyMap())
    override fun observeCategories(type: CategoryType?, includeArchived: Boolean): Flow<List<Category>> =
        items.map { all ->
            all.values.filter { (type == null || it.type == type) && (includeArchived || !it.archived) }
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
        items.map { all -> all.values.toList().let { list -> filter.limit?.let(list::take) ?: list } }
    override suspend fun getTransaction(id: String) = items.value[id]
    override suspend fun saveTransaction(transaction: Transaction) { items.value += transaction.id to transaction }
    override suspend fun deleteTransaction(id: String) { items.value -= id }
    override suspend fun countForAccount(accountId: String) =
        items.value.values.count { it.accountId == accountId || it.toAccountId == accountId }
    override suspend fun countForCategory(categoryId: String) =
        items.value.values.count { it.categoryId == categoryId }
}

class FakeTemplateRepository : TemplateRepository {
    val items = MutableStateFlow<Map<String, TransactionTemplate>>(emptyMap())
    override fun observeTemplates(): Flow<List<TransactionTemplate>> = items.map { it.values.sortedBy { t -> t.sortOrder } }
    override suspend fun getTemplate(id: String) = items.value[id]
    override suspend fun saveTemplate(template: TransactionTemplate) { items.value += template.id to template }
    override suspend fun deleteTemplate(id: String) { items.value -= id }
    override suspend fun maxSortOrder() = items.value.values.maxOfOrNull { it.sortOrder }
}

class FakeTagRepository : TagRepository {
    /** Live and deleted rows: `false` — live. */
    val rows = MutableStateFlow<Map<String, Pair<Tag, Boolean>>>(emptyMap())
    val usage = MutableStateFlow<Map<String, TagUsage>>(emptyMap())
    val live get() = rows.value.values.filter { !it.second }.map { it.first }
    override fun observeTags(): Flow<List<Tag>> = rows.map { r -> r.values.filter { !it.second }.map { it.first }.sortedBy { it.name.lowercase() } }
    override fun observeUsage(): Flow<Map<String, TagUsage>> = usage
    override suspend fun getTags() = live
    override suspend fun getTag(id: String) = rows.value[id]?.takeIf { !it.second }?.first
    override suspend fun isIdTaken(id: String) = id in rows.value
    override suspend fun saveTag(tag: Tag) { rows.value += tag.id to (tag to false) }
    override suspend fun deleteTag(id: String) { rows.value[id]?.let { rows.value += id to (it.first to true) } }
    override suspend fun addImportedTags(tags: List<Tag>) { tags.filter { it.id !in rows.value }.forEach { saveTag(it) } }
}

class FakeBudgetRepository : BudgetRepository {
    val items = MutableStateFlow<Map<String, Budget>>(emptyMap())
    override fun observeBudgets(): Flow<List<Budget>> = items.map { it.values.toList() }
    override suspend fun getBudgets() = items.value.values.toList()
    override suspend fun applyBudgets(save: List<Budget>, deleteIds: List<String>) {
        items.value = items.value - deleteIds.toSet() + save.associateBy { it.id }
    }
}
