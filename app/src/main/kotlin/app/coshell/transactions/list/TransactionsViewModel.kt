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
import kotlinx.coroutines.flow.asStateFlow
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

    private val _selection = MutableStateFlow(Selection())

    /** Rows chosen by a long press (19В); the screen swaps the bottom bar for «Дублировать · Удалить». */
    val selection: StateFlow<Selection> = _selection.asStateFlow()

    fun onLongPress(id: String) = _selection.update { it.longPress(id) }

    fun onToggle(id: String) = _selection.update { it.toggle(id) }

    fun clearSelection() = _selection.update { Selection() }

    /** Deletes the chosen rows at once; one «Отменить» brings them all back. */
    fun deleteSelected() {
        val ids = _selection.value.ids
        if (ids.isEmpty()) return
        _selection.value = Selection()
        viewModelScope.launch { deleteWithUndo(ids) }
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
        _selection.update { it.keepOnly(shown.mapTo(HashSet()) { t -> t.id }) }
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
