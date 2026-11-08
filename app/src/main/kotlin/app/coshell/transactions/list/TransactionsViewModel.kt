package app.coshell.transactions.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.CategoryRepository
import app.coshell.core.domain.TransactionRepository
import app.coshell.core.model.MoneyFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Live list of all transactions; grouping and formatting live in [buildTransactionDays]. */
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    transactions: TransactionRepository,
    accounts: AccountRepository,
    categories: CategoryRepository,
    moneyFormat: MoneyFormat,
    clock: Clock,
    timeZone: TimeZone,
) : ViewModel() {

    // TODO(stage 5): paging once the Wallet history (thousands of rows) is imported.
    val state: StateFlow<TransactionsUiState> = combine(
        transactions.observeTransactions(),
        accounts.observeAccounts(includeArchived = true),
        categories.observeCategories(includeArchived = true),
    ) { items, accountList, categoryList ->
        TransactionsUiState(
            days = buildTransactionDays(
                transactions = items,
                accountsById = accountList.associateBy { it.id },
                categoriesById = categoryList.associateBy { it.id },
                moneyFormat = moneyFormat,
                timeZone = timeZone,
                today = clock.now().toLocalDateTime(timeZone).date,
            ),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransactionsUiState(isLoading = true))
}
