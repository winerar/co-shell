package app.coshell.accounts.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.AccountRepository
import app.coshell.core.domain.family.OtherMembers
import app.coshell.core.model.AccountWithBalance
import app.coshell.core.model.MoneyFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@HiltViewModel
class AccountsViewModel @Inject constructor(
    accounts: AccountRepository,
    private val moneyFormat: MoneyFormat,
    otherMembers: OtherMembers = OtherMembers { flowOf(emptyMap()) },
) : ViewModel() {

    private val showArchived = MutableStateFlow(false)

    val state: StateFlow<AccountsUiState> =
        combine(accounts.observeAccountsWithBalance(includeArchived = true), showArchived, otherMembers.names(), ::buildState)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountsUiState(isLoading = true))

    fun toggleArchived() = showArchived.update { !it }

    internal fun buildState(
        accounts: List<AccountWithBalance>,
        showArchived: Boolean,
        others: Map<String, String> = emptyMap(),
    ): AccountsUiState {
        val toRow = { item: AccountWithBalance -> toRow(item, others) }
        val (archived, active) = accounts.partition { it.account.archived }
        return AccountsUiState(
            active = active.map(toRow),
            archived = archived.map(toRow),
            showArchived = showArchived,
        )
    }

    private fun toRow(item: AccountWithBalance, others: Map<String, String>) = AccountsUiState.Row(
        id = item.account.id,
        name = item.account.name,
        type = item.account.type,
        balance = moneyFormat.format(item.balance),
        isNegative = item.balance.isNegative,
        excludedFromTotal = item.account.excludedFromTotal,
        color = item.account.color,
        // Labels matter only with other members around.
        personalOf = if (item.account.isShared || others.isEmpty()) null else others[item.account.ownerMemberId].orEmpty(),
    )
}
