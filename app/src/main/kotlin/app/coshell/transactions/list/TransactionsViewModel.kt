package app.coshell.transactions.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.TransactionFilter
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.domain.family.OtherMembers
import app.coshell.core.model.MoneyFormat
import app.coshell.transactions.DeleteTransactionWithUndo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Live list of transactions, a page at a time ([GrowingWindow]); grouping and formatting live in [buildTransactionDays]. */
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

    /** The screen nears the end of the list: load the next page. */
    fun loadMore() = window.loadMore()

    val state: StateFlow<TransactionsUiState> = combine(
        window.observe { limit -> transactions.observeTransactions(TransactionFilter(limit = limit)) },
        accounts.observeAccounts(includeArchived = true),
        categories.observeCategories(includeArchived = true),
        otherMembers.names(),
    ) { page, accountList, categoryList, authors ->
        TransactionsUiState(
            hasMore = page.hasMore,
            days = buildTransactionDays(
                transactions = page.items,
                accountsById = accountList.associateBy { it.id },
                categoriesById = categoryList.associateBy { it.id },
                moneyFormat = moneyFormat,
                timeZone = timeZone,
                today = clock.now().toLocalDateTime(timeZone).date,
                authors = authors,
            ),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransactionsUiState(isLoading = true))
}
