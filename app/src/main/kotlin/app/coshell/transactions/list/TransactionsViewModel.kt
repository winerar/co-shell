package app.coshell.transactions.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.domain.family.OtherMembers
import app.coshell.core.model.MoneyFormat
import app.coshell.transactions.DeleteTransactionWithUndo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Live list of transactions, a page at a time ([GrowingWindow]), with search and filter chips ([ListFilter]);
 * grouping and formatting live in [buildTransactionDays].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    transactions: TransactionRepository,
    accounts: AccountRepository,
    categories: CategoryRepository,
    otherMembers: OtherMembers,
    moneyFormat: MoneyFormat,
    clock: Clock,
    timeZone: TimeZone,
    private val deleteWithUndo: DeleteTransactionWithUndo,
) : ViewModel() {

    /** Swipe in the list: deleted at once, «Отменить» in the snackbar. */
    fun delete(id: String) {
        viewModelScope.launch { deleteWithUndo(id) }
    }

    private val window = GrowingWindow()
    private val filter = MutableStateFlow(ListFilter())

    /** The screen nears the end of the list: load the next page. */
    fun loadMore() = window.loadMore()

    fun onFilter(change: (ListFilter) -> ListFilter) = filter.update(change)

    val state: StateFlow<TransactionsUiState> = combine(
        filter.flatMapLatest { f ->
            val day = clock.now().toLocalDateTime(timeZone).date
            window.observe { limit -> transactions.observeTransactions(f.toRepositoryFilter(limit, day, timeZone)) }
        },
        filter,
        accounts.observeAccounts(includeArchived = true),
        categories.observeCategories(includeArchived = true),
        otherMembers.names(),
    ) { page, f, accountList, categoryList, authors ->
        val accountsById = accountList.associateBy { it.id }
        val categoriesById = categoryList.associateBy { it.id }
        val shown = page.items.filter { f.matches(it, accountsById, categoriesById, moneyFormat) }
        TransactionsUiState(
            hasMore = page.hasMore && !f.needsAll,
            filter = f,
            accounts = accountList.filter { !it.archived }.map { TransactionsUiState.AccountChoice(it.id, it.name) },
            days = buildTransactionDays(
                transactions = shown,
                accountsById = accountsById,
                categoriesById = categoriesById,
                moneyFormat = moneyFormat,
                timeZone = timeZone,
                today = clock.now().toLocalDateTime(timeZone).date,
                authors = authors,
            ),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransactionsUiState(isLoading = true))
}
