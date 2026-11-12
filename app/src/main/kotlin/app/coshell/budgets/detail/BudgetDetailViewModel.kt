package app.coshell.budgets.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.budgets.BudgetSource
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.budgets.forCategory
import app.coshell.core.domain.family.OtherMembers
import app.coshell.core.model.MoneyFormat
import app.coshell.navigation.BudgetDetailDestination
import app.coshell.transactions.list.buildTransactionDays
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.todayIn

/**
 * The envelope screen (`BudDetail`, #183): the limit on one category or folder in one month — live, so an
 * operation edited from here or a limit changed in the editor shows at once. Pure parts — `BudgetDetailBoard.kt`.
 */
@HiltViewModel
class BudgetDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    source: BudgetSource,
    accounts: AccountRepository,
    otherMembers: OtherMembers,
    moneyFormat: MoneyFormat,
    clock: Clock,
    timeZone: TimeZone,
) : ViewModel() {

    val categoryId: String = checkNotNull(savedStateHandle[BudgetDetailDestination.ARG_CATEGORY_ID])
    private val month = YearMonth(
        checkNotNull(savedStateHandle.get<Int>(BudgetDetailDestination.ARG_YEAR)),
        Month(checkNotNull(savedStateHandle.get<Int>(BudgetDetailDestination.ARG_MONTH))),
    )
    private val today = clock.todayIn(timeZone)
    private val format = moneyFormat.copy(trimZeroFraction = true)

    val state: StateFlow<BudgetDetailUiState> = combine(
        source.observeEnvelope(categoryId, month),
        accounts.observeAccounts(includeArchived = true),
        otherMembers.names(),
    ) { envelope, accountList, authors ->
        val snapshot = envelope.snapshot
        val (head, inner) = buildEnvelopeDetail(categoryId, snapshot.calculator().month(month, today), snapshot.categories, snapshot.ledger, today, format)
        val operations = buildTransactionDays(
            envelope.operations,
            accountList.associateBy { it.id },
            snapshot.categories.associateBy { it.id },
            moneyFormat,
            timeZone,
            today,
            authors,
        ).flatMap { it.rows }
        BudgetDetailUiState(
            month = month,
            name = snapshot.categories.firstOrNull { it.id == categoryId }?.name ?: "—",
            head = head,
            note = snapshot.budgets.forCategory(categoryId, month)?.note,
            categories = inner,
            operations = operations,
        )
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), BudgetDetailUiState(isLoading = true, month = month))

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
