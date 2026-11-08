package app.coshell.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coshell.core.domain.AccountRepository
import app.coshell.core.model.AccountWithBalance
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.sum
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeViewModel @Inject constructor(
    accounts: AccountRepository,
    private val moneyFormat: MoneyFormat,
) : ViewModel() {

    val state: StateFlow<HomeUiState> = accounts.observeAccountsWithBalance()
        .map(::buildState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState(isLoading = true))

    internal fun buildState(accounts: List<AccountWithBalance>): HomeUiState {
        // No exchange rates yet (stage 3), so totals are shown per currency.
        val totals = accounts.map { it.balance }
            .groupBy { it.currency }
            .map { (currency, items) -> moneyFormat.format(items.sum(currency)) }
        return HomeUiState(
            isLoading = false,
            totals = totals,
            accounts = accounts.map { (account, balance) ->
                HomeUiState.AccountRow(account.id, account.name, moneyFormat.format(balance), balance.isNegative)
            },
        )
    }
}
